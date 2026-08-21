package uz.cib.mobile.common;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PhoneNormalizer {
    public String normalizeUzbekPhone(String raw) {
        if (raw == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_INVALID", "Phone is required");
        }
        String value = raw.replaceAll("[\\s()\\-]", "");
        if (value.startsWith("998")) value = "+" + value;
        else if (value.matches("\\d{9}")) value = "+998" + value;

        if (!value.matches("\\+998\\d{9}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_INVALID", "Phone must be a valid +998 number");
        }
        return value;
    }
}
