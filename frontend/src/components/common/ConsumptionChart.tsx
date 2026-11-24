import { Area, AreaChart, CartesianGrid, ReferenceArea, XAxis, YAxis } from 'recharts'
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from '@/components/ui/chart'
import { cn } from '@/lib/utils'
import type { ConsumptionPoint, TariffBand } from '@/types/dashboard'

// Series names and colours; ChartContainer turns them into --color-consumptionKwh and --color-solarKwh.
const chartConfig = {
  consumptionKwh: { label: 'Tüketim', color: 'var(--consumption)' },
  solarKwh: { label: 'GES üretimi', color: 'var(--solar)' },
} satisfies ChartConfig

const HOUR_MS = 60 * 60 * 1000

// Same hours as TariffBand in monitoring-service, read in Istanbul time like the backend.
const hourFormat = new Intl.DateTimeFormat('en-GB', { hour: 'numeric', hourCycle: 'h23', timeZone: 'Europe/Istanbul' })

function bandOf(time: number): TariffBand {
  const hour = Number(hourFormat.format(time))
  if (hour >= 6 && hour < 17) return 'DAY'
  if (hour >= 17 && hour < 22) return 'PEAK'
  return 'NIGHT'
}

const bandColors: Record<TariffBand, string> = {
  DAY: 'var(--tariff-day)',
  PEAK: 'var(--tariff-peak)',
  NIGHT: 'var(--tariff-night)',
}

interface BandRange {
  band: TariffBand
  from: number
  to: number
}

// Joins neighbouring hours of the same band: 06:00–17:00 becomes one stripe instead of eleven.
function bandRanges(times: number[]): BandRange[] {
  const ranges: BandRange[] = []
  for (const time of times) {
    const band = bandOf(time)
    const last = ranges.at(-1)
    if (last && last.band === band && last.to === time) {
      last.to = time + HOUR_MS
    } else {
      ranges.push({ band, from: time, to: time + HOUR_MS })
    }
  }
  return ranges
}

const zone = { timeZone: 'Europe/Istanbul' } as const
const timeLabel = new Intl.DateTimeFormat('tr-TR', { ...zone, hour: '2-digit', minute: '2-digit' })
const dayLabel = new Intl.DateTimeFormat('tr-TR', { ...zone, day: 'numeric', month: 'short' })
const fullLabel = new Intl.DateTimeFormat('tr-TR', { ...zone, day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })

interface ConsumptionChartProps {
  points: ConsumptionPoint[]
  // HOUR for today and the last 7 days, DAY for the last 30 days.
  resolution: 'HOUR' | 'DAY'
  className?: string
}

/** Consumption and solar output over time; in hourly view the background shows the day, peak and night tariff hours. */
export function ConsumptionChart({ points, resolution, className }: ConsumptionChartProps) {
  if (points.length === 0) {
    return (
      <p className={cn('flex h-72 items-center justify-center text-sm text-muted-foreground', className)}>
        Bu dönem için henüz okuma yok.
      </p>
    )
  }

  // Recharts needs a number on a time axis; the ISO text becomes milliseconds.
  const data = points.map((point) => ({ ...point, time: Date.parse(point.time) }))
  const hourly = resolution === 'HOUR'
  const spansDays = data.at(-1)!.time - data[0].time >= 24 * HOUR_MS
  const showSolar = points.some((point) => point.solarKwh > 0)

  const tickLabel = (time: number) => (hourly && !spansDays ? timeLabel : dayLabel).format(time)
  const tooltipLabel = (time: number) => (hourly ? fullLabel : dayLabel).format(time)

  return (
    <ChartContainer config={chartConfig} className={cn('h-72 w-full', className)}>
      <AreaChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        {hourly &&
          bandRanges(data.map((point) => point.time)).map((range) => (
            <ReferenceArea
              key={range.from}
              x1={range.from}
              x2={range.to}
              fill={bandColors[range.band]}
              fillOpacity={0.08}
              ifOverflow="hidden"
            />
          ))}
        <CartesianGrid vertical={false} strokeOpacity={0.15} />
        <XAxis
          dataKey="time"
          type="number"
          scale="time"
          domain={['dataMin', 'dataMax']}
          tickFormatter={tickLabel}
          tickLine={false}
          axisLine={false}
          minTickGap={32}
        />
        <YAxis tickLine={false} axisLine={false} width={40} />
        <ChartTooltip
          content={<ChartTooltipContent labelFormatter={(_, payload) => tooltipLabel(payload[0]?.payload.time)} />}
        />
        <Area
          dataKey="consumptionKwh"
          type="monotone"
          stroke="var(--color-consumptionKwh)"
          fill="var(--color-consumptionKwh)"
          fillOpacity={0.25}
          strokeWidth={2}
        />
        {showSolar && (
          <Area
            dataKey="solarKwh"
            type="monotone"
            stroke="var(--color-solarKwh)"
            fill="var(--color-solarKwh)"
            fillOpacity={0.2}
            strokeWidth={2}
          />
        )}
      </AreaChart>
    </ChartContainer>
  )
}
