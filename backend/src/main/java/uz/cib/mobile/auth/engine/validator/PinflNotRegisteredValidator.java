package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.repository.UserRepository;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PinflNotRegisteredValidator implements AuthFieldValidator {
    private final UserRepository users;
    public PinflNotRegisteredValidator(UserRepository users) { this.users = users; }
    @Override public String code() { return "PINFL_NOT_REGISTERED"; }
    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy) {
        if (value instanceof String pinfl && users.existsByPinfl(pinfl)) {
            throw new ApiException(HttpStatus.CONFLICT, "REGISTRATION_NOT_AVAILABLE",
                    "Registration is not available for this identity");
        }
    }
}
