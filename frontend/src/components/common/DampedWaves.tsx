export interface Wave {
  // Where the wave starts, in the 1440 × 900 drawing.
  x: number
  y: number
  direction: 'right' | 'left'
  length: number
  // Height of the first swing; it shrinks as the wave travels (damping).
  amplitude: number
  wavelength: number
  phase: number
  // Vertical drift per unit travelled: negative values make the wave rise as it goes.
  slope?: number
  // Seconds one current pulse needs to travel the whole wave; negative delay = already on its way.
  duration: number
  delay: number
}

// Damped sine wave, like an oscillation dying out: y = A · e^(−3s/L) · sin(2πs/λ + φ).
function dampedWavePath({ x, y, direction, length, amplitude, wavelength, phase, slope = 0 }: Wave): string {
  const points: string[] = []
  for (let s = 0; s <= length; s += 8) {
    const swing = amplitude * Math.exp((-3 * s) / length) * Math.sin((2 * Math.PI * s) / wavelength + phase)
    const px = direction === 'right' ? x + s : x - s
    points.push(`${px.toFixed(1)} ${(y + slope * s + swing).toFixed(1)}`)
  }
  return `M${points.join(' L')}`
}

const GOLD = 'oklch(0.85 0.16 85)'
const GOLD_BRIGHT = 'oklch(0.95 0.13 90)'

interface DampedWavesProps {
  waves: Wave[]
  // How visible the waves are (0–1); the dashboard uses them more quietly than the login page.
  intensity?: number
  className?: string
}

/** Golden damped waves with current pulses running along them; Basat's visual signature. */
export function DampedWaves({ waves, intensity = 1, className }: DampedWavesProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 1440 900"
      preserveAspectRatio="xMidYMid slice"
      aria-hidden="true"
      style={{ opacity: intensity }}
    >
      <defs>
        {/* bright at the start of the wave, transparent at its end; "left" waves fade the other way */}
        <linearGradient id="wave-fade-right" x1="0" x2="1">
          <stop offset="0" stopColor={GOLD} />
          <stop offset="1" stopColor={GOLD} stopOpacity="0" />
        </linearGradient>
        <linearGradient id="wave-fade-left" x1="1" x2="0">
          <stop offset="0" stopColor={GOLD} />
          <stop offset="1" stopColor={GOLD} stopOpacity="0" />
        </linearGradient>
        {/* same fade for the pulses, so the current dies out together with the wave */}
        <linearGradient id="pulse-fade-right" x1="0" x2="1">
          <stop offset="0" stopColor={GOLD_BRIGHT} />
          <stop offset="0.85" stopColor={GOLD_BRIGHT} stopOpacity="0" />
        </linearGradient>
        <linearGradient id="pulse-fade-left" x1="1" x2="0">
          <stop offset="0" stopColor={GOLD_BRIGHT} />
          <stop offset="0.85" stopColor={GOLD_BRIGHT} stopOpacity="0" />
        </linearGradient>
        {/* blurred copy under the pulse makes it glow */}
        <filter id="wave-glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="4" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      <g fill="none" strokeLinecap="round">
        {waves.map((wave) => {
          const path = dampedWavePath(wave)
          return (
            <g key={`${wave.x}-${wave.y}`}>
              {/* the wave itself, fading out as it travels */}
              <path d={path} stroke={`url(#wave-fade-${wave.direction})`} strokeWidth="1.5" strokeOpacity="0.7" />
              {/* the current: a short bright dash running along the wave */}
              <path
                d={path}
                pathLength={100}
                stroke={`url(#pulse-fade-${wave.direction})`}
                strokeWidth="3"
                strokeDasharray="5 95"
                filter="url(#wave-glow)"
                className="motion-safe:animate-[current-flow_5s_linear_infinite] motion-reduce:hidden"
                style={{ animationDuration: `${wave.duration}s`, animationDelay: `${wave.delay}s` }}
              />
            </g>
          )
        })}
      </g>
    </svg>
  )
}
