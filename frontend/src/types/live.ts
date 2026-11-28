import type { MeterType } from './meter'

// Mirrors LiveReading in monitoring-service: one meter reading pushed over WebSocket the moment it is stored.
export interface LiveReading {
  meterId: string
  serialNumber: string
  meterType: MeterType
  measuredAt: string
  // energy used since the meter's previous reading, not power
  energyKwh: number
}