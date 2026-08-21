package uz.cib.mobile.auth.engine;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidatorRegistry {
    private final Map<String, AuthFieldValidator> validators;

    public ValidatorRegistry(List<AuthFieldValidator> validators) {
        Map<String, AuthFieldValidator> map = new HashMap<>();
        for (AuthFieldValidator validator : validators) {
            if (map.put(validator.code(), validator) != null) {
                throw new IllegalStateException("Duplicate validator code: " + validator.code());
            }
        }
        this.validators = Map.copyOf(map);
    }

    public AuthFieldValidator require(String code) {
        AuthFieldValidator validator = validators.get(code);
        if (validator == null) throw new IllegalStateException("Unknown allowlisted validator: " + code);
        return validator;
    }
}
