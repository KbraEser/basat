import { fetchSites } from '@/api/sites'
import { useQuery } from '@tanstack/react-query'


// Shared key: adding or deleting a site later invalidates this one list.
export const SITES_KEY = ['sites']

/** All sites of the company; they rarely change, so no polling. */
export function useSites() {
  return useQuery({
    queryKey: SITES_KEY,
    queryFn: fetchSites,
  })
}