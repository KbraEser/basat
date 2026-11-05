package io.basat.simulator.profile;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A rooftop solar plant in central Türkiye. Unlike the other profiles it describes production:
 * the share of the panels' rated power being generated.
 */
@Component
public class SolarProfile implements ConsumptionProfile {

    private static final double SOLAR_NOON = 12.75;       // local time (UTC+3) when the sun is highest in Ankara
    private static final double MEAN_DAY_LENGTH = 12.0;   // hours of daylight at the equinoxes
    private static final double DAY_LENGTH_SWING = 2.8;   // about 15 h in June, 9 h in December
    private static final double MEAN_PEAK = 0.675;        // panels never reach rated power: heat, dust, inverter
    private static final double PEAK_SWING = 0.175;       // about 0.85 at noon in June, 0.50 in December
    private static final int LONGEST_DAY = 172;           // 21 June

    @Override
    public ProfileType type() {
        return ProfileType.SOLAR;
    }

    @Override
    public double loadFactor(LocalDateTime time) {
        // 1 on the longest day, -1 on the shortest; drives both day length and sun height
        double season = Math.cos(2 * Math.PI * (time.getDayOfYear() - LONGEST_DAY) / 365.0);
        double dayLength = MEAN_DAY_LENGTH + DAY_LENGTH_SWING * season;
        double sunrise = SOLAR_NOON - dayLength / 2;

        double hour = time.getHour() + time.getMinute() / 60.0;
        if (hour <= sunrise || hour >= sunrise + dayLength) {
            return 0;
        }
        // a bell curve over the day: zero at sunrise and sunset, highest at solar noon
        double sunHeight = Math.sin(Math.PI * (hour - sunrise) / dayLength);
        return (MEAN_PEAK + PEAK_SWING * season) * sunHeight;
    }
}