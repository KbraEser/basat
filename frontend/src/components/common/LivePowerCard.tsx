import { Activity } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sparkline } from './Sparkline'

interface LivePowerCardProps {
  // Current power in kW; null until the first figure can be worked out.
  powerKw: number | null
  // Recent power values, oldest first, for the line under the number.
  history: number[]
  // Time of the latest reading, already formatted, e.g. "11:22:10".
  updatedAt: string | null
  // Whether the WebSocket connection is open.
  connected: boolean
  // Which sites the figure covers, e.g. "Tüm tesisler".
  scope: string
}

const formatKw = (value: number) => value.toLocaleString('tr-TR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

function statusText(connected: boolean, updatedAt: string | null) {
  if (!connected) return 'Bağlantı yok · yeniden deneniyor'
  if (!updatedAt) return 'Bağlandı · ilk okumalar bekleniyor'
  return `Canlı · son okuma ${updatedAt}`
}

/** Wide card with the live power, updated over WebSocket. */
export function LivePowerCard({ powerKw, history, updatedAt, connected, scope }: LivePowerCardProps) {
  const live = connected && updatedAt !== null

  return (
    <section className="glass-card grid gap-4 rounded-xl p-5 md:grid-cols-[auto_1fr] md:items-end md:gap-10">
      <div className="grid gap-2">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Activity className="size-4 text-tariff-day" aria-hidden="true" />
          Şu anki güç · {scope}
        </div>
        <p className="font-mono text-5xl font-semibold tracking-tight tabular-nums">
          {powerKw === null ? '—' : formatKw(powerKw)}
          <span className="ml-2 font-sans text-xl font-normal text-muted-foreground">kW</span>
        </p>
        <p className="flex items-center gap-2 text-xs text-muted-foreground" aria-live="polite">
          {/* pulsing green dot only while data is arriving */}
          <span className="relative flex size-2">
            {live && (
              <span className="absolute inline-flex size-full rounded-full bg-success opacity-75 motion-safe:animate-ping" />
            )}
            <span className={cn('relative inline-flex size-2 rounded-full', live ? 'bg-success' : 'bg-muted-foreground/50')} />
          </span>
          {statusText(connected, updatedAt)}
        </p>
      </div>

      {/* static on purpose: a constantly moving chart at the top of the page tires the eyes */}
      <Sparkline values={history} color="var(--tariff-day)" className="h-20" />
    </section>
  )
}