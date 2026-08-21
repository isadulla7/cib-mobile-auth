package uz.cib.mobile.auth.infra.redis;
import java.util.UUID;

public class LoginChallenge {
    private String challengeId;
    private UUID userId;
    private String phone;
    private String deviceHash;
    private int policyVersion;

    public LoginChallenge() {}

    public LoginChallenge(String challengeId, UUID userId, String phone, String deviceHash, int policyVersion) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.phone = phone;
        this.deviceHash = deviceHash;
        this.policyVersion = policyVersion;
    }

    public String getChallengeId() { return challengeId; }
    public UUID getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getDeviceHash() { return deviceHash; }
    public int getPolicyVersion() { return policyVersion; }
}
