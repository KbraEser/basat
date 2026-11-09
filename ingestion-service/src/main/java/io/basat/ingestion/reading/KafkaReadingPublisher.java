package io.basat.ingestion.reading;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaReadingPublisher implements ReadingPublisher{

    private final KafkaTemplate<String, Reading> kafkaTemplate;
    private final String topic;

    public KafkaReadingPublisher(KafkaTemplate<String, Reading> kafkaTemplate, @Value("${app.kafka.readings-topic}")String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(Reading reading) {
        // the key sends every reading of one meter to the same partition, so they stay in order
        // join(): answer 202 only after Kafka has stored the reading
        kafkaTemplate.send(topic, reading.serialNumber(), reading).join();
        log.debug("Reading of meter {} published", reading.serialNumber());
    }

}
