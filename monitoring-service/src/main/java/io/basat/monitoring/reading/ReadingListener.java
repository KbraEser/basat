package io.basat.monitoring.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka entry point: turns each message on the readings topic into a call to {@link ReadingService}. */

@Component
@RequiredArgsConstructor
public class ReadingListener {
    private final ReadingService readingService;

    @KafkaListener(topics="${app.kafka.readings-topic}")
    public void onReading(ReadingMessage message) {
        readingService.store(message);
    }

}
