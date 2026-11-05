package io.basat.simulator.profile;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HotelProfileTest {

    private final HotelProfile hotel = new HotelProfile();

    @Test
    void winterNight_usesOnlyTheBaseLoad() {
        assertThat(hotel.loadFactor(LocalDateTime.of(2026, 1, 14, 3, 0))).isEqualTo(0.30);
    }

    @Test
    void breakfastAndEvening_arePeaks() {
        assertThat(hotel.loadFactor(LocalDateTime.of(2026, 1, 14, 8, 0))).isCloseTo(0.45, within(1e-9));
        assertThat(hotel.loadFactor(LocalDateTime.of(2026, 1, 14, 20, 0))).isCloseTo(0.55, within(1e-9));
    }

    @Test
    void summer_usesMoreThanWinter_atTheSameHour() {
        double winterEvening = hotel.loadFactor(LocalDateTime.of(2026, 1, 14, 20, 0));
        double summerEvening = hotel.loadFactor(LocalDateTime.of(2026, 7, 15, 20, 0));
        assertThat(summerEvening).isGreaterThan(winterEvening);
    }

    @Test
    void weekend_isAsBusyAsAWeekday() {
        // 2026-07-15 is a Wednesday, 2026-07-18 a Saturday
        assertThat(hotel.loadFactor(LocalDateTime.of(2026, 7, 18, 20, 0)))
                .isEqualTo(hotel.loadFactor(LocalDateTime.of(2026, 7, 15, 20, 0)));
    }
}
