package io.basat.monitoring.consumption;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** The time windows the dashboard offers. Each one starts at local midnight and runs until now. */
public enum ConsumptionPeriod {
    TODAY(0, Resolution.HOUR),
    LAST_7_DAYS(6, Resolution.HOUR),
    LAST_30_DAYS(29, Resolution.DAY);

    private final int previousDays;
    private final Resolution resolution;

    ConsumptionPeriod(int previousDays, Resolution resolution) {
        this.previousDays = previousDays;
        this.resolution = resolution;
    }

    public Resolution resolution() {
        return resolution;
    }

    public Instant start(LocalDate today, ZoneId zone) {
        return today.minusDays(previousDays).atStartOfDay(zone).toInstant();
    }
}