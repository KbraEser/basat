package io.basat.monitoring.reading;

import java.math.BigDecimal;
import java.time.Instant;

/** A reading as ingestion-service publishes it to the {@code meter.readings} topic. */
public record ReadingMessage(String serialNumber, Instant measuredAt, BigDecimal energyKwh) {
}