package io.basat.monitoring.meter;

import java.time.Instant;
import java.util.UUID;

public record MeterResponse(
        UUID id,
        UUID siteId,
        String name,
        String serialNumber,
        MeterType type,
        Instant createdAt
) {
    public static MeterResponse from(Meter meter) {
        return new MeterResponse(
                meter.getId(),
                meter.getSite().getId(),
                meter.getName(),
                meter.getSerialNumber(),
                meter.getType(),
                meter.getCreatedAt()
        );
    }
}
