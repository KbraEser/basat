import { useId } from 'react'
import { cn } from '@/lib/utils'

interface SparklineProps {
  // Values in time order, e.g. hourly kWh of today.
  values: number[]
  // CSS colour of the line and area, e.g. "var(--consumption)".
  color: string
  // Live data: blinking points, a pulsing latest point and a current pulse running along the line.
  live?: boolean
  className?: string
}

/** Tiny line chart without axes; shows the shape of a series at a glance. */
export function Sparkline({ values, color, live = false, className }: SparklineProps) {
  // unique ids, because several sparklines on one page must not share their gradient
  const id = useId()
  if (values.length < 2) return null

  const max = Math.max(...values)
  const min = Math.min(...values)
  const range = max - min || 1
  // the drawing is 100 × 30 units and stretched to the element's size
  const coords = values.map((value, i) => ({
    x: (i / (values.length - 1)) * 100,
    y: 28 - ((value - min) / range) * 24,
  }))
  const line = `M${coords.map((c) => `${c.x.toFixed(2)} ${c.y.toFixed(2)}`).join(' L')}`
  const area = `${line} L100 30 L0 30 Z`
  const last = coords[coords.length - 1]

  return (
    <div className={cn('relative h-12 w-full', className)} aria-hidden="true">
      <svg className="absolute inset-0 size-full overflow-visible" viewBox="0 0 100 30" preserveAspectRatio="none">
        <defs>
          <linearGradient id={`${id}-area`} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor={color} stopOpacity="0.35" />
            <stop offset="1" stopColor={color} stopOpacity="0" />
          </linearGradient>
        </defs>
        <path d={area} fill={`url(#${id}-area)`} />
        <path d={line} fill="none" stroke={color} strokeWidth="1.5" vectorEffect="non-scaling-stroke" />
        {/* no non-scaling-stroke here: it would make the dash ignore pathLength and repeat */}
        {live && (
          <path
            d={line}
            pathLength={100}
            fill="none"
            stroke="oklch(0.95 0.13 90)"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeDasharray="6 94"
            className="motion-safe:animate-[current-flow_3s_linear_infinite] motion-reduce:hidden"
          />
        )}
      </svg>

      {/* points are HTML, not SVG, so they stay round although the chart is stretched */}
      {live &&
        coords.slice(0, -1).map((c, i) => (
          <span
            key={i}
            className="absolute size-1.5 -translate-x-1/2 -translate-y-1/2 rounded-full motion-safe:animate-[twinkle_2.4s_ease-in-out_infinite]"
            style={{ left: `${c.x}%`, top: `${(c.y / 30) * 100}%`, backgroundColor: color, animationDelay: `${(i * 0.2) % 2.4}s` }}
          />
        ))}
      {live && (
        <span
          className="absolute flex size-2.5 -translate-x-1/2 -translate-y-1/2"
          style={{ left: `${last.x}%`, top: `${(last.y / 30) * 100}%` }}
        >
          <span className="absolute inline-flex size-full rounded-full opacity-75 motion-safe:animate-ping" style={{ backgroundColor: color }} />
          <span className="relative inline-flex size-2.5 rounded-full" style={{ backgroundColor: color }} />
        </span>
      )}
    </div>
  )
}
