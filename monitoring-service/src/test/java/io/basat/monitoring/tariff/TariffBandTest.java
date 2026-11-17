package io.basat.monitoring.tariff;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TariffBandTest {

    @ParameterizedTest
    @CsvSource({
            "00:00, NIGHT",
            "05:59, NIGHT",
            "06:00, DAY",
            "16:59, DAY",
            "17:00, PEAK",
            "21:59, PEAK",
            "22:00, NIGHT",
            "23:59, NIGHT"
    })
    void of_returnsTheBandOfTheLocalTime(LocalTime time, TariffBand expected) {
        assertThat(TariffBand.of(time)).isEqualTo(expected);
    }
}
