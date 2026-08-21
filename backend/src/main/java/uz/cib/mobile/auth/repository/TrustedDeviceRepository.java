package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.TrustedDeviceEntity;
import java.util.Optional;
import java.util.UUID;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDeviceEntity, UUID> {
    Optional<TrustedDeviceEntity> findByUserIdAndDeviceHash(UUID userId, String deviceHash);
}
