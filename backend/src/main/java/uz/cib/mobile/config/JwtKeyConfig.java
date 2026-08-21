package uz.cib.mobile.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyConfig.class);

    @Bean
    KeyPair jwtKeyPair(AppProperties properties) {
        var jwt = properties.getSecurity().getJwt();
        try {
            if (StringUtils.hasText(jwt.getPrivateKeyPkcs8Base64())
                    && StringUtils.hasText(jwt.getPublicKeyX509Base64())) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(
                        new PKCS8EncodedKeySpec(Base64.getDecoder().decode(jwt.getPrivateKeyPkcs8Base64())));
                RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                        new X509EncodedKeySpec(Base64.getDecoder().decode(jwt.getPublicKeyX509Base64())));
                return new KeyPair(publicKey, privateKey);
            }

            if (!jwt.isAllowEphemeralKeys()) {
                throw new IllegalStateException(
                        "JWT RSA keys are required. Set JWT_PRIVATE_KEY_PKCS8_B64 and JWT_PUBLIC_KEY_X509_B64.");
            }

            log.warn("Using ephemeral RSA JWT keys. LOCAL/DEV only; tokens invalidate after restart.");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(3072);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize JWT keys", e);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keyPair, AppProperties properties) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(properties.getSecurity().getJwt().getKeyId())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keyPair, AppProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        var issuerValidator = JwtValidators.createDefaultWithIssuer(properties.getSecurity().getJwt().getIssuer());
        decoder.setJwtValidator(new DelegatingAudienceJwtValidator(
                issuerValidator, properties.getSecurity().getJwt().getAudience()));
        return decoder;
    }
}
