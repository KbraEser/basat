package io.basat.monitoring.kpi;

import io.basat.monitoring.tariff.TariffBand;

import java.math.BigDecimal;

public record BandUsage(TariffBand band, BigDecimal consumptionKwh, BigDecimal costTry) {
}
