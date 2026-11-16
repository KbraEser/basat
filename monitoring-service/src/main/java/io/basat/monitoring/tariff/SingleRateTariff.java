package io.basat.monitoring.tariff;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/** One price for every hour of the day. */
public record SingleRateTariff(BigDecimal price) implements Tariff {

    public SingleRateTariff {
        Objects.requireNonNull(price, "app.tariff.single-rate is required for a SINGLE_RATE tariff");
    }

    @Override
    public BigDecimal priceAt(LocalTime time){
        return price;
    }
}
