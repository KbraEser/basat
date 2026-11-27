import { api } from '@/lib/api'
import type { Meter } from '@/types/meter'

// 404 when the site does not exist or belongs to another company.
export const fetchMeters = (siteId: string) => api.get<Meter[]>(`/api/sites/${siteId}/meters`)
