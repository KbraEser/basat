package io.basat.simulator.reading;

import io.basat.simulator.config.SimulatorProperties;
import io.basat.simulator.profile.ConsumptionProfile;
import io.basat.simulator.profile.ProfileType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Turns a meter's profile into the energy it used during one interval. */
@Component
public class ReadingGenerator {

    private static final ZoneId TURKIYE = ZoneId.of("Europe/Istanbul"); // profiles are written in local time
    private static final double NOISE = 0.05;                           // ±5%, so the curves do not look drawn

    private final Map<ProfileType, ConsumptionProfile> profiles = new EnumMap<>(ProfileType.class);


    public ReadingGenerator(List<ConsumptionProfile> profiles) {
        profiles.forEach(profile -> this.profiles.put(profile.type(), profile));
        // fail at startup rather than on the first reading of a meter with a missing profile
        for (ProfileType type : ProfileType.values()) {
            if (!this.profiles.containsKey(type)) {
                throw new IllegalStateException("No consumption profile for " + type);
            }
        }
    }



    public Reading generate(SimulatorProperties.MeterConfig meter, Instant measuredAt, Duration interval) {
        LocalDateTime localTime = LocalDateTime.ofInstant(measuredAt,TURKIYE);

        double powerKw = meter.installedKw() * profiles.get(meter.profile()).loadFactor(localTime);

        double noise = 1 + ThreadLocalRandom.current().nextDouble(-NOISE, NOISE);

        double hours = interval.toMillis() / 3_600_000.0;

        BigDecimal energyKwh = BigDecimal.valueOf(powerKw * noise * hours).setScale(4, RoundingMode.HALF_UP);

        return new Reading(meter.serialNumber(), measuredAt, energyKwh);

    }
}
