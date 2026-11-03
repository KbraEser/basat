package io.basat.monitoring.meter;

import io.basat.monitoring.error.MeterNotFoundException;
import io.basat.monitoring.error.SerialNumberAlreadyUsedException;
import io.basat.monitoring.site.Site;
import io.basat.monitoring.site.SiteNotFoundException;
import io.basat.monitoring.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final SiteRepository siteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MeterResponse> getMeters(UUID tenantId, UUID siteId) {
        Site site = findSite(tenantId, siteId);
        return
                meterRepository.findAllBySiteIdOrderByName(site.getId()).stream()
                        .map(MeterResponse::from)
                        .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MeterResponse getMeter(UUID tenantId, UUID meterId) {
        return MeterResponse.from(findMeter(tenantId,meterId));
    }

    @Override
    @Transactional
    public MeterResponse createMeter(UUID tenantId, UUID siteId, MeterRequest request) {
        Site site = findSite(tenantId, siteId);
        String serialNumber = normalizeSerialNumber(request.serialNumber());
        if (meterRepository.existsBySerialNumber(serialNumber)) {
            log.warn("Meter creation rejected for site {}: serial number already used", siteId);
            throw new SerialNumberAlreadyUsedException(serialNumber);
        }

        Meter meter = meterRepository.saveAndFlush(
                new Meter(site,request.name().strip(),serialNumber,request.type()));

        log.info("Meter {} created at site {}", meter.getId(), siteId);

        return MeterResponse.from(meter);
    }

    @Override
    @Transactional
    public MeterResponse updateMeter(UUID tenantId, UUID meterId, MeterRequest request) {
        Meter meter = findMeter(tenantId,meterId);

        String serialNumber = normalizeSerialNumber(request.serialNumber());
        if (meterRepository.existsBySerialNumberAndIdNot(serialNumber, meterId)) {
            log.warn("Meter {} update rejected: serial number already used", meterId);
            throw new SerialNumberAlreadyUsedException(serialNumber);
        }

        meter.update(request.name().strip(),serialNumber,request.type());
        log.info("Meter {} updated", meterId);
        return MeterResponse.from(meter);
    }

    @Override
    @Transactional
    public void deleteMeter(UUID tenantId, UUID meterId) {
        meterRepository.delete(findMeter(tenantId,meterId));
        log.info("Meter {} deleted", meterId);
    }

    private Site findSite(UUID tenantId, UUID siteId) {
        return siteRepository.findByIdAndTenantId(siteId, tenantId)
                        .orElseThrow(() -> new SiteNotFoundException(siteId));
    }

    private Meter findMeter(UUID tenantId, UUID meterId) {
        // the tenant check goes through the meter's site
        return meterRepository.findByIdAndSiteTenantId(meterId,tenantId)
                .orElseThrow(() -> new MeterNotFoundException(meterId));
    }

    private static String normalizeSerialNumber(String serialNumber) {
        // "em-2024-00187" and "EM-2024-00187" are the same device
        return serialNumber.strip().toUpperCase(Locale.ROOT);
    }
}
