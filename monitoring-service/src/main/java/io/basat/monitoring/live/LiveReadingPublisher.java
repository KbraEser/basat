package io.basat.monitoring.live;

import java.util.UUID;

/** Pushes stored readings to the users watching a site. */
public interface LiveReadingPublisher {

    /**
     * Sends the reading to everyone subscribed to the site's live topic.
     *
     * @param tenantId the company that owns the site
     * @param siteId the site the meter belongs to
     * @param reading the reading that was just stored
     */
    void publish(UUID tenantId, UUID siteId, LiveReading reading);
}
