package io.basat.monitoring.consumption;

import io.basat.monitoring.common.BusinessTime;
import io.basat.monitoring.site.SiteNotFoundException;
import io.basat.monitoring.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumptionServiceImpl implements ConsumptionService {

    private final SiteRepository siteRepository;
    private final ConsumptionRepository consumptionRepository;
    private final Clock clock;

    @Override
    public ConsumptionResponse getConsumption(UUID tenantId, UUID siteId, ConsumptionPeriod period) {
        if (siteId != null && siteRepository.findByIdAndTenantId(siteId, tenantId).isEmpty()) {
            throw new SiteNotFoundException(siteId);
        }
        Instant now = clock.instant();
        LocalDate today = LocalDate.ofInstant(now, BusinessTime.ZONE);
        Instant from = period.start(today, BusinessTime.ZONE);

        var points = consumptionRepository.findPoints(tenantId, siteId, period.resolution(), from, now);
        return new ConsumptionResponse(period, period.resolution(), from, now, points);
    }
}
