import { useQuery } from '@tanstack/react-query'
import { fetchMeters } from '@/api/meters'

/** Meters of one site; the site id is part of the key, so each site has its own list. */
export function useMeters(siteId: string) {
  return useQuery({
    queryKey: ['meters', siteId],
    queryFn: () => fetchMeters(siteId),
  })
}
