package io.basat.monitoring.error;

import java.util.UUID;

public class MeterNotFoundException extends NotFoundException {
    public MeterNotFoundException(UUID meterId) {
        super("Meter not found: " + meterId);
    }
}
