package uz.cib.mobile.auth.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auth_policy_item")
public class AuthPolicyItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "policy_version_id", nullable = false) private Long policyVersionId;
    @Column(name = "policy_key", nullable = false, length = 100) private String policyKey;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode valueJson;

    protected AuthPolicyItemEntity() {}
    public Long getPolicyVersionId() { return policyVersionId; }
    public String getPolicyKey() { return policyKey; }
    public JsonNode getValueJson() { return valueJson; }
}
