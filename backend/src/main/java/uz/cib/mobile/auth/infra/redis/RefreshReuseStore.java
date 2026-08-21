package uz.cib.mobile.auth.infra.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshReuseStore {
    private final StringRedisTemplate redis;
    public RefreshReuseStore(StringRedisTemplate redis) { this.redis = redis; }

    public void markConsumed(String oldRefreshHash, UUID familyId, Duration ttl) {
        redis.opsForValue().set("refresh_reuse:" + oldRefreshHash, familyId.toString(), ttl);
    }

    public Optional<UUID> findFamily(String refreshHash) {
        String value = redis.opsForValue().get("refresh_reuse:" + refreshHash);
        if (value == null) return Optional.empty();
        try { return Optional.of(UUID.fromString(value)); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}
