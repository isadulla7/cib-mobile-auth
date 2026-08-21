package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PinflFormatValidator implements AuthFieldValidator {
    @Override public String code() { return "PINFL_FORMAT"; }
    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy) {
        if (!(value instanceof String pinfl) || !pinfl.matches("\\d{14}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PINFL_INVALID", "PINFL must contain 14 digits");
        }
    }
}
