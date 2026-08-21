package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.AuthSessionEntity;
import uz.cib.mobile.auth.domain.SessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {
    Optional<AuthSessionEntity> findByRefreshTokenHashAndStatus(String refreshTokenHash, SessionStatus status);
    List<AuthSessionEntity> findByTokenFamilyIdAndStatus(UUID tokenFamilyId, SessionStatus status);
}
