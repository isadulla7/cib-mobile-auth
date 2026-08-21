package uz.cib.mobile.auth.engine;
import java.util.Map;

public interface AuthFieldValidator {
    String code();
    void validate(String fieldCode, Object value, Map<String, Object> values, AuthPolicy policy);
}
