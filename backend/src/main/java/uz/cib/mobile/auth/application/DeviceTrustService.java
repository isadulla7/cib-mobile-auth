package uz.cib.mobile.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.cib.mobile.auth.domain.TrustedDeviceEntity;
import uz.cib.mobile.auth.engine.AuthPolicy;
import uz.cib.mobile.auth.repository.TrustedDeviceRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class DeviceTrustService {
    private final TrustedDeviceRepository repository;

    public DeviceTrustService(TrustedDeviceRepository repository) { this.repository = repository; }

    public boolean isTrusted(UUID userId, String deviceHash) {
        return repository.findByUserIdAndDeviceHash(userId, deviceHash)
                .filter(d -> d.getRevokedAt() == null)
                .filter(d -> d.getTrustUntil().isAfter(Instant.now()))
                .isPresent();
    }

    @Transactional
    public void trust(UUID userId, String deviceHash, AuthPolicy policy) {
        Instant until = Instant.now().plus(policy.trustedDeviceDays(), ChronoUnit.DAYS);
        repository.findByUserIdAndDeviceHash(userId, deviceHash)
                .ifPresentOrElse(
                        existing -> existing.renew(until),
                        () -> repository.save(TrustedDeviceEntity.create(userId, deviceHash, until))
                );
    }
}
