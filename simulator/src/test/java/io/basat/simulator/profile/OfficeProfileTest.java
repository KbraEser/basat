package io.basat.simulator.profile;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OfficeProfileTest {

    private final OfficeProfile office = new OfficeProfile();

    // 2026-11-04 is a Wednesday, 2026-11-07 a Saturday, 2026-07-15 a Wednesday
    @Test
    void nightOnAWeekday_usesOnlyTheBaseLoad() {
        assertThat(office.loadFactor(LocalDateTime.of(2026, 11, 4, 3, 0))).isEqualTo(0.15);
    }

    @Test
    void workingHoursInAutumn_addTheOccupiedLoad_withoutCooling() {
        assertThat(office.loadFactor(LocalDateTime.of(2026, 11, 4, 10, 0))).isCloseTo(0.60, within(1e-9));
    }

    @Test
    void summerAfternoon_isThePeak_becauseOfCooling() {
        assertThat(office.loadFactor(LocalDateTime.of(2026, 7, 15, 15, 0))).isCloseTo(0.90, within(1e-9));
    }

    @Test
    void weekend_staysAtTheBaseLoad_evenOnASummerAfternoon() {
        assertThat(office.loadFactor(LocalDateTime.of(2026, 11, 7, 15, 0))).isEqualTo(0.15);
        assertThat(office.loadFactor(LocalDateTime.of(2026, 7, 18, 15, 0))).isEqualTo(0.15);
    }
}