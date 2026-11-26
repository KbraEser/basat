import { useQuery } from '@tanstack/react-query'
import { fetchConsumption, fetchKpis, fetchSiteKpis } from '@/api/dashboard'
import { useAppSelector } from '@/store/hooks'

// readings arrive every few seconds; refreshing the summaries once a minute is enough for the cards
const REFRESH_MS = 60 * 1000

// The period always comes from the store; a site page passes its own site, the overview uses the store's.
function useFilters(siteIdFromPage?: string) {
  const { period, siteId } = useAppSelector((state) => state.dashboard)
  return { period, siteId: siteIdFromPage ?? siteId }
}

/** KPIs for the selected period, for all sites or for the given site. */
export function useKpis(siteIdFromPage?: string) {
  const { period, siteId } = useFilters(siteIdFromPage)
  return useQuery({
    // a new key for every period/site: switching back shows the cached answer at once
    queryKey: ['kpis', period, siteId],
    queryFn: () => fetchKpis(period, siteId),
    refetchInterval: REFRESH_MS,
  })
}

/** Hourly (or daily for 30 days) consumption for the same period and site. */
export function useConsumption(siteIdFromPage?: string) {
  const { period, siteId } = useFilters(siteIdFromPage)
  return useQuery({
    queryKey: ['consumption', period, siteId],
    queryFn: () => fetchConsumption(period, siteId),
    refetchInterval: REFRESH_MS,
  })
}

/** One row per site for the comparison table; follows the period only, never a single site. */
export function useSiteKpis() {
  const period = useAppSelector((state) => state.dashboard.period)
  return useQuery({
    queryKey: ['kpis', 'sites', period],
    queryFn: () => fetchSiteKpis(period),
    refetchInterval: REFRESH_MS,
  })
}