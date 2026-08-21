package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
public class AuthSessionEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "device_hash", nullable = false, length = 64) private String deviceHash;
    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64) private String refreshTokenHash;
    @Column(name = "token_family_id", nullable = false) private UUID tokenFamilyId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SessionStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "last_seen_at", nullable = false) private Instant lastSeenAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;

    protected AuthSessionEntity() {}

    public static AuthSessionEntity create(UUID userId, String deviceHash, String refreshHash, Instant expiresAt) {
        AuthSessionEntity s = new AuthSessionEntity();
        s.id = UUID.randomUUID();
        s.userId = userId;
        s.deviceHash = deviceHash;
        s.refreshTokenHash = refreshHash;
        s.tokenFamilyId = UUID.randomUUID();
        s.status = SessionStatus.ACTIVE;
        s.createdAt = Instant.now();
        s.lastSeenAt = s.createdAt;
        s.expiresAt = expiresAt;
        return s;
    }

    public void rotate(String newHash) { this.refreshTokenHash = newHash; this.lastSeenAt = Instant.now(); }
    public void revoke() { this.status = SessionStatus.REVOKED; this.revokedAt = Instant.now(); }
    public void expire() { this.status = SessionStatus.EXPIRED; this.revokedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDeviceHash() { return deviceHash; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public UUID getTokenFamilyId() { return tokenFamilyId; }
    public SessionStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
}
