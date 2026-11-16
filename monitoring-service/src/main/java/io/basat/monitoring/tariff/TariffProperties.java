package io.basat.monitoring.tariff;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/** Unit prices of the three-period tariff in TRY per kWh, read from app.tariff in application.yaml. */
@Validated
@ConfigurationProperties(prefix = "app.tariff")
public record TariffProperties(
        @NotNull @Positive BigDecimal day,
        @NotNull @Positive BigDecimal peak,
        @NotNull @Positive BigDecimal night
) {

    public BigDecimal priceOf(TariffBand band) {
        return switch (band) {
            case DAY -> day;
            case PEAK -> peak;
            case NIGHT -> night;
        };
    }
}
