package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PinFormatValidator implements AuthFieldValidator {
    @Override public String code() { return "PIN_FORMAT"; }
    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy) {
        if (!(value instanceof String pin) || !pin.matches("\\d{" + policy.pinLength() + "}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PIN_INVALID",
                    "PIN must contain exactly " + policy.pinLength() + " digits");
        }
    }
}
