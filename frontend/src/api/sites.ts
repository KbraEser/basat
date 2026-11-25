import { api } from '@/lib/api'
import type { Site, SiteRequest } from '@/types/site'

// The backend lists only the sites of the company in the token, ordered by name.
export const fetchSites = () => api.get<Site[]>('/api/sites')

// 201 with the new site; 409 when the company already has a site with this name.
export const createSite = (request: SiteRequest) => api.post<Site>('/api/sites', request)

// 200 with the changed site; 409 when another site of the company already has the new name.
export const updateSite = (siteId: string, request: SiteRequest) => api.put<Site>(`/api/sites/${siteId}`, request)

// 204; 409 while the site still has meters (they are never deleted along with it).
export const deleteSite = (siteId: string) => api.delete(`/api/sites/${siteId}`)
