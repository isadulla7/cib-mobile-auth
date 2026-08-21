package uz.cib.mobile.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.infra.redis.*;
import uz.cib.mobile.auth.integration.SmsGateway;
import uz.cib.mobile.auth.security.OtpHasher;
import uz.cib.mobile.common.ApiException;
import uz.cib.mobile.config.AppProperties;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {
    private final SecureRandom random = new SecureRandom();
    private final OtpStore store;
    private final OtpHasher hasher;
    private final SmsGateway smsGateway;
    private final AppProperties properties;

    public OtpService(OtpStore store, OtpHasher hasher, SmsGateway smsGateway, AppProperties properties) {
        this.store = store;
        this.hasher = hasher;
        this.smsGateway = smsGateway;
        this.properties = properties;
    }

    public void issue(OtpPurpose purpose, String challengeId, String phone, AuthPolicy policy) {
        String code = generate(policy.otpLength());
        String hash = hasher.hash(purpose.name(), challengeId, code);
        store.put(purpose, challengeId, hash, policy.otpMaxAttempts(), Duration.ofSeconds(policy.otpTtlSeconds()));
        try {
            SmsGateway.SmsResult result = smsGateway.sendOtp(phone, code);
            if (!result.accepted()) {
                store.delete(purpose, challengeId);
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SMS_UNAVAILABLE", "Unable to send OTP");
            }
        } catch (RuntimeException ex) {
            store.delete(purpose, challengeId);
            if (ex instanceof ApiException api) throw api;
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SMS_UNAVAILABLE", "Unable to send OTP");
        }
    }

    public void verify(OtpPurpose purpose, String challengeId, String code) {
        OtpVerificationResult result = store.verifyAndConsume(
                purpose, challengeId, hasher.hash(purpose.name(), challengeId, code));
        switch (result) {
            case VERIFIED -> { return; }
            case INVALID -> throw new ApiException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Invalid OTP");
            case EXPIRED -> throw new ApiException(HttpStatus.GONE, "OTP_EXPIRED", "OTP expired or already used");
            case LOCKED -> throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_LOCKED", "OTP attempts exceeded");
        }
    }

    private String generate(int length) {
        String fixed = properties.getMock().getSms().getFixedOtp();
        if (properties.getMock().getSms().isEnabled() && StringUtils.hasText(fixed)) {
            if (fixed.length() != length) {
                throw new IllegalStateException("MOCK_SMS_FIXED_OTP length must equal policy OTP_LENGTH");
            }
            return fixed;
        }

        int bound = (int) Math.pow(10, length);
        int minimum = (int) Math.pow(10, length - 1);
        int value = minimum + random.nextInt(bound - minimum);
        return Integer.toString(value);
    }
}
