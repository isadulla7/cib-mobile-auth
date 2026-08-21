package uz.cib.mobile.auth.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisRegistrationFlowStore implements RegistrationFlowStore {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisRegistrationFlowStore(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public void save(RegistrationFlow flow, Duration ttl) {
        try {
            redis.opsForValue().set(key(flow.getFlowId()), mapper.writeValueAsString(flow), ttl);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to persist registration flow", e);
        }
    }

    @Override
    public Optional<RegistrationFlow> find(String flowId) {
        String json = redis.opsForValue().get(key(flowId));
        if (json == null) return Optional.empty();
        try { return Optional.of(mapper.readValue(json, RegistrationFlow.class)); }
        catch (Exception e) { throw new IllegalStateException("Unable to read registration flow", e); }
    }

    @Override public void delete(String flowId) { redis.delete(key(flowId)); }
    private String key(String flowId) { return "auth_flow:" + flowId; }
}
