package io.basat.monitoring.meter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MeterRequest(
        @Schema(description = "Meter name, e.g. what it measures", example = "Ana Pano")
        @NotBlank
        @Size(max = 200)
        String name,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "may contain only letters, digits and hyphens")
        String serialNumber,

        @Schema(description = "What the meter measures", example = "ELECTRICITY")
        @NotNull
        MeterType type
) {
}
