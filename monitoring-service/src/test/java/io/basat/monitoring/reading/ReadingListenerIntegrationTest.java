package io.basat.monitoring.reading;

import io.basat.monitoring.TestcontainersConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.kafka.KafkaContainer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private KafkaContainer kafka;

    @Value("${app.kafka.readings-topic}")
    private String topic;

    @Value("${app.kafka.readings-dlt-topic}")
    private String deadLetterTopic;

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
        send("EM-TEST-001", readingJson("EM-TEST-001", "2026-11-09T10:15:00Z"));

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(countReadings()).isEqualTo(1));
    }

    @Test
    void sameReadingSentTwice_isStoredOnce() {
        String json = readingJson("EM-TEST-001", "2026-11-09T10:15:00Z");
        send("EM-TEST-001", json);
        send("EM-TEST-001", json);
        // a later reading on the same partition proves both copies were processed
        send("EM-TEST-001", readingJson("EM-TEST-001", "2026-11-09T10:15:10Z"));

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(countReadings()).isEqualTo(2));
    }

    @Test
    void readingForUnknownMeter_goesToDeadLetterTopic() {
        try (Consumer<String, String> deadLetters = deadLetterConsumer()) {
            send("EM-UNKNOWN-999", readingJson("EM-UNKNOWN-999", "2026-11-09T10:15:00Z"));

            ConsumerRecord<String, String> failed = KafkaTestUtils.getSingleRecord(deadLetters, deadLetterTopic, Duration.ofSeconds(15));

            assertThat(failed.key()).isEqualTo("EM-UNKNOWN-999");
            assertThat(failed.value()).contains("\"serialNumber\":\"EM-UNKNOWN-999\"");
            assertThat(header(failed, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN)).isEqualTo(UnknownMeterException.class.getName());
            assertThat(countReadings()).isZero();
        }
    }

    @Test
    void messageThatIsNotValidJson_goesToDeadLetterTopicUnchanged() {
        try (Consumer<String, String> deadLetters = deadLetterConsumer()) {
            send("EM-TEST-001", "this is not json");

            ConsumerRecord<String, String> failed = KafkaTestUtils.getSingleRecord(deadLetters, deadLetterTopic, Duration.ofSeconds(15));

            assertThat(failed.value()).isEqualTo("this is not json");
            assertThat(countReadings()).isZero();
        }
    }

    private Consumer<String, String> deadLetterConsumer() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
        consumer.subscribe(List.of(deadLetterTopic));
        // wait until partitions are assigned, so the consumer starts at the current end of the topic
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            consumer.poll(Duration.ofMillis(100));
            return !consumer.assignment().isEmpty();
        });
        return consumer;
    }

    private static String header(ConsumerRecord<?, ?> rec, String name) {
        return new String(rec.headers().lastHeader(name).value(), StandardCharsets.UTF_8);
    }

    private static String readingJson(String serialNumber, String measuredAt) {
        // same JSON shape ingestion-service publishes
        return """
                {"serialNumber":"%s","measuredAt":"%s","energyKwh":0.6944}
                """.formatted(serialNumber, measuredAt);
    }

    private void send(String key, String json) {
        kafkaTemplate.send(topic, key, json).join();
    }

    private long countReadings() {
        return jdbcClient.sql("SELECT count(*) FROM readings").query(Long.class).single();
    }
}