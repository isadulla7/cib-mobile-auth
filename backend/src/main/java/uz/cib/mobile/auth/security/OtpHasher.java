package uz.cib.mobile.auth.security;

import org.springframework.stereotype.Service;
import uz.cib.mobile.common.HashingSupport;
import uz.cib.mobile.config.AppProperties;

@Service
public class OtpHasher {
    private final String pepper;
    public OtpHasher(AppProperties properties) { this.pepper = properties.getSecurity().getOtpPepper(); }

    public String hash(String purpose, String challengeId, String code) {
        return HashingSupport.hmacSha256Hex(pepper, purpose + ":" + challengeId + ":" + code);
    }
}
