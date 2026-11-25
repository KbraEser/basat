import { createSite, fetchSites } from '@/api/sites'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'


// Shared key: adding or deleting a site later invalidates this one list.
export const SITES_KEY = ['sites']

/** All sites of the company; they rarely change, so no polling. */
export function useSites() {
  return useQuery({
    queryKey: SITES_KEY,
    queryFn: fetchSites,
  })
}

/** Adds a site, then refreshes the site list and the dashboard figures that list sites. */
export function useCreateSite() {
  const queryClient = useQueryClient()
  return useMutation({ 
    mutationFn: createSite,
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: SITES_KEY})
      queryClient.invalidateQueries({queryKey: ['kpis']})
    }
    
  })
  
}
