package uz.cib.mobile.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.cib.mobile.auth.domain.AuthSessionEntity;
import uz.cib.mobile.auth.domain.SessionStatus;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.infra.redis.RefreshReuseStore;
import uz.cib.mobile.auth.repository.AuthSessionRepository;
import uz.cib.mobile.auth.security.JwtTokenService;
import uz.cib.mobile.auth.security.OpaqueRefreshTokenService;
import uz.cib.mobile.common.ApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {
    private final AuthSessionRepository sessions;
    private final OpaqueRefreshTokenService refreshTokens;
    private final JwtTokenService jwtTokens;
    private final RefreshReuseStore reuseStore;
    private final SessionRevocationService revocation;

    public SessionService(AuthSessionRepository sessions,
                          OpaqueRefreshTokenService refreshTokens,
                          JwtTokenService jwtTokens,
                          RefreshReuseStore reuseStore,
                          SessionRevocationService revocation) {
        this.sessions = sessions;
        this.refreshTokens = refreshTokens;
        this.jwtTokens = jwtTokens;
        this.reuseStore = reuseStore;
        this.revocation = revocation;
    }

    @Transactional
    public TokenPair create(UUID userId, String deviceHash, AuthPolicy policy) {
        String rawRefresh = refreshTokens.generate();
        String refreshHash = refreshTokens.hash(rawRefresh);
        Instant expiresAt = Instant.now().plusSeconds(policy.refreshTokenTtlSeconds());

        AuthSessionEntity session = AuthSessionEntity.create(userId, deviceHash, refreshHash, expiresAt);
        sessions.save(session);

        var access = jwtTokens.issue(userId, session.getId(), policy);
        return new TokenPair(access.token(), access.expiresInSeconds(), rawRefresh,
                policy.refreshTokenTtlSeconds(), session.getId());
    }

    @Transactional
    public TokenPair refresh(String rawRefresh, String deviceHash, AuthPolicy policy) {
        String presentedHash = refreshTokens.hash(rawRefresh);

        AuthSessionEntity session = sessions.findByRefreshTokenHashAndStatus(presentedHash, SessionStatus.ACTIVE)
                .orElseGet(() -> {
                    reuseStore.findFamily(presentedHash).ifPresent(revocation::revokeFamily);
                    throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID",
                            "Refresh token is invalid");
                });

        if (!session.getDeviceHash().equals(deviceHash)) {
            revocation.revokeFamily(session.getTokenFamilyId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_DEVICE_MISMATCH",
                    "Refresh token cannot be used from this device");
        }

        if (!session.getExpiresAt().isAfter(Instant.now())) {
            revocation.expireSession(session.getId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh token expired");
        }

        String newRaw = refreshTokens.generate();
        String newHash = refreshTokens.hash(newRaw);

        Duration reuseTtl = Duration.between(Instant.now(), session.getExpiresAt());
        if (!reuseTtl.isNegative() && !reuseTtl.isZero()) {
            reuseStore.markConsumed(presentedHash, session.getTokenFamilyId(), reuseTtl);
        }

        session.rotate(newHash);
        var access = jwtTokens.issue(session.getUserId(), session.getId(), policy);
        int remaining = (int) Math.max(0, Duration.between(Instant.now(), session.getExpiresAt()).toSeconds());

        return new TokenPair(access.token(), access.expiresInSeconds(), newRaw, remaining, session.getId());
    }

    @Transactional
    public void logout(String rawRefresh, String deviceHash) {
        String hash = refreshTokens.hash(rawRefresh);
        sessions.findByRefreshTokenHashAndStatus(hash, SessionStatus.ACTIVE).ifPresent(session -> {
            if (session.getDeviceHash().equals(deviceHash)) session.revoke();
        });
    }
}
