package io.basat.monitoring.live;

import io.basat.monitoring.meter.MeterType;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StompLiveReadingPublisherTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID SITE = UUID.randomUUID();

    @Test
    void publishSendsReadingToTheSiteTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        LiveReading reading = new LiveReading(UUID.randomUUID(), "EM-2024-001", MeterType.ELECTRICITY,
                Instant.parse("2026-11-18T10:00:00Z"), new BigDecimal("1.2500"));

        new StompLiveReadingPublisher(template).publish(TENANT, SITE, reading);

        verify(template).convertAndSend("/topic/tenant/" + TENANT + "/site/" + SITE, reading);
    }

    @Test
    void acceptsASingleSiteOfTheSameTenant() {
        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT,
                StompLiveReadingPublisher.destination(TENANT, SITE))).isTrue();
    }

    @Test
    void rejectsAnotherTenantsTopic() {
        String other = StompLiveReadingPublisher.destination(UUID.randomUUID(), SITE);

        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, other)).isFalse();
    }

    @Test
    void rejectsWildcardsAndMalformedDestinations() {
        String prefix = "/topic/tenant/" + TENANT + "/site/";

        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, prefix + "*")).isFalse();
        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, prefix + "**")).isFalse();
        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, prefix)).isFalse();
        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, prefix + SITE + "/extra")).isFalse();
        assertThat(StompLiveReadingPublisher.isTopicOf(TENANT, null)).isFalse();
    }
}
