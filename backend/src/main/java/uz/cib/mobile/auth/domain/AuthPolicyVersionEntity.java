package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_policy_version")
public class AuthPolicyVersionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private int version;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 128) private String checksum;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AuthPolicyVersionEntity() {}
    public Long getId() { return id; }
    public int getVersion() { return version; }
    public String getStatus() { return status; }
}
