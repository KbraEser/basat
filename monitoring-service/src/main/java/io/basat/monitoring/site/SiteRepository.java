package io.basat.monitoring.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    List<Site> findAllByTenantIdOrderByName(UUID tenantId);

    Optional<Site> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndNameAndIdNot(UUID tenantId, String name, UUID id);
}