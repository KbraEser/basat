package io.basat.monitoring.tariff;


import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/** Three-period tariff: the price depends on the day, peak or night band of the hour. */
public record TimeOfUseTariff(BigDecimal day, BigDecimal peak, BigDecimal night) implements Tariff {

    public TimeOfUseTariff{
        Objects.requireNonNull(day,"app.tariff.day is required for a TIME_OF_USE tariff");
        Objects.requireNonNull(peak,"app.tariff.peak is required for a TIME_OF_USE tariff");
        Objects.requireNonNull(night,"app.tariff.night is required for a TIME_OF_USE tariff");
    }

    @Override
    public BigDecimal priceAt(LocalTime time) {
        return switch (TariffBand.of(time)){
            case DAY -> day;
            case PEAK -> peak;
            case NIGHT -> night;
        };
    }
}
