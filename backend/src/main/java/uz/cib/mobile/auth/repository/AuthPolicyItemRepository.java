package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.AuthPolicyItemEntity;
import java.util.List;

public interface AuthPolicyItemRepository extends JpaRepository<AuthPolicyItemEntity, Long> {
    List<AuthPolicyItemEntity> findByPolicyVersionId(Long policyVersionId);
}
