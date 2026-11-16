package io.basat.monitoring.consumption;

import java.util.UUID;

/** Energy over time for the dashboard charts. */
public interface ConsumptionService {

    /**
     * Returns the energy per hour or day of the given period, up to now.
     *
     * @param siteId one site of the company, or {@code null} for all its sites
     * @throws io.basat.monitoring.site.SiteNotFoundException if the site does not belong to the company
     */
    ConsumptionResponse getConsumption(UUID tenantId, UUID siteId, ConsumptionPeriod period);

}
