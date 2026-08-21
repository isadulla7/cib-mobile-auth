package uz.cib.mobile.auth.security;

import org.springframework.stereotype.Service;
import uz.cib.mobile.common.HashingSupport;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class OpaqueRefreshTokenService {
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String raw) { return HashingSupport.sha256Hex(raw); }
}
