package io.basat.ingestion.reading;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaReadingPublisherTest {

    private static final Reading READING =
            new Reading("EM-2024-001", Instant.parse("2026-11-06T10:15:00Z"), new BigDecimal("0.6944"));

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Reading> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaReadingPublisher publisher = new KafkaReadingPublisher(kafkaTemplate, "meter.readings");

    @Test
    void publish_sendsToTheTopic_keyedBySerialNumber() {
        when(kafkaTemplate.send("meter.readings", "EM-2024-001", READING))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish(READING);

        verify(kafkaTemplate).send("meter.readings", "EM-2024-001", READING);
    }

    @Test
    void publish_throws_whenKafkaDoesNotStoreTheReading() {
        when(kafkaTemplate.send("meter.readings", "EM-2024-001", READING))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker unavailable")));

        assertThatThrownBy(() -> publisher.publish(READING))
                .isInstanceOf(ReadingNotPublishedException.class)
                .hasMessageContaining("EM-2024-001");
    }

    @Test
    void publish_throws_whenTheBrokerCannotBeReached() {
        when(kafkaTemplate.send("meter.readings", "EM-2024-001", READING))
                .thenThrow(new KafkaException("Send failed"));

        assertThatThrownBy(() -> publisher.publish(READING))
                .isInstanceOf(ReadingNotPublishedException.class);
    }
}