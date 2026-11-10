package io.basat.monitoring.reading;

import io.basat.monitoring.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReadingRepositoryTest {

    private static final Instant TEN_FIFTEEN = Instant.parse("2026-11-09T10:15:00Z");

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private UUID meterId;

    @BeforeEach
    void createMeter() {
        UUID siteId = jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:t, 'Otel', 'HOTEL') RETURNING id")
                .param("t", UUID.randomUUID()).query(UUID.class).single();
        meterId = jdbcClient.sql("""
                        INSERT INTO meters (site_id, name, serial_number, meter_type)
                        VALUES (:s, 'Ana Pano', 'EM-TEST-001', 'ELECTRICITY') RETURNING id
                        """)
                .param("s", siteId).query(UUID.class).single();
    }

    @AfterEach
    void cleanUp() {
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void save_storesTheReading() {
        boolean stored = readingRepository.save(meterId, TEN_FIFTEEN, new BigDecimal("0.6944"));

        assertThat(stored).isTrue();
        BigDecimal energy = jdbcClient.sql("SELECT energy_kwh FROM readings WHERE meter_id = :m")
                .param("m", meterId).query(BigDecimal.class).single();
        assertThat(energy).isEqualByComparingTo("0.6944");
    }

    @Test
    void save_ignoresASecondReadingForTheSameMeterAndTime() {
        readingRepository.save(meterId, TEN_FIFTEEN, new BigDecimal("0.6944"));

        boolean storedAgain = readingRepository.save(meterId, TEN_FIFTEEN, new BigDecimal("0.6944"));

        assertThat(storedAgain).isFalse();
        assertThat(jdbcClient.sql("SELECT count(*) FROM readings").query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void save_rejectsAReadingForAnUnknownMeter() {
        assertThatThrownBy(() -> readingRepository.save(UUID.randomUUID(), TEN_FIFTEEN, BigDecimal.ONE))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void readingsTable_isATimescaleHypertable() {
        Long hypertables = jdbcClient.sql(
                        "SELECT count(*) FROM timescaledb_information.hypertables WHERE hypertable_name = 'readings'")
                .query(Long.class).single();
        assertThat(hypertables).isEqualTo(1);
    }
}