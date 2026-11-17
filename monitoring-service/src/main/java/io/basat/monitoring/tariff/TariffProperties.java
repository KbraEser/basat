package io.basat.monitoring.tariff;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/** Tariff settings from app.tariff; only the prices of the chosen type are required. */
@Validated
@ConfigurationProperties(prefix = "app.tariff")
public record TariffProperties(

        @NotNull TariffType type,
        @Positive BigDecimal singleRate,
        @Positive BigDecimal day,
        @Positive BigDecimal peak,
        @Positive BigDecimal night

) {

    public Tariff toTariff(){
        return switch (type){
            case SINGLE_RATE -> new SingleRateTariff(singleRate);
            case TIME_OF_USE -> new TimeOfUseTariff(day, peak, night);
        };
    }
}
