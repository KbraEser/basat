import { Client } from '@stomp/stompjs'
import { useEffect, useState } from 'react'
import type { LiveReading } from '@/types/live'
import { useCurrentUser } from './useCurrentUser'

const HOUR_MS = 60 * 60 * 1000
const HISTORY_SIZE = 30
const STALE_MS = 2 * 60 * 1000

const timeLabel = new Intl.DateTimeFormat('tr-TR', {
  timeZone: 'Europe/Istanbul',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
})

interface PowerPoint {
  time: number
  kw: number
}

interface MeterPower {
  measuredAt: number
  kw: number
}

export interface LivePower {
  powerKw: number | null
  history: number[]
  updatedAt: string | null
  connected: boolean
}

function brokerUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws`
}

/** Current power of the given sites, worked out from the readings pushed over STOMP. */
export function useLivePower(siteIds: string[]): LivePower {
  const tenantId = useCurrentUser().data?.tenantId
  const [history, setHistory] = useState<PowerPoint[]>([])
  const [connected, setConnected] = useState(false)

  const siteKey = siteIds.join(',')

  useEffect(() => {
    if (!tenantId || siteKey === '') return

    const lastReadingAt = new Map<string, number>()
    const meterPower = new Map<string, MeterPower>()
    // false once this connection is being closed: its late messages and close event must not touch the new one's state
    let active = true

    function handle(reading: LiveReading) {
      if (!active) return
      if (reading.meterType === 'SOLAR') return

      const time = Date.parse(reading.measuredAt)
      const previous = lastReadingAt.get(reading.meterId)
      lastReadingAt.set(reading.meterId, time)
      if (previous === undefined || time <= previous) return

      meterPower.set(reading.meterId, { measuredAt: time, kw: reading.energyKwh / ((time - previous) / HOUR_MS) })

      let totalKw = 0
      for (const meter of meterPower.values()) {
        if (time - meter.measuredAt <= STALE_MS) totalKw += meter.kw
      }

      setHistory((points) => {
        const next = points.at(-1)?.time === time ? points.slice(0, -1) : points
        return [...next, { time, kw: totalKw }].slice(-HISTORY_SIZE)
      })
    }

    const client = new Client({
      brokerURL: brokerUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        if (!active) return
        setConnected(true)
        // one subscription per site: the server rejects wildcards and other companies' sites
        for (const siteId of siteKey.split(',')) {
          client.subscribe(`/topic/tenant/${tenantId}/site/${siteId}`, (message) => handle(JSON.parse(message.body)))
        }
      },
      onWebSocketClose: () => {
        if (active) setConnected(false)
      },
    })
    client.activate()

    return () => {
      active = false
      // the next sites start from an empty chart instead of continuing the old one
      setHistory([])
      setConnected(false)
      void client.deactivate()
    }
  }, [tenantId, siteKey])

  const latest = history.at(-1)
  return {
    powerKw: latest ? latest.kw : null,
    history: history.map((point) => point.kw),
    updatedAt: latest ? timeLabel.format(latest.time) : null,
    connected,
  }
}