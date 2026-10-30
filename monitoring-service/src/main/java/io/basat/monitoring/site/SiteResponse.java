package io.basat.monitoring.site;

import io.basat.monitoring.site.Site;
import io.basat.monitoring.site.SiteType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SiteResponse(
        UUID id,
        String name,
        SiteType type,
        BigDecimal floorAreaM2,
        String city,
        Instant createdAt
) {
    public static SiteResponse from(Site site) {
        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getType(),
                site.getFloorAreaM2(),
                site.getCity(),
                site.getCreatedAt()
        );
    }
}
