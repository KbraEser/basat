package io.basat.monitoring.kpi;

import io.basat.monitoring.consumption.ConsumptionPeriod;

import java.util.List;
import java.util.UUID;

/** Summary figures for the dashboard cards. */
public interface KpiService {

    /**
     * Returns total consumption, its cost under the configured tariff, CO2 emissions and solar production
     * of the given period, up to now.
     *
     * @param siteId one site of the company, or {@code null} for all its sites
     * @throws io.basat.monitoring.site.SiteNotFoundException if the site does not belong to the company
     */
    KpiResponse getKpis(UUID tenantId, UUID siteId, ConsumptionPeriod period);


    /**
     * Returns the figures of each site of the company for the given period, ordered by site name.
     * Sites without readings are included with zeros.
     */
    List<SiteKpi> getSiteKpis(UUID tenantId, ConsumptionPeriod period);
}
