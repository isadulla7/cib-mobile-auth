package uz.cib.mobile.auth.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.cib.mobile.auth.domain.AuthPolicyItemEntity;
import uz.cib.mobile.auth.domain.AuthPolicyVersionEntity;
import uz.cib.mobile.auth.repository.AuthPolicyItemRepository;
import uz.cib.mobile.auth.repository.AuthPolicyVersionRepository;
import uz.cib.mobile.common.ApiException;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PolicyResolver {
    private static final String ACTIVE_VERSION_KEY = "auth_policy:ACTIVE_VERSION";
    private final AuthPolicyVersionRepository versionRepository;
    private final AuthPolicyItemRepository itemRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public PolicyResolver(AuthPolicyVersionRepository versionRepository,
                          AuthPolicyItemRepository itemRepository,
                          StringRedisTemplate redis,
                          ObjectMapper objectMapper) {
        this.versionRepository = versionRepository;
        this.itemRepository = itemRepository;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public AuthPolicy active() {
        String cached = redis.opsForValue().get(ACTIVE_VERSION_KEY);
        if (cached != null) {
            try { return byVersion(Integer.parseInt(cached)); }
            catch (NumberFormatException ignored) { redis.delete(ACTIVE_VERSION_KEY); }
        }

        AuthPolicyVersionEntity version = versionRepository.findFirstByStatusOrderByVersionDesc("ACTIVE")
                .orElseThrow(() -> new IllegalStateException("No ACTIVE auth policy"));
        redis.opsForValue().set(
                ACTIVE_VERSION_KEY, Integer.toString(version.getVersion()), Duration.ofSeconds(30));
        return byVersion(version.getVersion());
    }

    public AuthPolicy byVersion(int versionNumber) {
        String key = "auth_policy:" + versionNumber;
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try { return objectMapper.readValue(cached, AuthPolicy.class); }
            catch (Exception ignored) { redis.delete(key); }
        }

        AuthPolicyVersionEntity version = versionRepository.findByVersion(versionNumber)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.CONFLICT, "POLICY_VERSION_UNKNOWN", "Auth policy version is unavailable"));

        Map<String, AuthPolicyItemEntity> items = itemRepository.findByPolicyVersionId(version.getId()).stream()
                .collect(Collectors.toMap(AuthPolicyItemEntity::getPolicyKey, Function.identity()));

        AuthPolicy policy = new AuthPolicy(
                version.getId(),
                version.getVersion(),
                integer(items, "OTP_LENGTH"),
                integer(items, "OTP_TTL_SECONDS"),
                integer(items, "OTP_MAX_ATTEMPTS"),
                integer(items, "REGISTRATION_FLOW_TTL_SECONDS"),
                integer(items, "LOGIN_CHALLENGE_TTL_SECONDS"),
                integer(items, "LOGIN_MAX_ATTEMPTS"),
                integer(items, "LOGIN_BLOCK_SECONDS"),
                integer(items, "ACCESS_TOKEN_TTL_SECONDS"),
                integer(items, "REFRESH_TOKEN_TTL_SECONDS"),
                integer(items, "TRUSTED_DEVICE_DAYS"),
                integer(items, "PASSWORD_MIN_LENGTH"),
                bool(items, "PASSWORD_REQUIRE_DIGIT"),
                bool(items, "PASSWORD_REQUIRE_UPPER"),
                bool(items, "PASSWORD_REQUIRE_LOWER"),
                bool(items, "PASSWORD_REQUIRE_SPECIAL"),
                integer(items, "PIN_LENGTH")
        );
        validatePolicy(policy);

        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(policy), Duration.ofMinutes(5));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot cache auth policy", e);
        }
        return policy;
    }

    private void validatePolicy(AuthPolicy p) {
        if (p.otpLength() < 4 || p.otpLength() > 8) throw invalid("OTP_LENGTH");
        if (p.otpTtlSeconds() < 30 || p.otpTtlSeconds() > 600) throw invalid("OTP_TTL_SECONDS");
        if (p.otpMaxAttempts() < 1 || p.otpMaxAttempts() > 10) throw invalid("OTP_MAX_ATTEMPTS");
        if (p.registrationFlowTtlSeconds() < 300 || p.registrationFlowTtlSeconds() > 7200) throw invalid("REGISTRATION_FLOW_TTL_SECONDS");
        if (p.accessTokenTtlSeconds() < 60 || p.accessTokenTtlSeconds() > 3600) throw invalid("ACCESS_TOKEN_TTL_SECONDS");
        if (p.refreshTokenTtlSeconds() < 3600 || p.refreshTokenTtlSeconds() > 7776000) throw invalid("REFRESH_TOKEN_TTL_SECONDS");
        if (p.passwordMinLength() < 8 || p.passwordMinLength() > 128) throw invalid("PASSWORD_MIN_LENGTH");
        if (p.pinLength() < 4 || p.pinLength() > 8) throw invalid("PIN_LENGTH");
        if (p.loginMaxAttempts() < 1 || p.loginMaxAttempts() > 20) throw invalid("LOGIN_MAX_ATTEMPTS");
    }

    private IllegalStateException invalid(String key) {
        return new IllegalStateException("Unsafe auth policy value for " + key);
    }

    private int integer(Map<String, AuthPolicyItemEntity> items, String key) {
        return required(items, key).getValueJson().asInt();
    }

    private boolean bool(Map<String, AuthPolicyItemEntity> items, String key) {
        return required(items, key).getValueJson().asBoolean();
    }

    private AuthPolicyItemEntity required(Map<String, AuthPolicyItemEntity> items, String key) {
        AuthPolicyItemEntity item = items.get(key);
        if (item == null) throw new IllegalStateException("Missing auth policy key: " + key);
        return item;
    }
}
