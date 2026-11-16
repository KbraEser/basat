package io.basat.monitoring.consumption;

import io.basat.monitoring.common.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumptionPeriodTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    @Test
    void today_startsAtIstanbulMidnight() {
        // midnight in Istanbul (UTC+3) is 21:00 UTC on the day before
        assertThat(ConsumptionPeriod.TODAY.start(TODAY, BusinessTime.ZONE))
                .isEqualTo(Instant.parse("2026-11-09T21:00:00Z"));
    }

    @Test
    void lastSevenDays_includeToday(){
        assertThat(ConsumptionPeriod.LAST_7_DAYS.start(TODAY, BusinessTime.ZONE))
        .isEqualTo(Instant.parse("2026-11-09T21:00:00Z"));
    }

    @Test
    void lastThirtyDays_useDailyPoints() {
        assertThat(ConsumptionPeriod.LAST_30_DAYS.start(TODAY, BusinessTime.ZONE))
                .isEqualTo(Instant.parse("2026-10-11T21:00:00Z"));
        assertThat(ConsumptionPeriod.LAST_30_DAYS.resolution()).isEqualTo(Resolution.DAY);
    }


}
