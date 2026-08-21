package uz.cib.mobile.auth.application;
import java.util.UUID;

public record TokenPair(
        String accessToken,
        int accessTokenExpiresIn,
        String refreshToken,
        int refreshTokenExpiresIn,
        UUID sessionId
) {}
