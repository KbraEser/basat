package io.basat.simulator.reading;

import io.basat.simulator.config.SimulatorProperties.MeterConfig;
import io.basat.simulator.profile.HotelProfile;
import io.basat.simulator.profile.OfficeProfile;
import io.basat.simulator.profile.ProfileType;
import io.basat.simulator.profile.SolarProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadingGeneratorTest {

    private static final Duration QUARTER_HOUR = Duration.ofMinutes(15);

    private final ReadingGenerator generator =
            new ReadingGenerator(List.of(new OfficeProfile(), new HotelProfile(), new SolarProfile()));

    @Test
    void energy_isPowerTimesInterval_withinTheNoise() {
        // 00:00 UTC is 03:00 in Türkiye: a 100 kW office draws its 15 kW base load, 3.75 kWh in 15 minutes
        MeterConfig office = new MeterConfig("EM-2024-002", ProfileType.OFFICE, 100);
        Instant measuredAt = Instant.parse("2026-11-04T00:00:00Z");

        Reading reading = generator.generate(office, measuredAt, QUARTER_HOUR);

        assertThat(reading.serialNumber()).isEqualTo("EM-2024-002");
        assertThat(reading.measuredAt()).isEqualTo(measuredAt);
        assertThat(reading.energyKwh()).isBetween(new BigDecimal("3.5625"), new BigDecimal("3.9375"));
    }

    @Test
    void solarMeter_atNight_reportsExactlyZero() {
        MeterConfig solar = new MeterConfig("GES-2024-001", ProfileType.SOLAR, 50);

        Reading reading = generator.generate(solar, Instant.parse("2026-11-04T20:00:00Z"), QUARTER_HOUR);

        assertThat(reading.energyKwh()).isZero();
    }

    @Test
    void missingProfile_failsAtStartup() {
        assertThatThrownBy(() -> new ReadingGenerator(List.of(new OfficeProfile())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HOTEL");
    }
}
