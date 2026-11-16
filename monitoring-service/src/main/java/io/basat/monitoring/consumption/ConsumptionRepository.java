package io.basat.monitoring.consumption;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/** Reads consumption totals from the continuous aggregates, never from the raw readings. */
@Repository
@RequiredArgsConstructor
public class ConsumptionRepository {

    private final JdbcClient jdbcClient;

    /**
     * Sums all meters of the company, or of one site when {@code siteId} is not null,
     * per bucket in {@code [from, to)}.
     */
    public List<ConsumptionPoint> findPoints(UUID tenantId, UUID siteId, Resolution resolution, Instant from, Instant to) {
        // The view name comes from the enum, never from the request, so adding it to the SQL is safe.
        String sql = """
                SELECT a.bucket,
                       COALESCE(sum(a.energy_kwh) FILTER (WHERE m.meter_type = 'ELECTRICITY'), 0) AS consumption_kwh,
                       COALESCE(sum(a.energy_kwh) FILTER (WHERE m.meter_type = 'SOLAR'), 0)       AS solar_kwh
                FROM %s a
                JOIN meters m ON m.id = a.meter_id
                JOIN sites s ON s.id = m.site_id
                WHERE s.tenant_id = :tenantId
                  AND a.bucket >= :from AND a.bucket < :to
                  %s
                GROUP BY a.bucket
                ORDER BY a.bucket
                """.formatted(resolution.view(), siteId == null ? "" : "AND s.id = :siteId");

        JdbcClient.StatementSpec statement = jdbcClient.sql(sql)
                .param("tenantId", tenantId)
                .param("from", OffsetDateTime.ofInstant(from, ZoneOffset.UTC))
                .param("to", OffsetDateTime.ofInstant(to, ZoneOffset.UTC));
        if (siteId != null) {
            statement = statement.param("siteId", siteId);
        }
        return statement
                .query((rs, rowNum) -> new ConsumptionPoint(
                        rs.getObject("bucket", OffsetDateTime.class).toInstant(),
                        rs.getBigDecimal("consumption_kwh"),
                        rs.getBigDecimal("solar_kwh")))
                .list();
    }
}