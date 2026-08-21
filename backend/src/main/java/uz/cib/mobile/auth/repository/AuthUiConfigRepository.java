package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.cib.mobile.auth.domain.AuthUiConfigEntity;
import java.util.List;

public interface AuthUiConfigRepository extends JpaRepository<AuthUiConfigEntity, Long> {
    @Query("""
        select c from AuthUiConfigEntity c
        where c.policyVersionId = :policyVersionId and c.enabled = true
        order by c.flowCode asc, c.orderNo asc
        """)
    List<AuthUiConfigEntity> findEnabledByPolicyVersionId(@Param("policyVersionId") Long policyVersionId);
}
