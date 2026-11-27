import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createSite, deleteSite, fetchSite, fetchSites, updateSite } from '@/api/sites'
import type { SiteRequest } from '@/types/site'

// Shared key: adding, changing or deleting a site invalidates this one list.
export const SITES_KEY = ['sites']

/** All sites of the company; they rarely change, so no polling. */
export function useSites() {
  return useQuery({
    queryKey: SITES_KEY,
    queryFn: fetchSites,
  })
}

/** One site for its own page; stored under the list's key, so a change to any site refreshes it too. */
export function useSite(siteId: string) {
  return useQuery({
    queryKey: [...SITES_KEY, siteId],
    queryFn: () => fetchSite(siteId),
    // 404 means another company's or a deleted site; asking again will not change that
    retry: false,
  })
}

// After any change the site list and the dashboard figures that list sites are stale.
function useRefreshSites() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: SITES_KEY })
    queryClient.invalidateQueries({ queryKey: ['kpis'] })
  }
}

/** Adds a site, then refreshes the lists. */
export function useCreateSite() {
  const refreshSites = useRefreshSites()
  return useMutation({
    mutationFn: createSite,
    onSuccess: refreshSites,
  })
}

/** Changes a site, then refreshes the lists. */
export function useUpdateSite(siteId: string) {
  const refreshSites = useRefreshSites()
  return useMutation({
    mutationFn: (request: SiteRequest) => updateSite(siteId, request),
    onSuccess: refreshSites,
  })
}

/** Deletes a site, then refreshes the lists. */
export function useDeleteSite() {
  const refreshSites = useRefreshSites()
  return useMutation({
    mutationFn: deleteSite,
    onSuccess: refreshSites,
  })
}
