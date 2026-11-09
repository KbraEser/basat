package io.basat.ingestion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.spi.ToolProvider;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic readingsTopic(@Value("${app.kafka.readings-topic}") String name){
        // 3 partitions: up to 3 consumers of one group can read in parallel; 1 replica: single local broker
        return
                TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
