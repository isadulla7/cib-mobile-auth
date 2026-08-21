package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByPinfl(String pinfl);
    boolean existsByPassport(String passport);
}
