package uz.cib.mobile.auth.integration;

import org.springframework.stereotype.Component;
import uz.cib.mobile.config.AppProperties;

@Component
public class MockIdentityProvider implements IdentityProvider {
    private final AppProperties properties;
    public MockIdentityProvider(AppProperties properties) { this.properties = properties; }

    @Override
    public IdentityResult verify(String pinfl, String passport) {
        if (!properties.getMock().getMyid().isEnabled()) {
            throw new IllegalStateException("Real MyID provider is not configured");
        }
        return new IdentityResult(true, "mock-subject-" + pinfl, "MOCK", "USER");
    }
}
