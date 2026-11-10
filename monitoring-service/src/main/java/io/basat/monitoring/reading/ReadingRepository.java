package io.basat.monitoring.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Stores meter readings with plain SQL. Readings are many, small and never updated,
 * so they skip JPA's entity tracking.
 */
@Repository
@RequiredArgsConstructor
public class ReadingRepository {

    private final JdbcClient jdbcClient;

    /** @return {@code true} if stored, {@code false} if this meter already had a reading at that time */
    public boolean save(UUID meterId, Instant measuredAt, BigDecimal energyKwh) {
        int inserted = jdbcClient.sql("""
                        INSERT INTO readings (meter_id, measured_at, energy_kwh)
                        VALUES (:meterId, :measuredAt, :energyKwh)
                        ON CONFLICT (meter_id, measured_at) DO NOTHING
                        """)
                .param("meterId", meterId)
                .param("measuredAt", OffsetDateTime.ofInstant(measuredAt, ZoneOffset.UTC))
                .param("energyKwh", energyKwh)
                .update();
        return inserted == 1;
    }
}