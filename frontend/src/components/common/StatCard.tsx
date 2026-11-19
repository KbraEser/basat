import type { LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface StatItem {
  label: string
  value: React.ReactNode
}

// Category colour of the card; class names are written out so Tailwind keeps them.
export type StatTone = 'consumption' | 'cost' | 'carbon' | 'solar' | 'neutral'

const tones: Record<StatTone, string> = {
  consumption: 'bg-consumption/15 text-consumption',
  cost: 'bg-tariff-day/15 text-tariff-day',
  carbon: 'bg-success/15 text-success',
  solar: 'bg-solar/15 text-solar',
  neutral: 'bg-muted/60 text-muted-foreground',
}

interface StatCardProps {
  icon: LucideIcon
  title: string
  value: React.ReactNode
  // Unit shown smaller next to the value, e.g. "kWh".
  unit?: string
  caption?: string
  // Top-right corner, usually a StatusPill.
  badge?: React.ReactNode
  // Small figures in a row at the bottom.
  stats?: StatItem[]
  // Category colour of the icon.
  tone?: StatTone
  // Extra content at the bottom, e.g. a Sparkline or a TariffBar.
  children?: React.ReactNode
  className?: string
}

/** Card with a coloured icon, a big meter-style number and optional details; the building block of the dashboard. */
export function StatCard({ icon: Icon, title, value, unit, caption, badge, stats, tone = 'neutral', children, className }: StatCardProps) {
  return (
    <section className={cn('glass-card flex flex-col gap-4 rounded-xl p-5', className)}>
      <header className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex min-w-0 items-center gap-3">
          <span className={cn('flex size-8 shrink-0 items-center justify-center rounded-lg', tones[tone])}>
            <Icon className="size-4" aria-hidden="true" />
          </span>
          <h3 className="truncate text-sm font-medium">{title}</h3>
        </div>
        {badge}
      </header>

      <div>
        <p className="font-mono text-3xl font-semibold tracking-tight tabular-nums">
          {value}
          {unit && <span className="ml-1.5 font-sans text-base font-normal text-muted-foreground">{unit}</span>}
        </p>
        {caption && <p className="mt-1 text-xs text-muted-foreground">{caption}</p>}
      </div>

      {children}

      {stats && stats.length > 0 && (
        <dl className="grid grid-cols-3 gap-4 border-t pt-4">
          {stats.map((stat) => (
            <div key={stat.label} className="grid gap-1">
              <dt className="text-xs text-muted-foreground">{stat.label}</dt>
              <dd className="font-mono text-sm font-semibold tabular-nums">{stat.value}</dd>
            </div>
          ))}
        </dl>
      )}
    </section>
  )
}
