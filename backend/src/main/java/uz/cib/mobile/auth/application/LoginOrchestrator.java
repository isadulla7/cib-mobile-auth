package uz.cib.mobile.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uz.cib.mobile.auth.domain.UserEntity;
import uz.cib.mobile.auth.domain.UserStatus;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.engine.PolicyResolver;
import uz.cib.mobile.auth.infra.redis.*;
import uz.cib.mobile.auth.repository.UserCredentialRepository;
import uz.cib.mobile.auth.repository.UserRepository;
import uz.cib.mobile.auth.security.CredentialHashService;
import uz.cib.mobile.common.ApiException;
import uz.cib.mobile.common.PhoneNormalizer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginOrchestrator {
    private final UserRepository users;
    private final UserCredentialRepository credentialsRepository;
    private final CredentialHashService credentials;
    private final PolicyResolver policies;
    private final LoginAttemptService attempts;
    private final DeviceTrustService deviceTrust;
    private final LoginChallengeStore challenges;
    private final OtpService otp;
    private final SessionService sessions;
    private final PhoneNormalizer phoneNormalizer;
    private final AuditService audit;

    public LoginOrchestrator(
            UserRepository users,
            UserCredentialRepository credentialsRepository,
            CredentialHashService credentials,
            PolicyResolver policies,
            LoginAttemptService attempts,
            DeviceTrustService deviceTrust,
            LoginChallengeStore challenges,
            OtpService otp,
            SessionService sessions,
            PhoneNormalizer phoneNormalizer,
            AuditService audit) {
        this.users = users;
        this.credentialsRepository = credentialsRepository;
        this.credentials = credentials;
        this.policies = policies;
        this.attempts = attempts;
        this.deviceTrust = deviceTrust;
        this.challenges = challenges;
        this.otp = otp;
        this.sessions = sessions;
        this.phoneNormalizer = phoneNormalizer;
        this.audit = audit;
    }

    public LoginResult login(String rawPhone, String password, String deviceHash, String remoteIp) {
        AuthPolicy policy = policies.active();
        String phone = phoneNormalizer.normalizeUzbekPhone(rawPhone);
        attempts.assertAllowed(phone, remoteIp, policy);

        var userOptional = users.findByPhone(phone);
        if (userOptional.isEmpty()) {
            credentials.consumeDummyPasswordCheck(password);
            attempts.failure(phone, remoteIp, policy);
            audit.record(null, "LOGIN_PASSWORD", "FAILURE", null, deviceHash, remoteIp, Map.of());
            throw invalidCredentials();
        }

        UserEntity user = userOptional.get();
        var credential = credentialsRepository.findById(user.getId()).orElse(null);
        boolean valid = credential != null
                && credentials.verifyPassword(password, credential.getPasswordHash());

        if (!valid || user.getStatus() != UserStatus.ACTIVE) {
            attempts.failure(phone, remoteIp, policy);
            audit.record(user.getId(), "LOGIN_PASSWORD", "FAILURE", null, deviceHash, remoteIp, Map.of());
            throw invalidCredentials();
        }

        attempts.success(phone);

        if (deviceTrust.isTrusted(user.getId(), deviceHash)) {
            TokenPair tokens = sessions.create(user.getId(), deviceHash, policy);
            audit.record(user.getId(), "LOGIN_COMPLETED", "SUCCESS", null,
                    deviceHash, remoteIp, Map.of("trustedDevice", true));
            return LoginResult.authenticated(tokens);
        }

        String challengeId = UUID.randomUUID().toString();
        LoginChallenge challenge = new LoginChallenge(
                challengeId, user.getId(), user.getPhone(), deviceHash, policy.version());
        challenges.save(challenge, Duration.ofSeconds(policy.loginChallengeTtlSeconds()));

        try {
            otp.issue(OtpPurpose.LOGIN_NEW_DEVICE, challengeId, user.getPhone(), policy);
        } catch (RuntimeException ex) {
            challenges.delete(challengeId);
            throw ex;
        }

        audit.record(user.getId(), "LOGIN_NEW_DEVICE_CHALLENGE", "SUCCESS", challengeId,
                deviceHash, remoteIp, Map.of());

        return LoginResult.challenge(challengeId, policy.otpTtlSeconds());
    }

    @Transactional
    public LoginResult verifyNewDeviceOtp(String challengeId, String code, String deviceHash, String remoteIp) {
        LoginChallenge challenge = challenges.find(challengeId)
                .orElseThrow(() -> new ApiException(HttpStatus.GONE, "LOGIN_CHALLENGE_EXPIRED",
                        "Login challenge expired"));

        if (!challenge.getDeviceHash().equals(deviceHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "DEVICE_MISMATCH",
                    "Challenge does not belong to this device");
        }

        AuthPolicy policy = policies.byVersion(challenge.getPolicyVersion());
        otp.verify(OtpPurpose.LOGIN_NEW_DEVICE, challengeId, code);

        deviceTrust.trust(challenge.getUserId(), deviceHash, policy);
        TokenPair tokens = sessions.create(challenge.getUserId(), deviceHash, policy);

        Runnable afterCommit = () -> {
            challenges.delete(challengeId);
            audit.record(challenge.getUserId(), "LOGIN_NEW_DEVICE_VERIFIED", "SUCCESS", challengeId,
                    deviceHash, remoteIp, Map.of());
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { afterCommit.run(); }
            });
        } else {
            afterCommit.run();
        }

        return LoginResult.authenticated(tokens);
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    public record LoginResult(
            String status,
            TokenPair tokens,
            String challengeId,
            int expiresIn
    ) {
        static LoginResult authenticated(TokenPair tokens) {
            return new LoginResult("AUTHENTICATED", tokens, null, 0);
        }
        static LoginResult challenge(String challengeId, int expiresIn) {
            return new LoginResult("OTP_REQUIRED", null, challengeId, expiresIn);
        }
    }
}
