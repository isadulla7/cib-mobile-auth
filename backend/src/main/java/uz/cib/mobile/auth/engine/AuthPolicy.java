package uz.cib.mobile.auth.engine;

public record AuthPolicy(
        long id,
        int version,
        int otpLength,
        int otpTtlSeconds,
        int otpMaxAttempts,
        int registrationFlowTtlSeconds,
        int loginChallengeTtlSeconds,
        int loginMaxAttempts,
        int loginBlockSeconds,
        int accessTokenTtlSeconds,
        int refreshTokenTtlSeconds,
        int trustedDeviceDays,
        int passwordMinLength,
        boolean passwordRequireDigit,
        boolean passwordRequireUpper,
        boolean passwordRequireLower,
        boolean passwordRequireSpecial,
        int pinLength
) {}
