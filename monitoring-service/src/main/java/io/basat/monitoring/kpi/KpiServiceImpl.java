package io.basat.monitoring.kpi;

import io.basat.monitoring.common.BusinessTime;
import io.basat.monitoring.consumption.ConsumptionPeriod;
import io.basat.monitoring.consumption.ConsumptionPoint;
import io.basat.monitoring.consumption.ConsumptionRepository;
import io.basat.monitoring.consumption.Resolution;
import io.basat.monitoring.site.SiteNotFoundException;
import io.basat.monitoring.site.SiteRepository;
import io.basat.monitoring.tariff.Tariff;
import io.basat.monitoring.tariff.TariffBand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KpiServiceImpl implements KpiService {

    private final SiteRepository siteRepository;
    private final ConsumptionRepository consumptionRepository;
    private final Tariff tariff;
    private final CarbonProperties carbon;
    private final Clock clock;

    @Override
    public KpiResponse getKpis(UUID tenantId, UUID siteId, ConsumptionPeriod period) {
        if (siteId != null && siteRepository.findByIdAndTenantId(siteId, tenantId).isEmpty()) {
            throw new SiteNotFoundException(siteId);
        }
        return summarize(tenantId, siteId, period);
    }

    @Override
    public List<SiteKpi> getSiteKpis(UUID tenantId, ConsumptionPeriod period) {
        return siteRepository.findAllByTenantIdOrderByName(tenantId).stream()
                .map(site -> SiteKpi.of(site, summarize(tenantId, site.getId(), period)))
                .toList();
    }

    // the caller has already checked that the site belongs to the company
    private KpiResponse summarize(UUID tenantId, UUID siteId, ConsumptionPeriod period) {
        Instant now = clock.instant();
        Instant from = period.start(LocalDate.ofInstant(now, BusinessTime.ZONE), BusinessTime.ZONE);

        // always hourly, even for 30 days: the price depends on the hour
        List<ConsumptionPoint> points =
                consumptionRepository.findPoints(tenantId, siteId, Resolution.HOUR, from, now);

        Map<TariffBand, BigDecimal> energyByBand = new EnumMap<>(TariffBand.class);
        Map<TariffBand, BigDecimal> costByBand = new EnumMap<>(TariffBand.class);
        BigDecimal solar = BigDecimal.ZERO;
        for (ConsumptionPoint point : points) {
            LocalTime hour = LocalTime.ofInstant(point.time(), BusinessTime.ZONE);
            TariffBand band = TariffBand.of(hour);
            energyByBand.merge(band, point.consumptionKwh(), BigDecimal::add);
            costByBand.merge(band, point.consumptionKwh().multiply(tariff.priceAt(hour)), BigDecimal::add);
            solar = solar.add(point.solarKwh());
        }

        BigDecimal consumption = sum(energyByBand);
        List<BandUsage> bands = Arrays.stream(TariffBand.values())
                .map(band -> new BandUsage(band,
                        energy(energyByBand.getOrDefault(band, BigDecimal.ZERO)),
                        money(costByBand.getOrDefault(band, BigDecimal.ZERO))))
                .toList();

        return new KpiResponse(period, from, now,
                energy(consumption),
                money(sum(costByBand)),
                energy(consumption.multiply(carbon.gridFactor())),
                energy(solar),
                bands);
    }

    private static BigDecimal sum(Map<TariffBand, BigDecimal> values) {
        return values.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // rounding happens once, at the end, so small errors do not add up
    private static BigDecimal energy(BigDecimal kwh) {
        return kwh.setScale(3, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}