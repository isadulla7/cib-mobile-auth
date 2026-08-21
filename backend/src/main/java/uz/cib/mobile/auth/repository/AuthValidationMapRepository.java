package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.cib.mobile.auth.domain.AuthValidationMapEntity;
import java.util.List;

public interface AuthValidationMapRepository extends JpaRepository<AuthValidationMapEntity, Long> {
    @Query("""
        select v from AuthValidationMapEntity v
        where v.policyVersionId = :policyVersionId
          and v.domainCode = :domainCode
          and v.stepCode = :stepCode
          and v.enabled = true
        order by v.orderNo asc
        """)
    List<AuthValidationMapEntity> findForStep(
            @Param("policyVersionId") Long policyVersionId,
            @Param("domainCode") String domainCode,
            @Param("stepCode") String stepCode);
}
