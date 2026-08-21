package uz.cib.mobile.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SecuritySecretsValidator {
    private final AppProperties properties;

    public SecuritySecretsValidator(AppProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validate() {
        requireStrong("app.security.otp-pepper", properties.getSecurity().getOtpPepper());
        requireStrong("app.security.pin-pepper", properties.getSecurity().getPinPepper());
        requireStrong("app.security.device-pepper", properties.getSecurity().getDevicePepper());
    }

    private void requireStrong(String name, String value) {
        if (!StringUtils.hasText(value) || value.length() < 32) {
            throw new IllegalStateException(name + " must be at least 32 characters");
        }
    }
}
