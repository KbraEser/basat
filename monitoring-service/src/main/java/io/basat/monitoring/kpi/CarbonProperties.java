package io.basat.monitoring.kpi;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/** Emission factor of grid electricity in kg CO2 per kWh, read from app.carbon. */
@Validated
@ConfigurationProperties(prefix = "app.carbon")
public record CarbonProperties(@NotNull @Positive BigDecimal gridFactor) {
}
