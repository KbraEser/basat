import { DampedWaves, type Wave } from '@/components/common/DampedWaves'

// Horizontal waves at different heights; the ones near the card are short so they fade out before reaching it.
const waves: Wave[] = [
  { x: -20, y: 90, direction: 'right', length: 1100, amplitude: 40, wavelength: 260, phase: 0, duration: 5, delay: 0 },
  { x: 1460, y: 190, direction: 'left', length: 900, amplitude: 30, wavelength: 220, phase: 1.2, duration: 6, delay: -2 },
  { x: -20, y: 330, direction: 'right', length: 520, amplitude: 26, wavelength: 180, phase: 2.1, duration: 4.5, delay: -3.5 },
  { x: 1460, y: 450, direction: 'left', length: 480, amplitude: 22, wavelength: 170, phase: 0.6, duration: 4, delay: -1 },
  { x: -20, y: 590, direction: 'right', length: 500, amplitude: 28, wavelength: 190, phase: 1.8, duration: 5, delay: -2.5 },
  { x: 1460, y: 710, direction: 'left', length: 950, amplitude: 34, wavelength: 240, phase: 2.6, duration: 6.5, delay: -4 },
  { x: -20, y: 820, direction: 'right', length: 1150, amplitude: 42, wavelength: 270, phase: 0.9, duration: 5.5, delay: -1.5 },
]

// Decorative background for the login and register pages.
export function AuthBackground() {
  return (
    <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-[oklch(0.12_0.02_265)]" aria-hidden="true">
      <DampedWaves waves={waves} className="absolute inset-0 size-full" />
    </div>
  )
}
