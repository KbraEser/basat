package io.basat.monitoring.tariff;

import java.time.LocalTime;

/** Time periods of the Turkish three-period electricity tariff. */
public enum TariffBand {
    DAY,
    PEAK,
    NIGHT;

    // local time: day 06:00-17:00, peak 17:00-22:00, night 22:00-06:00
    public static TariffBand of(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 6 && hour < 17) {
            return DAY;
        }
        if (hour >= 17 && hour < 22) {
            return PEAK;
        }
        return NIGHT;
    }
}
