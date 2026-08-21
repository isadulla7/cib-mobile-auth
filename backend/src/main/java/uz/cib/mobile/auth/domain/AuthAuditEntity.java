package uz.cib.mobile.auth.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_audit")
public class AuthAuditEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "event_type", nullable = false, length = 80) private String eventType;
    @Column(nullable = false, length = 30) private String result;
    @Column(name = "correlation_id", length = 80) private String correlationId;
    @Column(name = "device_hash", length = 64) private String deviceHash;
    @Column(name = "ip_hash", length = 64) private String ipHash;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "jsonb") private JsonNode detailsJson;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AuthAuditEntity() {}

    public static AuthAuditEntity create(UUID userId, String eventType, String result,
                                         String correlationId, String deviceHash, String ipHash, JsonNode details) {
        AuthAuditEntity e = new AuthAuditEntity();
        e.userId = userId;
        e.eventType = eventType;
        e.result = result;
        e.correlationId = correlationId;
        e.deviceHash = deviceHash;
        e.ipHash = ipHash;
        e.detailsJson = details;
        e.createdAt = Instant.now();
        return e;
    }
}
