package uz.cib.mobile.auth.infra.redis;
import java.time.Duration;

public interface OtpStore {
    void put(OtpPurpose purpose, String challengeId, String otpHash, int maxAttempts, Duration ttl);
    OtpVerificationResult verifyAndConsume(OtpPurpose purpose, String challengeId, String candidateHash);
    void delete(OtpPurpose purpose, String challengeId);
}
