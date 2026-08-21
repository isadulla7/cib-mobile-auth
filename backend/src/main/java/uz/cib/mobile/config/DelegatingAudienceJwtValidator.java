package uz.cib.mobile.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class DelegatingAudienceJwtValidator implements OAuth2TokenValidator<Jwt> {
    private final OAuth2TokenValidator<Jwt> delegate;
    private final String audience;

    public DelegatingAudienceJwtValidator(OAuth2TokenValidator<Jwt> delegate, String audience) {
        this.delegate = delegate;
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        OAuth2TokenValidatorResult base = delegate.validate(token);
        if (base.hasErrors()) return base;
        if (!token.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Required audience is missing", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}
