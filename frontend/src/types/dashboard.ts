export type ConsumptionPeriod = 'TODAY' | 'LAST_7_DAYS' | 'LAST_30_DAYS'


// TariffBand in monitoring-service: day 06–17, peak 17–22, night 22–06
export type TariffBand = 'DAY' | 'PEAK' | 'NIGHT'

// GET /api/kpis; amounts are BigDecimal in Java and plain numbers in JSON
export interface BandUsage {
  band: TariffBand
  consumptionKwh: number
  costTry: number
}

export interface KpiResponse {
  period: ConsumptionPeriod
  from: string
  to: string
  consumptionKwh: number
  costTry: number
  co2Kg: number
  solarKwh: number
  bands: BandUsage[]
}

// GET /api/consumption
export interface ConsumptionPoint {
  time: string
  consumptionKwh: number
  solarKwh: number
}

export interface ConsumptionResponse {
  period: ConsumptionPeriod
  resolution: 'HOUR' | 'DAY'
  from: string
  to: string
  points: ConsumptionPoint[]
}