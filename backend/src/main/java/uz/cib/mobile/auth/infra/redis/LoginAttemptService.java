package uz.cib.mobile.auth.infra.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.common.ApiException;
import uz.cib.mobile.common.HashingSupport;

import java.time.Duration;

@Service
public class LoginAttemptService {
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
        local n = redis.call('INCR', KEYS[1])
        if n == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
        return n
        """, Long.class);

    private final StringRedisTemplate redis;

    public LoginAttemptService(StringRedisTemplate redis) { this.redis = redis; }

    public void assertAllowed(String normalizedPhone, String remoteIp, AuthPolicy policy) {
        if (count(phoneKey(normalizedPhone)) >= policy.loginMaxAttempts()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "LOGIN_THROTTLED",
                    "Too many login attempts. Try again later.");
        }
        if (count(ipKey(remoteIp)) >= 50) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "LOGIN_THROTTLED",
                    "Too many login attempts. Try again later.");
        }
    }

    public void failure(String normalizedPhone, String remoteIp, AuthPolicy policy) {
        increment(phoneKey(normalizedPhone), Duration.ofSeconds(policy.loginBlockSeconds()));
        increment(ipKey(remoteIp), Duration.ofSeconds(policy.loginBlockSeconds()));
    }

    public void success(String normalizedPhone) {
        redis.delete(phoneKey(normalizedPhone));
    }

    private void increment(String key, Duration ttl) {
        redis.execute(INCREMENT_SCRIPT, List.of(key), Long.toString(ttl.toMillis()));
    }

    private long count(String key) {
        String value = redis.opsForValue().get(key);
        if (value == null) return 0L;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return 0L; }
    }

    private String phoneKey(String phone) { return "login_fail:" + HashingSupport.sha256Hex(phone); }
    private String ipKey(String ip) {
        return "rate:login:ip:" + HashingSupport.sha256Hex(ip == null ? "unknown" : ip);
    }
}
