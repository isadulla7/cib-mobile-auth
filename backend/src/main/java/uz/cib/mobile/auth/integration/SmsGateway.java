package uz.cib.mobile.auth.integration;

public interface SmsGateway {
    SmsResult sendOtp(String phone, String code);
    record SmsResult(boolean accepted, String messageId) {}
}
