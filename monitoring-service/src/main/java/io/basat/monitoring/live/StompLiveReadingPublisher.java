package io.basat.monitoring.live;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StompLiveReadingPublisher implements LiveReadingPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(UUID tenantId, UUID siteId, LiveReading reading) {
        messagingTemplate.convertAndSend(destination(tenantId, siteId), reading);
    }

    static String destination(UUID tenantId, UUID siteId) {
        return "/topic/tenant/" + tenantId + "/site/" + siteId;
    }
}
