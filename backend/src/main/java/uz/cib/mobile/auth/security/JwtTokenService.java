package uz.cib.mobile.auth.security;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.config.AppProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final AppProperties properties;

    public JwtTokenService(JwtEncoder encoder, AppProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public AccessToken issue(UUID userId, UUID sessionId, AuthPolicy policy) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(policy.accessTokenTtlSeconds());
        var jwt = properties.getSecurity().getJwt();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwt.getIssuer())
                .audience(List.of(jwt.getAudience()))
                .issuedAt(now)
                .expiresAt(expires)
                .subject(userId.toString())
                .claim("sid", sessionId.toString())
                .claim("scope", "mobile")
                .build();

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwt.getKeyId())
                .build();

        String token = encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return new AccessToken(token, policy.accessTokenTtlSeconds());
    }

    public record AccessToken(String token, int expiresInSeconds) {}
}
