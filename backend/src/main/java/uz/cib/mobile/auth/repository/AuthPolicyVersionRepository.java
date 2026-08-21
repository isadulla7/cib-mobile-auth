package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.AuthPolicyVersionEntity;
import java.util.Optional;

public interface AuthPolicyVersionRepository extends JpaRepository<AuthPolicyVersionEntity, Long> {
    Optional<AuthPolicyVersionEntity> findFirstByStatusOrderByVersionDesc(String status);
    Optional<AuthPolicyVersionEntity> findByVersion(int version);
}
