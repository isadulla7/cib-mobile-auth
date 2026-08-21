package uz.cib.mobile.auth.application;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.engine.PolicyResolver;
import uz.cib.mobile.auth.repository.AuthUiConfigRepository;

import java.util.List;
import java.util.Map;

@Service
public class AuthConfigService {
    private final PolicyResolver policies;
    private final AuthUiConfigRepository uiConfigs;

    public AuthConfigService(PolicyResolver policies, AuthUiConfigRepository uiConfigs) {
        this.policies = policies;
        this.uiConfigs = uiConfigs;
    }

    public AuthConfigResponse current() {
        AuthPolicy policy = policies.active();
        List<UiStep> steps = uiConfigs.findEnabledByPolicyVersionId(policy.id()).stream()
                .map(c -> new UiStep(
                        c.getFlowCode(), c.getStepCode(), c.getOrderNo(),
                        c.getMinAppVersion(), c.getConfigJson()))
                .toList();

        return new AuthConfigResponse(
                1,
                policy.version(),
                Map.of(
                        "otpLength", policy.otpLength(),
                        "otpTtlSeconds", policy.otpTtlSeconds(),
                        "passwordMinLength", policy.passwordMinLength(),
                        "pinLength", policy.pinLength()
                ),
                steps
        );
    }

    public record AuthConfigResponse(
            int schemaVersion,
            int policyVersion,
            Map<String, Object> publicPolicy,
            List<UiStep> ui
    ) {}

    public record UiStep(
            String flowCode,
            String stepCode,
            int order,
            String minAppVersion,
            JsonNode config
    ) {}
}
