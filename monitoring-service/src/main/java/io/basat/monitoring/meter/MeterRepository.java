package io.basat.monitoring.meter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {

    List<Meter> findAllBySiteIdOrderByName(UUID siteId);
    // SiteTenantId walks the relation: meters.site_id -> sites.tenant_id
    Optional<Meter> findByIdAndSiteTenantId(UUID uuid,  UUID tenantId);
    Optional<Meter> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);
    boolean existsBySerialNumberAndIdNot(String serialNumber, UUID id);
    boolean existsBySiteId(UUID siteId);
}
