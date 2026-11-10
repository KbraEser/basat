package io.basat.monitoring.reading;

import io.basat.monitoring.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReadingListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @Value("${app.kafka.readings-topic}")
    private String topic;

    @BeforeEach
    void createMeter() {
        UUID siteId = jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:t, 'Otel', 'HOTEL') RETURNING id")
                .param("t", UUID.randomUUID()).query(UUID.class).single();
        jdbcClient.sql("""
                        INSERT INTO meters (site_id, name, serial_number, meter_type)
                        VALUES (:s, 'Ana Pano', 'EM-TEST-001', 'ELECTRICITY')
                        """)
                .param("s", siteId).update();
    }

    @AfterEach
    void cleanUp() {
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void readingFromKafka_isStored() {
        send("EM-TEST-001", "2026-11-09T10:15:00Z", "0.6944");

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(countReadings()).isEqualTo(1));
    }

    @Test
    void readingForUnknownMeter_isSkippedAndNextReadingIsStillStored() {
        send("EM-UNKNOWN-999", "2026-11-09T10:15:00Z", "1.0");
        send("EM-TEST-001", "2026-11-09T10:15:10Z", "0.6944");

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(countReadings()).isEqualTo(1));
    }

    private void send(String serialNumber, String measuredAt, String energyKwh) {
        // same JSON shape ingestion-service publishes
        String json = """
                {"serialNumber":"%s","measuredAt":"%s","energyKwh":%s}
                """.formatted(serialNumber, measuredAt, energyKwh);
        kafkaTemplate.send(topic, serialNumber, json).join();
    }

    private long countReadings() {
        return jdbcClient.sql("SELECT count(*) FROM readings").query(Long.class).single();
    }
}