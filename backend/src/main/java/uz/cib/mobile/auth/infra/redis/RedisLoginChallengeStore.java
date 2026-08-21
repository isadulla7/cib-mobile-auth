package uz.cib.mobile.auth.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisLoginChallengeStore implements LoginChallengeStore {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisLoginChallengeStore(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public void save(LoginChallenge challenge, Duration ttl) {
        try {
            redis.opsForValue().set(key(challenge.getChallengeId()), mapper.writeValueAsString(challenge), ttl);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot save login challenge", e);
        }
    }

    @Override
    public Optional<LoginChallenge> find(String challengeId) {
        String json = redis.opsForValue().get(key(challengeId));
        if (json == null) return Optional.empty();
        try { return Optional.of(mapper.readValue(json, LoginChallenge.class)); }
        catch (Exception e) { throw new IllegalStateException("Cannot read login challenge", e); }
    }

    @Override public void delete(String challengeId) { redis.delete(key(challengeId)); }
    private String key(String id) { return "device_challenge:" + id; }
}
