package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trusted_devices")
public class TrustedDeviceEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "device_hash", nullable = false, length = 64) private String deviceHash;
    @Column(name = "trust_until", nullable = false) private Instant trustUntil;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected TrustedDeviceEntity() {}

    public static TrustedDeviceEntity create(UUID userId, String deviceHash, Instant trustUntil) {
        TrustedDeviceEntity d = new TrustedDeviceEntity();
        d.id = UUID.randomUUID();
        d.userId = userId;
        d.deviceHash = deviceHash;
        d.trustUntil = trustUntil;
        d.createdAt = Instant.now();
        d.updatedAt = d.createdAt;
        return d;
    }

    public void renew(Instant trustUntil) {
        this.trustUntil = trustUntil;
        this.revokedAt = null;
        this.updatedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public String getDeviceHash() { return deviceHash; }
    public Instant getTrustUntil() { return trustUntil; }
    public Instant getRevokedAt() { return revokedAt; }
}
