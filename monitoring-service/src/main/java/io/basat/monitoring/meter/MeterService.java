package io.basat.monitoring.meter;

import io.basat.monitoring.error.MeterNotFoundException;
import io.basat.monitoring.error.SerialNumberAlreadyUsedException;
import io.basat.monitoring.site.SiteNotFoundException;
import java.util.List;
import java.util.UUID;

public interface MeterService {

    /**
     * Returns the meters of a site, ordered by name.
     *
     * @throws SiteNotFoundException if the site does not exist or belongs to another company
     */
    List<MeterResponse> getMeters(UUID tenantId, UUID siteId);

    /**
     * Returns one meter of the company.
     *
     * @throws MeterNotFoundException if the meter does not exist or belongs to another company
     */
    MeterResponse getMeter(UUID tenantId, UUID meterId);


    /**
     * Adds a meter to a site of the company.
     *
     * @throws SiteNotFoundException            if the site does not exist or belongs to another company
     * @throws SerialNumberAlreadyUsedException if a meter with this serial number already exists
     */
    MeterResponse createMeter(UUID tenantId, UUID siteId, MeterRequest request);


    /**
     * Replaces the details of a meter; the meter stays at its site.
     *
     * @throws MeterNotFoundException           if the meter does not exist or belongs to another company
     * @throws SerialNumberAlreadyUsedException if another meter already has this serial number
     */
    MeterResponse updateMeter(UUID tenantId, UUID meterId, MeterRequest request);

    /**
     * Deletes a meter.
     *
     * @throws MeterNotFoundException if the meter does not exist or belongs to another company
     */
    void deleteMeter(UUID tenantId, UUID meterId);
}
