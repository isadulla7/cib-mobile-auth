package uz.cib.mobile.auth.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uz.cib.mobile.config.AppProperties;

@Component
public class MockSmsGateway implements SmsGateway {
    private static final Logger log = LoggerFactory.getLogger(MockSmsGateway.class);
    private final AppProperties properties;

    public MockSmsGateway(AppProperties properties) { this.properties = properties; }

    @Override
    public SmsResult sendOtp(String phone, String code) {
        if (!properties.getMock().getSms().isEnabled()) {
            throw new IllegalStateException("Real SMS provider is not configured");
        }
        log.info("MOCK SMS accepted for phone={} otp={}", phone, code);
        return new SmsResult(true, "mock-sms-static-response");
    }
}
