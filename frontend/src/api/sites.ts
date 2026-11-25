import { api } from '@/lib/api'
import type { Site } from '@/types/site'

// The backend lists only the sites of the company in the token, ordered by name.
export const fetchSites = () => api.get<Site[]>('/api/sites')