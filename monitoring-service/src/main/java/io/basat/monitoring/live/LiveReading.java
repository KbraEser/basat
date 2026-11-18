package io.basat.monitoring.live;

import io.basat.monitoring.meter.MeterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A reading pushed to the browser the moment it is stored. */
public record LiveReading(UUID meterId, String serialNumber, MeterType meterType, Instant measuredAt, BigDecimal energyKwh) {
}
