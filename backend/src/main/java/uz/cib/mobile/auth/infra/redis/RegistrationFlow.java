package uz.cib.mobile.auth.infra.redis;

import uz.cib.mobile.auth.domain.RegistrationState;

public class RegistrationFlow {
    private String flowId;
    private int policyVersion;
    private RegistrationState state;
    private String phone;
    private String pinfl;
    private String passport;
    private String identitySubject;
    private String passwordHash;

    public RegistrationFlow() {}

    public static RegistrationFlow started(String flowId, int policyVersion, String phone) {
        RegistrationFlow f = new RegistrationFlow();
        f.flowId = flowId;
        f.policyVersion = policyVersion;
        f.phone = phone;
        f.state = RegistrationState.OTP_SENT;
        return f;
    }

    public String getFlowId() { return flowId; }
    public int getPolicyVersion() { return policyVersion; }
    public RegistrationState getState() { return state; }
    public void setState(RegistrationState state) { this.state = state; }
    public String getPhone() { return phone; }
    public String getPinfl() { return pinfl; }
    public void setPinfl(String pinfl) { this.pinfl = pinfl; }
    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }
    public String getIdentitySubject() { return identitySubject; }
    public void setIdentitySubject(String identitySubject) { this.identitySubject = identitySubject; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
