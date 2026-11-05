package io.basat.simulator.profile;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** A hotel: open every day, with breakfast and evening peaks and heavy cooling in summer. */
@Component
public class HotelProfile implements ConsumptionProfile {

    private static final double BASE_LOAD = 0.30;      // corridors, lobby, kitchen fridges, pumps: day and night
    private static final double BREAKFAST_LOAD = 0.15; // kitchen and restaurant, 07:00-10:00
    private static final double EVENING_LOAD = 0.25;   // restaurant, room lighting and TVs, 18:00-23:00
    private static final double MAX_COOLING = 0.35;    // rooms are occupied and cooled all day in summer

    @Override
    public ProfileType type() {
        return ProfileType.HOTEL;
    }

    @Override
    public double loadFactor(LocalDateTime time) {
        int hour = time.getHour();
        double load = BASE_LOAD + MAX_COOLING * CoolingDemand.at(time);
        if (hour >= 7 && hour < 10) {
            load += BREAKFAST_LOAD;
        } else if (hour >= 18 && hour < 23) {
            load += EVENING_LOAD;
        }
        return load;
    }
}