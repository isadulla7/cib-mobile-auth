package uz.cib.mobile.auth.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auth_ui_config")
public class AuthUiConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "policy_version_id", nullable = false) private Long policyVersionId;
    @Column(name = "flow_code", nullable = false, length = 30) private String flowCode;
    @Column(name = "step_code", nullable = false, length = 40) private String stepCode;
    @Column(name = "order_no", nullable = false) private int orderNo;
    @Column(name = "min_app_version", length = 30) private String minAppVersion;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode configJson;
    @Column(nullable = false) private boolean enabled;

    protected AuthUiConfigEntity() {}
    public String getFlowCode() { return flowCode; }
    public String getStepCode() { return stepCode; }
    public int getOrderNo() { return orderNo; }
    public String getMinAppVersion() { return minAppVersion; }
    public JsonNode getConfigJson() { return configJson; }
}
