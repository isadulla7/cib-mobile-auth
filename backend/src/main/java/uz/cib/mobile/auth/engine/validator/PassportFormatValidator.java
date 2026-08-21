package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PassportFormatValidator implements AuthFieldValidator {
    @Override public String code() { return "PASSPORT_FORMAT"; }
    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy) {
        if (!(value instanceof String passport) || !passport.matches("[A-Z]{2}\\d{7}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSPORT_INVALID", "Passport must match AA1234567");
        }
    }
}
