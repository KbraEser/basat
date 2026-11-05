package io.basat.simulator.profile;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SolarProfileTest {

    private final SolarProfile solar = new SolarProfile();

    @Test
    void night_producesNothing() {
        assertThat(solar.loadFactor(LocalDateTime.of(2026, 6, 21, 2, 0))).isZero();
        assertThat(solar.loadFactor(LocalDateTime.of(2026, 12, 21, 18, 0))).isZero();
    }

    @Test
    void solarNoonOnTheLongestDay_isTheYearlyPeak() {
        assertThat(solar.loadFactor(LocalDateTime.of(2026, 6, 21, 12, 45))).isCloseTo(0.85, within(1e-6));
    }

    @Test
    void winterNoon_producesLessThanSummerNoon() {
        assertThat(solar.loadFactor(LocalDateTime.of(2026, 12, 21, 12, 45))).isCloseTo(0.50, within(0.01));
    }

    @Test
    void morning_producesLessThanNoon() {
        double morning = solar.loadFactor(LocalDateTime.of(2026, 6, 21, 8, 0));
        double noon = solar.loadFactor(LocalDateTime.of(2026, 6, 21, 12, 45));
        assertThat(morning).isPositive().isLessThan(noon);
    }
}
