package io.basat.monitoring.live;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StompLiveReadingPublisher implements LiveReadingPublisher {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(UUID tenantId, UUID siteId, LiveReading reading) {
        messagingTemplate.convertAndSend(destination(tenantId, siteId), reading);
    }

    static String destination(UUID tenantId, UUID siteId) {
        return sitesOf(tenantId) + siteId;
    }

    // only a single site of the company: wildcards such as "/topic/tenant/*/site/*" are rejected
    static boolean isTopicOf(UUID tenantId, String destination) {
        String prefix = sitesOf(tenantId);
        return destination != null && destination.startsWith(prefix)
                && UUID_PATTERN.matcher(destination.substring(prefix.length())).matches();
    }

    private static String sitesOf(UUID tenantId) {
        return "/topic/tenant/" + tenantId + "/site/";
    }
}