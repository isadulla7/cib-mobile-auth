package uz.cib.mobile.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cib.mobile.auth.domain.UserCredentialEntity;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredentialEntity, UUID> {}
