import { api } from '@/lib/api'
import type { Site, SiteRequest } from '@/types/site'

// The backend lists only the sites of the company in the token, ordered by name.
export const fetchSites = () => api.get<Site[]>('/api/sites')


// 201 with the new site; 409 when the company already has a site with this name.
export const createSite = (request:SiteRequest) => api.post<Site>('/api/sites', request)