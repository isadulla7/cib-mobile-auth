package uz.cib.mobile.auth.engine;

import org.springframework.stereotype.Service;
import uz.cib.mobile.auth.repository.AuthValidationMapRepository;
import java.util.Map;

@Service
public class ValidationEngine {
    private final AuthValidationMapRepository repository;
    private final ValidatorRegistry registry;

    public ValidationEngine(AuthValidationMapRepository repository, ValidatorRegistry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    public void validateRegistrationStep(String stepCode, Map<String, Object> values, AuthPolicy policy) {
        repository.findForStep(policy.id(), "REGISTRATION", stepCode)
                .forEach(mapping -> registry.require(mapping.getValidatorCode())
                        .validate(mapping.getFieldCode(), values.get(mapping.getFieldCode()), values, policy));
    }
}
