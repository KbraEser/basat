package io.basat.monitoring.tariff;

import java.math.BigDecimal;
import java.time.LocalTime;

/** Electricity price of a subscription; cost calculations depend only on this. */
public interface Tariff {

    /**
     * Returns the unit price that applies at the given local time.
     *
     * @param time local time in Istanbul
     * @return price in TRY per kWh, excluding taxes
     */
    BigDecimal priceAt(LocalTime time);
}