package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 20) private String phone;
    @Column(nullable = false, unique = true, length = 14) private String pinfl;
    @Column(nullable = false, unique = true, length = 20) private String passport;
    @Column(name = "identity_subject", length = 120) private String identitySubject;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UserEntity() {}

    public static UserEntity create(String phone, String pinfl, String passport, String identitySubject) {
        UserEntity u = new UserEntity();
        u.id = UUID.randomUUID();
        u.phone = phone;
        u.pinfl = pinfl;
        u.passport = passport;
        u.identitySubject = identitySubject;
        u.status = UserStatus.ACTIVE;
        u.createdAt = Instant.now();
        u.updatedAt = u.createdAt;
        return u;
    }

    public UUID getId() { return id; }
    public String getPhone() { return phone; }
    public String getPinfl() { return pinfl; }
    public String getPassport() { return passport; }
    public UserStatus getStatus() { return status; }
}
