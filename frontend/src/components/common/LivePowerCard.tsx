import { Activity } from 'lucide-react'
import { Sparkline } from './Sparkline'

interface LivePowerCardProps {
  // Current power in kW, worked out from the latest live readings.
  powerKw: number
  // Recent power values, oldest first, for the line under the number.
  history: number[]
  // Time of the latest reading, already formatted, e.g. "11:22:10".
  updatedAt: string
  // Which sites the figure covers, e.g. "Tüm tesisler".
  scope: string
}

const formatKw = (value: number) => value.toLocaleString('tr-TR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

/** Wide card at the top of the dashboard with the live power, updated over WebSocket. */
export function LivePowerCard({ powerKw, history, updatedAt, scope }: LivePowerCardProps) {
  return (
    <section className="glass-card grid gap-4 rounded-xl p-5 md:grid-cols-[auto_1fr] md:items-end md:gap-10">
      <div className="grid gap-2">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Activity className="size-4 text-tariff-day" aria-hidden="true" />
          Şu anki güç · {scope}
        </div>
        <p className="font-mono text-5xl font-semibold tracking-tight tabular-nums">
          {formatKw(powerKw)}
          <span className="ml-2 font-sans text-xl font-normal text-muted-foreground">kW</span>
        </p>
        <p className="flex items-center gap-2 text-xs text-muted-foreground">
          {/* pulsing dot: data is arriving */}
          <span className="relative flex size-2">
            <span className="absolute inline-flex size-full rounded-full bg-success opacity-75 motion-safe:animate-ping" />
            <span className="relative inline-flex size-2 rounded-full bg-success" />
          </span>
          Canlı · son okuma {updatedAt}
        </p>
      </div>

      {/* static on purpose: a constantly moving chart at the top of the page tires the eyes */}
      <Sparkline values={history} color="var(--tariff-day)" className="h-20" />
    </section>
  )
}
