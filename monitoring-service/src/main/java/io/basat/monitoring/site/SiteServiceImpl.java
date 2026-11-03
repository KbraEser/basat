package io.basat.monitoring.site;

import io.basat.monitoring.meter.MeterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final MeterRepository meterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> getSites(UUID tenantId) {
        return siteRepository.findAllByTenantIdOrderByName(tenantId).stream().map(SiteResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse getSite(UUID tenantId, UUID siteId) {
        return SiteResponse.from(findSite(tenantId, siteId));
    }


    @Override
    @Transactional
    public SiteResponse createSite(UUID tenantId, SiteRequest request) {
        String name = request.name().strip();
        if(siteRepository.existsByTenantIdAndName(tenantId, name)) {
            log.warn("Site creation rejected for tenant {}: name already used", tenantId);            throw new SiteNameAlreadyUsedException(name);
        }

        Site site = siteRepository.saveAndFlush(new Site(tenantId,name,request.type(),request.floorAreaM2(), request.city()));

        log.info("Site {} created for tenant {}", site.getId(), tenantId);

        return SiteResponse.from(site);
    }

    @Override
    @Transactional
    public SiteResponse updateSite(UUID tenantId, UUID siteId, SiteRequest request) {
        Site site = findSite(tenantId, siteId);
        String name = request.name().strip();

        if(siteRepository.existsByTenantIdAndNameAndIdNot(tenantId, name, siteId)) {
            log.warn("Site {} update rejected: name already used", siteId);            throw new SiteNameAlreadyUsedException(name);
        }
        // no save() needed: the entity is managed, so Hibernate writes the changes when the transaction commits

        site.update(name, request.type(), request.floorAreaM2(), request.city());
        log.info("Site {} updated", siteId);
        return SiteResponse.from(site);
    }

    @Override
    @Transactional
    public void deleteSite(UUID tenantId, UUID siteId) {
        Site site = findSite(tenantId, siteId);
        // meters keep the site's readings; deleting them silently would lose consumption history
        if (meterRepository.existsBySiteId(siteId)) {
            log.warn("Site {} deletion rejected: it still has meters", siteId);
            throw new SiteHasMetersException(siteId);
        }
        siteRepository.delete(site);
        log.info("Site {} deleted", siteId);
    }



    private Site findSite(UUID tenantId, UUID siteId) {
        // filtering by tenantId as well means another company's site id behaves as if it did not exist

        return siteRepository.findByIdAndTenantId(siteId,tenantId).orElseThrow(()->new SiteNotFoundException(siteId));
    }
}
