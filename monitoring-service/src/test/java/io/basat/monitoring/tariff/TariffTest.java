package io.basat.monitoring.tariff;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TariffTest {

    private static final BigDecimal DAY = new BigDecimal("5.98");
    private static final BigDecimal PEAK = new BigDecimal("8.45");
    private static final BigDecimal NIGHT = new BigDecimal("4.02");

    @Test
    void timeOfUse_usesThePriceOfTheBand() {
        Tariff tariff = new TimeOfUseTariff(DAY, PEAK, NIGHT);

        assertThat(tariff.priceAt(LocalTime.of(9, 0))).isEqualTo(DAY);
        assertThat(tariff.priceAt(LocalTime.of(18, 0))).isEqualTo(PEAK);
        assertThat(tariff.priceAt(LocalTime.of(23, 0))).isEqualTo(NIGHT);
    }

    @Test
    void singleRate_usesTheSamePriceAllDay() {
        Tariff tariff = new SingleRateTariff(DAY);

        assertThat(tariff.priceAt(LocalTime.of(9, 0))).isEqualTo(DAY);
        assertThat(tariff.priceAt(LocalTime.of(18, 0))).isEqualTo(DAY);
        assertThat(tariff.priceAt(LocalTime.of(23, 0))).isEqualTo(DAY);
    }

    @Test
    void timeOfUse_withoutPeakPrice_isRejected() {
        assertThatThrownBy(() -> new TimeOfUseTariff(DAY, null, NIGHT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("app.tariff.peak");
    }
}
