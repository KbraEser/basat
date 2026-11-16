package io.basat.monitoring.consumption;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Energy in one hour or one day, starting at {@code time}.
 * Solar meters measure production, so they are reported apart from consumption.
 */
public record ConsumptionPoint(Instant time, BigDecimal consumptionKwh, BigDecimal solarKwh) {
}