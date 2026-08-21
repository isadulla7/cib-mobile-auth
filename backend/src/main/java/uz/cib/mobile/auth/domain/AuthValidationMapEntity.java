package uz.cib.mobile.auth.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_validation_map")
public class AuthValidationMapEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "policy_version_id", nullable = false) private Long policyVersionId;
    @Column(name = "domain_code", nullable = false, length = 30) private String domainCode;
    @Column(name = "step_code", nullable = false, length = 40) private String stepCode;
    @Column(name = "field_code", nullable = false, length = 60) private String fieldCode;
    @Column(name = "validator_code", nullable = false, length = 80) private String validatorCode;
    @Column(name = "order_no", nullable = false) private int orderNo;
    @Column(nullable = false) private boolean enabled;

    protected AuthValidationMapEntity() {}
    public String getFieldCode() { return fieldCode; }
    public String getValidatorCode() { return validatorCode; }
    public int getOrderNo() { return orderNo; }
}
