package io.basat.simulator.reading;

import java.math.BigDecimal;
import java.time.Instant;

/** One reading as the ingestion service expects it in {@code POST /api/readings}. */
public record Reading(String serialNumber, Instant measuredAt, BigDecimal energyKwh) {
}