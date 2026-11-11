package io.basat.monitoring.config;

import io.basat.monitoring.reading.UnknownMeterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${app.kafka.readings-dlt-topic}")
    private String deadLetterTopic;

    @Bean
    public NewTopic readingsDeadLetterTopic() {
        // same partition count as meter.readings: a failed message keeps its partition number
        return TopicBuilder.name(deadLetterTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(ProducerFactory<Object, Object> producerFactory) {
        DeadLetterPublishingRecoverer deadLetterPublisher = new DeadLetterPublishingRecoverer(
                deadLetterTemplates(producerFactory),
                (rec, ex) -> new TopicPartition(deadLetterTopic, rec.partition()));

        // Spring logs nothing when recovery succeeds, so leave a trace of every dead-lettered message
        ConsumerRecordRecoverer recoverer = (rec, ex) -> {
            log.warn("Sending message from {}-{}@{} to {}: {}",
                    rec.topic(), rec.partition(), rec.offset(), deadLetterTopic, rootMessage(ex));
            deadLetterPublisher.accept(rec, ex);
        };

        // a failing message is tried 3 times in total, 1 second apart, then sent to the dead-letter topic
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
        errorHandler.addNotRetryableExceptions(UnknownMeterException.class);
        return errorHandler;
    }

    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }

    // Unreadable messages reach the recoverer as raw bytes, readable ones as ReadingMessage objects.
    private Map<Class<?>, KafkaOperations<?, ?>> deadLetterTemplates(ProducerFactory<Object, Object> producerFactory) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, new KafkaTemplate<>(producerFactory,
                Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class)));
        templates.put(Object.class, new KafkaTemplate<>(producerFactory,
                Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class,
                        "spring.json.add.type.headers", false)));
        return templates;
    }
}