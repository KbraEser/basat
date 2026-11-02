package io.basat.monitoring.site;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SiteRequest(

        @Schema(description = "Site name, unique within the company", example = "Antalya Otel")
        @NotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "Type of building", example = "HOTEL")
        @NotNull
        SiteType type,

        @Schema(description = "Floor area in m², used for consumption per m²", example = "12500.50")
        @Positive
        @Digits(integer = 8, fraction = 2)
        BigDecimal floorAreaM2,

        @Schema(description = "City", example = "Antalya")
        @Size(max = 100)
        String city
) {
}