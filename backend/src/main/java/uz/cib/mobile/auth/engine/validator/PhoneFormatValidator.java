package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PhoneFormatValidator implements AuthFieldValidator {
    @Override public String code() { return "PHONE_FORMAT"; }
    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy) {
        if (!(value instanceof String phone) || !phone.matches("\\+998\\d{9}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHONE_INVALID", "Invalid phone format");
        }
    }
}
