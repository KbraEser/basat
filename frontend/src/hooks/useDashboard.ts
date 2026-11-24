import { fetchConsumption, fetchKpis } from "@/api/dashboard"
import { useAppSelector } from "@/store/hooks"
import { useQuery } from "@tanstack/react-query"

// readings arrive every few seconds; refreshing the summaries once a minute is enough for the cards
const REFRESH_MS = 60 * 1000

/** KPIs for the period and site picked on the dashboard (from the Redux store). */
export function useKpis(){
    const { period, siteId} = useAppSelector((state) => state.dashboard)
    return useQuery({
        // a new key for every period/site: switching back shows the cached answer at once
        queryKey: ['kpis', period, siteId],
        queryFn: () => fetchKpis(period, siteId),
        refetchInterval: REFRESH_MS,
    })
}

/** Hourly (or daily for 30 days) consumption for the same period and site. */
export function useConsumption() {
  const { period, siteId } = useAppSelector((state) => state.dashboard)
  return useQuery({
    queryKey: ['consumption', period, siteId],
    queryFn: () => fetchConsumption(period, siteId),
    refetchInterval: REFRESH_MS,
  })
}

