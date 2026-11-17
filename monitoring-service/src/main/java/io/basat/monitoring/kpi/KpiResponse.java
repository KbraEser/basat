package io.basat.monitoring.kpi;

import io.basat.monitoring.consumption.ConsumptionPeriod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record KpiResponse(
        ConsumptionPeriod period,
        Instant from,
        Instant to,
        BigDecimal consumptionKwh,
        BigDecimal costTry,
        BigDecimal co2Kg,
        BigDecimal solarKwh,
        List<BandUsage> bands
) {
}
