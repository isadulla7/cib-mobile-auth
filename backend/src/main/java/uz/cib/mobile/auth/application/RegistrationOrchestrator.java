package uz.cib.mobile.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uz.cib.mobile.auth.domain.*;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.engine.PolicyResolver;
import uz.cib.mobile.auth.engine.ValidationEngine;
import uz.cib.mobile.auth.infra.redis.*;
import uz.cib.mobile.auth.integration.IdentityProvider;
import uz.cib.mobile.auth.repository.UserCredentialRepository;
import uz.cib.mobile.auth.repository.UserRepository;
import uz.cib.mobile.auth.security.CredentialHashService;
import uz.cib.mobile.common.ApiException;
import uz.cib.mobile.common.PhoneNormalizer;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RegistrationOrchestrator {
    private final PolicyResolver policies;
    private final ValidationEngine validation;
    private final RegistrationFlowStore flows;
    private final OtpService otp;
    private final IdentityProvider identityProvider;
    private final CredentialHashService credentials;
    private final UserRepository users;
    private final UserCredentialRepository credentialRepository;
    private final DeviceTrustService deviceTrust;
    private final SessionService sessionService;
    private final PhoneNormalizer phoneNormalizer;
    private final AuditService audit;

    public RegistrationOrchestrator(
            PolicyResolver policies,
            ValidationEngine validation,
            RegistrationFlowStore flows,
            OtpService otp,
            IdentityProvider identityProvider,
            CredentialHashService credentials,
            UserRepository users,
            UserCredentialRepository credentialRepository,
            DeviceTrustService deviceTrust,
            SessionService sessionService,
            PhoneNormalizer phoneNormalizer,
            AuditService audit) {
        this.policies = policies;
        this.validation = validation;
        this.flows = flows;
        this.otp = otp;
        this.identityProvider = identityProvider;
        this.credentials = credentials;
        this.users = users;
        this.credentialRepository = credentialRepository;
        this.deviceTrust = deviceTrust;
        this.sessionService = sessionService;
        this.phoneNormalizer = phoneNormalizer;
        this.audit = audit;
    }

    public RegistrationStepResult start(String rawPhone, int requestedPolicyVersion, String remoteIp) {
        AuthPolicy active = policies.active();
        if (requestedPolicyVersion != active.version()) {
            throw new ApiException(HttpStatus.CONFLICT, "POLICY_VERSION_STALE",
                    "Reload auth configuration", Map.of("activePolicyVersion", active.version()));
        }

        String phone = phoneNormalizer.normalizeUzbekPhone(rawPhone);
        validation.validateRegistrationStep("PHONE", Map.of("phone", phone), active);

        String flowId = UUID.randomUUID().toString();
        RegistrationFlow flow = RegistrationFlow.started(flowId, active.version(), phone);
        flows.save(flow, Duration.ofSeconds(active.registrationFlowTtlSeconds()));

        try {
            otp.issue(OtpPurpose.REGISTRATION, flowId, phone, active);
        } catch (RuntimeException ex) {
            flows.delete(flowId);
            throw ex;
        }

        audit.record(null, "REGISTRATION_STARTED", "SUCCESS", flowId, null, remoteIp, Map.of());
        return new RegistrationStepResult(
                flowId, RegistrationState.OTP_SENT.name(), active.otpTtlSeconds(), "OTP");
    }

    public RegistrationStepResult verifyOtp(String flowId, String code, String remoteIp) {
        RegistrationFlow flow = requireFlow(flowId);
        requireState(flow, RegistrationState.OTP_SENT);
        AuthPolicy policy = policies.byVersion(flow.getPolicyVersion());

        otp.verify(OtpPurpose.REGISTRATION, flowId, code);
        flow.setState(RegistrationState.PHONE_VERIFIED);
        save(flow, policy);

        audit.record(null, "REGISTRATION_PHONE_VERIFIED", "SUCCESS", flowId, null, remoteIp, Map.of());
        return new RegistrationStepResult(flowId, flow.getState().name(), 0, "IDENTITY");
    }

    public RegistrationStepResult identity(String flowId, String rawPinfl, String rawPassport, String remoteIp) {
        RegistrationFlow flow = requireFlow(flowId);
        requireState(flow, RegistrationState.PHONE_VERIFIED);
        AuthPolicy policy = policies.byVersion(flow.getPolicyVersion());

        String pinfl = rawPinfl == null ? "" : rawPinfl.trim();
        String passport = rawPassport == null ? "" :
                rawPassport.replaceAll("\\s", "").toUpperCase(Locale.ROOT);

        validation.validateRegistrationStep(
                "IDENTITY", Map.of("pinfl", pinfl, "passport", passport), policy);

        IdentityProvider.IdentityResult result = identityProvider.verify(pinfl, passport);
        if (!result.verified()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "IDENTITY_NOT_VERIFIED",
                    "Identity verification failed");
        }

        flow.setPinfl(pinfl);
        flow.setPassport(passport);
        flow.setIdentitySubject(result.subjectId());
        flow.setState(RegistrationState.IDENTITY_VERIFIED);
        save(flow, policy);

        audit.record(null, "REGISTRATION_IDENTITY_VERIFIED", "SUCCESS", flowId, null, remoteIp, Map.of());
        return new RegistrationStepResult(flowId, flow.getState().name(), 0, "PASSWORD");
    }

    public RegistrationStepResult password(String flowId, String password, String remoteIp) {
        RegistrationFlow flow = requireFlow(flowId);
        requireState(flow, RegistrationState.IDENTITY_VERIFIED);
        AuthPolicy policy = policies.byVersion(flow.getPolicyVersion());

        validation.validateRegistrationStep("PASSWORD", Map.of("password", password), policy);
        flow.setPasswordHash(credentials.hashPassword(password));
        flow.setState(RegistrationState.PASSWORD_SET);
        save(flow, policy);

        audit.record(null, "REGISTRATION_PASSWORD_SET", "SUCCESS", flowId, null, remoteIp, Map.of());
        return new RegistrationStepResult(flowId, flow.getState().name(), 0, "PIN");
    }

    @Transactional
    public TokenPair pinAndComplete(String flowId, String pin, String deviceHash, String remoteIp) {
        RegistrationFlow flow = requireFlow(flowId);
        requireState(flow, RegistrationState.PASSWORD_SET);
        AuthPolicy policy = policies.byVersion(flow.getPolicyVersion());

        validation.validateRegistrationStep("PIN", Map.of("pin", pin), policy);

        // Re-check at commit boundary. DB UNIQUE constraints remain the final guard.
        if (users.existsByPhone(flow.getPhone())
                || users.existsByPinfl(flow.getPinfl())
                || users.existsByPassport(flow.getPassport())) {
            throw new ApiException(HttpStatus.CONFLICT, "REGISTRATION_NOT_AVAILABLE",
                    "Registration is not available for this identity");
        }

        UserEntity user = users.save(UserEntity.create(
                flow.getPhone(), flow.getPinfl(), flow.getPassport(), flow.getIdentitySubject()));

        credentialRepository.save(UserCredentialEntity.create(
                user.getId(), flow.getPasswordHash(), credentials.hashPin(pin)));

        deviceTrust.trust(user.getId(), deviceHash, policy);
        TokenPair tokens = sessionService.create(user.getId(), deviceHash, policy);

        Runnable afterCommit = () -> {
            flows.delete(flowId);
            audit.record(user.getId(), "REGISTRATION_COMPLETED", "SUCCESS", flowId,
                    deviceHash, remoteIp, Map.of("sessionId", tokens.sessionId().toString()));
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { afterCommit.run(); }
            });
        } else {
            afterCommit.run();
        }

        return tokens;
    }

    private RegistrationFlow requireFlow(String flowId) {
        return flows.find(flowId)
                .orElseThrow(() -> new ApiException(HttpStatus.GONE, "REGISTRATION_FLOW_EXPIRED",
                        "Registration flow expired or not found"));
    }

    private void requireState(RegistrationFlow flow, RegistrationState expected) {
        if (flow.getState() != expected) {
            throw new ApiException(HttpStatus.CONFLICT, "REGISTRATION_STATE_INVALID",
                    "Registration step is not allowed",
                    Map.of("expected", expected.name(), "actual", flow.getState().name()));
        }
    }

    private void save(RegistrationFlow flow, AuthPolicy policy) {
        flows.save(flow, Duration.ofSeconds(policy.registrationFlowTtlSeconds()));
    }

    public record RegistrationStepResult(
            String flowId,
            String state,
            int expiresIn,
            String nextStep
    ) {}
}
