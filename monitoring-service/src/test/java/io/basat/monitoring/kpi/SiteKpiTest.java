package io.basat.monitoring.kpi;

import io.basat.monitoring.consumption.ConsumptionPeriod;
import io.basat.monitoring.site.Site;
import io.basat.monitoring.site.SiteType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SiteKpiTest {

    private final KpiResponse kpis = new KpiResponse(ConsumptionPeriod.TODAY, Instant.now(), Instant.now(),
            new BigDecimal("500"), new BigDecimal("2990.99"), new BigDecimal("237"), new BigDecimal("40"), List.of());

    @Test
    void consumptionPerSquareMetre_isCalculatedFromTheFloorArea() {
        Site hotel = new Site(UUID.randomUUID(), "Otel", SiteType.HOTEL, new BigDecimal("2000"), "Antalya");

        SiteKpi row = SiteKpi.of(hotel, kpis);

        assertThat(row.siteName()).isEqualTo("Otel");
        assertThat(row.costTry()).isEqualByComparingTo("2990.99");
        assertThat(row.consumptionKwhPerM2()).isEqualByComparingTo("0.25");
    }

    @Test
    void siteWithoutFloorArea_hasNoConsumptionPerSquareMetre() {
        Site office = new Site(UUID.randomUUID(), "Ofis", SiteType.OFFICE, null, "Ankara");

        assertThat(SiteKpi.of(office, kpis).consumptionKwhPerM2()).isNull();
    }
}
