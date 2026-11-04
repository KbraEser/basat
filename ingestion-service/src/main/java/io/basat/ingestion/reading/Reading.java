package io.basat.ingestion.reading;

import java.math.BigDecimal;
import java.time.Instant;

public record Reading(String serialNumber, Instant measuredAt, BigDecimal energyKwh) {
}
