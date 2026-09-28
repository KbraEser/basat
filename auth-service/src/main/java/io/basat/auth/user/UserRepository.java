package io.basat.auth.user;

import io.basat.auth.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Tenant, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
