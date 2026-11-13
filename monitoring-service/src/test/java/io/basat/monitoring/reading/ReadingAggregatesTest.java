package io.basat.monitoring.reading;

import io.basat.monitoring.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReadingAggregatesTest {

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private UUID meterId;

    @BeforeEach
    void createMeter() {
        UUID siteId = jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:tenantId, 'Otel', 'HOTEL') RETURNING id")
                .param("tenantId", UUID.randomUUID()).query(UUID.class).single();
        meterId = jdbcClient.sql("""
                        INSERT INTO meters (site_id, name, serial_number, meter_type)
                        VALUES (:siteId, 'Ana Pano', 'EM-TEST-001', 'ELECTRICITY') RETURNING id
                        """)
                .param("siteId", siteId).query(UUID.class).single();
    }

    @AfterEach
    void cleanUp() {
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void readingsAreSummedPer15MinutesAndPerHour() {
        save("2026-11-09T10:00:00Z", "1.0");
        save("2026-11-09T10:05:00Z", "2.0");
        save("2026-11-09T10:20:00Z", "4.0");

        assertThat(energyPerBucket("readings_15m")).containsExactly(
                new Bucket(Instant.parse("2026-11-09T10:00:00Z"), new BigDecimal("3.0")),
                new Bucket(Instant.parse("2026-11-09T10:15:00Z"), new BigDecimal("4.0")));
        assertThat(energyPerBucket("readings_hourly")).containsExactly(
                new Bucket(Instant.parse("2026-11-09T10:00:00Z"), new BigDecimal("7.0")));
    }

    @Test
    void dailyTotalsFollowTurkishDaysNotUtcDays() {
        // 20:30 UTC is 23:30 on 9 November in Istanbul, 21:30 UTC is already 00:30 on 10 November
        save("2026-11-09T20:30:00Z", "1.0");
        save("2026-11-09T21:30:00Z", "2.0");

        assertThat(energyPerBucket("readings_daily")).containsExactly(
                new Bucket(Instant.parse("2026-11-08T21:00:00Z"), new BigDecimal("1.0")),
                new Bucket(Instant.parse("2026-11-09T21:00:00Z"), new BigDecimal("2.0")));
    }

    private void save(String measuredAt, String energyKwh) {
        readingRepository.save(meterId, Instant.parse(measuredAt), new BigDecimal(energyKwh));
    }

    private List<Bucket> energyPerBucket(String view) {
        // view names come from the tests above, never from user input
        return jdbcClient.sql("SELECT bucket, energy_kwh FROM " + view + " WHERE meter_id = :meterId ORDER BY bucket")
                .param("meterId", meterId)
                .query((rs, rowNum) -> new Bucket(
                        rs.getObject("bucket", OffsetDateTime.class).toInstant(),
                        rs.getBigDecimal("energy_kwh")))
                .list();
    }

    private record Bucket(Instant start, BigDecimal energyKwh) {
        Bucket {
            energyKwh = energyKwh.stripTrailingZeros();
        }
    }
}
