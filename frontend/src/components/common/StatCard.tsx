import type { LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface StatItem {
  label: string
  value: React.ReactNode
}

interface StatCardProps {
  icon: LucideIcon
  title: string
  value: React.ReactNode
  unit?: string
  caption?: string
  badge?: React.ReactNode
  stats?: StatItem[]
  className?: string
}

/** Card with an icon, a big number and optional details; the building block of the dashboard. */
export function StatCard({ icon: Icon, title, value, unit, caption, badge, stats, className }: StatCardProps) {
  return (
    // no border: the card stands out from the background by its shadow, and the blur softens the glow behind it
    <section className={cn('flex flex-col gap-4 rounded-xl bg-card p-5 shadow-[0_16px_40px_-12px_rgb(0_0_0/0.75),inset_0_1px_0_rgb(255_255_255/0.07)] backdrop-blur-sm', className)}>
      <header className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex min-w-0 items-center gap-3">
          <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-muted/60">
            <Icon className="size-4 text-muted-foreground" aria-hidden="true" />
          </span>
          <h3 className="truncate text-sm font-medium">{title}</h3>
        </div>
        {badge}
      </header>

      <div>
        <p className="text-3xl font-semibold tracking-tight tabular-nums">
          {value}
          {unit && <span className="ml-1 text-base font-normal text-muted-foreground">{unit}</span>}
        </p>
        {caption && <p className="mt-1 text-xs text-muted-foreground">{caption}</p>}
      </div>

      {stats && stats.length > 0 && (
        <dl className="grid grid-cols-3 gap-4 border-t pt-4">
          {stats.map((stat) => (
            <div key={stat.label} className="grid gap-1">
              <dt className="text-xs text-muted-foreground">{stat.label}</dt>
              <dd className="text-sm font-semibold tabular-nums">{stat.value}</dd>
            </div>
          ))}
        </dl>
      )}
    </section>
  )
}