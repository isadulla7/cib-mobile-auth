package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
public class UserCredentialEntity {
    @Id @Column(name = "user_id") private UUID userId;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "pin_hash", nullable = false) private String pinHash;
    @Column(name = "password_algo", nullable = false, length = 40) private String passwordAlgo;
    @Column(name = "pin_algo", nullable = false, length = 40) private String pinAlgo;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UserCredentialEntity() {}

    public static UserCredentialEntity create(UUID userId, String passwordHash, String pinHash) {
        UserCredentialEntity c = new UserCredentialEntity();
        c.userId = userId;
        c.passwordHash = passwordHash;
        c.pinHash = pinHash;
        c.passwordAlgo = "ARGON2ID";
        c.pinAlgo = "HMAC_SHA256+ARGON2ID";
        c.createdAt = Instant.now();
        c.updatedAt = c.createdAt;
        return c;
    }

    public UUID getUserId() { return userId; }
    public String getPasswordHash() { return passwordHash; }
    public String getPinHash() { return pinHash; }
}
