package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.AuthAuditEntity;

public interface AuthAuditRepository extends JpaRepository<AuthAuditEntity, Long> {}
