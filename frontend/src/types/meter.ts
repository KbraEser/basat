// MeterType enum in monitoring-service
export type MeterType = 'ELECTRICITY' | 'SOLAR'

export const meterTypeLabels: Record<MeterType, string> = {
  ELECTRICITY: 'Elektrik',
  SOLAR: 'GES üretimi',
}

// GET /api/sites/{siteId}/meters; MeterResponse in monitoring-service.
export interface Meter {
  id: string
  siteId: string
  name: string
  serialNumber: string
  type: MeterType
  createdAt: string
}
