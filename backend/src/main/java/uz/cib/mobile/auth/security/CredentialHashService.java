package uz.cib.mobile.auth.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.cib.mobile.common.HashingSupport;
import uz.cib.mobile.config.AppProperties;

@Service
public class CredentialHashService {
    private final PasswordEncoder encoder;
    private final String pinPepper;
    private final String dummyPasswordHash;

    public CredentialHashService(PasswordEncoder encoder, AppProperties properties) {
        this.encoder = encoder;
        this.pinPepper = properties.getSecurity().getPinPepper();
        this.dummyPasswordHash = encoder.encode("not-a-real-password-" + System.nanoTime());
    }

    public String hashPassword(String rawPassword) { return encoder.encode(rawPassword); }
    public boolean verifyPassword(String rawPassword, String hash) { return encoder.matches(rawPassword, hash); }
    public void consumeDummyPasswordCheck(String rawPassword) { encoder.matches(rawPassword, dummyPasswordHash); }

    public String hashPin(String pin) {
        return encoder.encode(HashingSupport.hmacSha256Hex(pinPepper, pin));
    }

    public boolean verifyPin(String pin, String hash) {
        return encoder.matches(HashingSupport.hmacSha256Hex(pinPepper, pin), hash);
    }
}
