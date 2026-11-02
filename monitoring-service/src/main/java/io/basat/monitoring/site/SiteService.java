package io.basat.monitoring.site;

import java.util.List;
import java.util.UUID;

/**
 * Manages the sites (buildings) of a company.
 * <p>
 * Every method takes the caller's {@code tenantId} from the access token, so a company can only
 * see and change its own sites; a site of another company is reported as not found.
 */
public interface SiteService {

    /** Returns all sites of the company, ordered by name. */
    List<SiteResponse> getSites(UUID tenantId);

    /**
     * Returns one site of the company.
     *
     * @throws SiteNotFoundException if the site does not exist or belongs to another company
     */
    SiteResponse getSite(UUID tenantId, UUID siteId);

    /**
     * Creates a site for the company.
     *
     * @throws SiteNameAlreadyUsedException if the company already has a site with this name
     */
    SiteResponse createSite(UUID tenantId, SiteRequest request);

    /**
     * Replaces the details of a site.
     *
     * @throws SiteNotFoundException        if the site does not exist or belongs to another company
     * @throws SiteNameAlreadyUsedException if another site of the company already has this name
     */
    SiteResponse updateSite(UUID tenantId, UUID siteId, SiteRequest request);

    /**
     * Deletes a site.
     *
     * @throws SiteNotFoundException if the site does not exist or belongs to another company
     */
    void deleteSite(UUID tenantId, UUID siteId);
}
