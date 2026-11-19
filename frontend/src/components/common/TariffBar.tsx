import { cn } from '@/lib/utils'

export type TariffBand = 'DAY' | 'PEAK' | 'NIGHT'

export interface TariffShare {
  band: TariffBand
  costTry: number
}

// Fixed order and colours of Türkiye's three-period tariff; class names are written out so Tailwind keeps them.
const bands: Record<TariffBand, { label: string; hours: string; color: string }> = {
  DAY: { label: 'Gündüz', hours: '06–17', color: 'bg-tariff-day' },
  PEAK: { label: 'Puant', hours: '17–22', color: 'bg-tariff-peak' },
  NIGHT: { label: 'Gece', hours: '22–06', color: 'bg-tariff-night' },
}

const order: TariffBand[] = ['DAY', 'PEAK', 'NIGHT']

const formatTry = (value: number) => value.toLocaleString('tr-TR', { maximumFractionDigits: 0 })

interface TariffBarProps {
  shares: TariffShare[]
  className?: string
}

/** Splits a cost into day, peak and night with one three-coloured bar and a legend. */
export function TariffBar({ shares, className }: TariffBarProps) {
  const total = shares.reduce((sum, share) => sum + share.costTry, 0)
  const costOf = (band: TariffBand) => shares.find((share) => share.band === band)?.costTry ?? 0
  const percentOf = (band: TariffBand) => (total > 0 ? (costOf(band) / total) * 100 : 0)

  return (
    <div className={cn('grid gap-3', className)}>
      <div className="flex h-2 overflow-hidden rounded-full bg-muted/60" role="img" aria-label="Maliyetin tarife dilimlerine dağılımı">
        {order.map((band) => (
          <div key={band} className={bands[band].color} style={{ width: `${percentOf(band)}%` }} />
        ))}
      </div>
      <dl className="grid grid-cols-3 gap-3">
        {order.map((band) => (
          <div key={band} className="grid gap-0.5">
            <dt className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <span className={cn('size-2 rounded-full', bands[band].color)} aria-hidden="true" />
              {bands[band].label}
            </dt>
            <dd className="font-mono text-sm font-semibold tabular-nums">
              {formatTry(costOf(band))} ₺
              <span className="ml-1 text-xs font-normal text-muted-foreground">%{percentOf(band).toFixed(0)}</span>
            </dd>
          </div>
        ))}
      </dl>
    </div>
  )
}
