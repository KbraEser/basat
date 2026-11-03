package io.basat.monitoring.site;

import java.util.UUID;

public class SiteHasMetersException extends RuntimeException {
    public SiteHasMetersException(UUID siteId) {
        super("Site " + siteId + " still has meters; delete or move them first");
    }
}
