package io.basat.simulator.profile;

import java.time.LocalDateTime;

final class CoolingDemand {
    private CoolingDemand() {}

    static double at(LocalDateTime time){
        return seasonal(time.getMonthValue())* daily(time.getHour());
    }

    private static double seasonal(int month){
        return switch (month){
            case 6, 7, 8, 9 ->1.0;   // summer: cooling runs every day
            case 5, 10 -> 0.4;       // shoulder months: only on warm afternoons
            default -> 0.0;          // heating season; heating is mostly gas, not electricity
        };
    }

    private static double daily(int hour){
        // follows the outdoor temperature: zero at 06:00, highest at 15:00, back to zero at midnight
        return Math.max(0, Math.sin(Math.PI * (hour - 6) / 18.0));
    }
}
