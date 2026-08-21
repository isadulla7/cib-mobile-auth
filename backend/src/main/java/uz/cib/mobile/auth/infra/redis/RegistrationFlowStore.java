package uz.cib.mobile.auth.infra.redis;
import java.time.Duration;
import java.util.Optional;

public interface RegistrationFlowStore {
    void save(RegistrationFlow flow, Duration ttl);
    Optional<RegistrationFlow> find(String flowId);
    void delete(String flowId);
}
