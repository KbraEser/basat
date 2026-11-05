package io.basat.simulator.profile;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/** An office building: busy on weekday working hours, almost empty at night and at weekends. */
@Component
public class OfficeProfile implements ConsumptionProfile {

    private static final double BASE_LOAD = 0.15;      // servers, fridges, emergency lighting: never off
    private static final double OCCUPIED_LOAD = 0.45;  // lighting, computers, ventilation while people are in
    private static final double MAX_COOLING = 0.30;    // chillers on a hot summer afternoon

    @Override
    public ProfileType type() {
        return ProfileType.OFFICE;
    }

    @Override
    public double loadFactor(LocalDateTime time) {
        if (!isWorkingHours(time)) {
            return BASE_LOAD;
        }
        return BASE_LOAD + OCCUPIED_LOAD + MAX_COOLING * CoolingDemand.at(time);
    }

    private static boolean isWorkingHours(LocalDateTime time) {
        DayOfWeek day = time.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        return weekday && time.getHour() >= 8 && time.getHour() < 18;
    }
}