package io.basat.ingestion.reading;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

public record ReadingRequest(
        @Schema(description = "Serial number of the meter that took the reading", example = "EM-2024-001")
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "may contain only letters, digits and hyphens")
        String serialNumber,

        @Schema(description = "End of the measured interval (UTC)", example = "2026-11-04T10:15:00Z")
        @NotNull
        @PastOrPresent
        Instant measuredAt,

        @Schema(description = "Energy consumed during the interval, in kWh", example = "12.75")
        @NotNull
        @PositiveOrZero
        BigDecimal energyKwh
) {
    public Reading toReading(){
            // same canonical form as monitoring-service, so the meter can be matched by serial number
            return new Reading(serialNumber.strip().toUpperCase(Locale.ROOT),measuredAt,energyKwh);
    }
}
