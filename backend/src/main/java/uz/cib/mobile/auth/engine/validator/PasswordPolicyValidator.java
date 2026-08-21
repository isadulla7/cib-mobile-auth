package uz.cib.mobile.auth.engine.validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.cib.mobile.auth.engine.AuthFieldValidator;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import java.util.Map;

@Component
public class PasswordPolicyValidator implements AuthFieldValidator {
    @Override public String code() { return "PASSWORD_POLICY"; }

    @Override
    public void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy p) {
        if (!(value instanceof String password) || password.length() < p.passwordMinLength()) {
            fail("Password is too short");
        }
        String password = (String) value;
        if (p.passwordRequireDigit() && password.chars().noneMatch(Character::isDigit)) fail("Password requires a digit");
        if (p.passwordRequireUpper() && password.chars().noneMatch(Character::isUpperCase)) fail("Password requires uppercase");
        if (p.passwordRequireLower() && password.chars().noneMatch(Character::isLowerCase)) fail("Password requires lowercase");
        if (p.passwordRequireSpecial() && password.chars().allMatch(Character::isLetterOrDigit)) fail("Password requires a special character");
    }

    private void fail(String message) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_FAILED", message);
    }
}
