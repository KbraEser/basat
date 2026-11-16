package io.basat.monitoring.common;

import java.time.ZoneId;

/** Days, tariff hours and bills follow Turkish local time, while readings are stored in UTC. */
public class BusinessTime {
    public static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");

    private BusinessTime() {}
}
