package io.basat.monitoring.consumption;

import java.time.Instant;
import java.util.List;

public record ConsumptionResponse(
        ConsumptionPeriod period,
        Resolution resolution,
        Instant from,
        Instant to,
        List<ConsumptionPoint> points
) {
}