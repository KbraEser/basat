package io.basat.monitoring.site;

import io.basat.monitoring.error.NotFoundException;

import java.util.UUID;

public class SiteNotFoundException extends NotFoundException {
    public SiteNotFoundException(UUID siteId) {
        super("Site not found: " + siteId);
    }
}
