import { api } from '@/lib/api'
import type { ConsumptionPeriod, ConsumptionResponse, KpiResponse } from '@/types/dashboard'

// Without a site the backend sums all sites of the company.
function query(period: ConsumptionPeriod, siteId: string | null) {
  const params = new URLSearchParams({ period })
  if (siteId) params.set('siteId', siteId)
  return params.toString()
}

export const fetchKpis = (period: ConsumptionPeriod, siteId: string | null) =>
  api.get<KpiResponse>(`/api/kpis?${query(period, siteId)}`)

export const fetchConsumption = (period: ConsumptionPeriod, siteId: string | null) =>
  api.get<ConsumptionResponse>(`/api/consumption?${query(period, siteId)}`)