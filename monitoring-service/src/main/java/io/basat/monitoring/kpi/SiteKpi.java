package io.basat.monitoring.kpi;

import io.basat.monitoring.site.Site;
import io.basat.monitoring.site.SiteType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/** One row of the site comparison table. */
public record SiteKpi(
        UUID siteId,
        String siteName,
        SiteType siteType,
        BigDecimal floorAreaM2,
        BigDecimal consumptionKwh,
        BigDecimal costTry,
        BigDecimal solarKwh,
        BigDecimal consumptionKwhPerM2
) {

    static SiteKpi of(Site site, KpiResponse kpis) {
        return new SiteKpi(
                site.getId(),
                site.getName(),
                site.getType(),
                site.getFloorAreaM2(),
                kpis.consumptionKwh(),
                kpis.costTry(),
                kpis.solarKwh(),
                perSquareMetre(kpis.consumptionKwh(), site.getFloorAreaM2()));
    }

    // without a floor area the intensity is unknown, not zero
    private static BigDecimal perSquareMetre(BigDecimal kwh, BigDecimal floorAreaM2) {
        if (floorAreaM2 == null || floorAreaM2.signum() == 0) {
            return null;
        }
        return kwh.divide(floorAreaM2, 3, RoundingMode.HALF_UP);
    }
}