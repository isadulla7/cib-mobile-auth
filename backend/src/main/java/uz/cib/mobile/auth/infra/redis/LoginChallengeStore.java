package uz.cib.mobile.auth.infra.redis;
import java.time.Duration;
import java.util.Optional;

public interface LoginChallengeStore {
    void save(LoginChallenge challenge, Duration ttl);
    Optional<LoginChallenge> find(String challengeId);
    void delete(String challengeId);
}
