package uz.cib.mobile.auth.infra.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.List;

@Repository
public class RedisOtpStore implements OtpStore {
    private static final DefaultRedisScript<Long> PUT_SCRIPT = new DefaultRedisScript<>("""
        redis.call('HSET', KEYS[1], 'hash', ARGV[1], 'attempts', '0', 'maxAttempts', ARGV[2])
        redis.call('PEXPIRE', KEYS[1], ARGV[3])
        return 1
        """, Long.class);

    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>("""
        local stored = redis.call('HGET', KEYS[1], 'hash')
        if not stored then return -2 end
        local attempts = tonumber(redis.call('HGET', KEYS[1], 'attempts') or '0')
        local maxAttempts = tonumber(redis.call('HGET', KEYS[1], 'maxAttempts') or '0')
        if attempts >= maxAttempts then return -1 end
        if stored == ARGV[1] then
          redis.call('DEL', KEYS[1])
          return 1
        end
        attempts = redis.call('HINCRBY', KEYS[1], 'attempts', 1)
        if attempts >= maxAttempts then return -1 end
        return 0
        """, Long.class);

    private final StringRedisTemplate redis;

    public RedisOtpStore(StringRedisTemplate redis) { this.redis = redis; }

    @Override
    public void put(OtpPurpose purpose, String challengeId, String otpHash, int maxAttempts, Duration ttl) {
        redis.execute(
                PUT_SCRIPT,
                List.of(key(purpose, challengeId)),
                otpHash,
                Integer.toString(maxAttempts),
                Long.toString(ttl.toMillis())
        );
    }

    @Override
    public OtpVerificationResult verifyAndConsume(OtpPurpose purpose, String challengeId, String candidateHash) {
        Long result = redis.execute(VERIFY_SCRIPT, List.of(key(purpose, challengeId)), candidateHash);
        if (result == null || result == -2L) return OtpVerificationResult.EXPIRED;
        if (result == -1L) return OtpVerificationResult.LOCKED;
        if (result == 1L) return OtpVerificationResult.VERIFIED;
        return OtpVerificationResult.INVALID;
    }

    @Override
    public void delete(OtpPurpose purpose, String challengeId) {
        redis.delete(key(purpose, challengeId));
    }

    private String key(OtpPurpose purpose, String challengeId) {
        return "otp:" + purpose.name().toLowerCase() + ":" + challengeId;
    }
}
