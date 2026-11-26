import { Skeleton } from '@/components/ui/skeleton'
import type { ConsumptionResponse } from '@/types/dashboard'
import { ConsumptionChart } from './ConsumptionChart'

interface ConsumptionPanelProps {
  // Undefined while loading.
  consumption?: ConsumptionResponse
}

/** Card around the consumption chart with a title that says hourly or daily. */
export function ConsumptionPanel({ consumption }: ConsumptionPanelProps) {
  const daily = consumption?.resolution === 'DAY'

  return (
    <section className="glass-card rounded-xl p-5">
      <header className="mb-4 flex flex-wrap items-center justify-between gap-2">
        <h3 className="text-sm font-medium">{daily ? 'Günlük' : 'Saatlik'} tüketim (kWh)</h3>
        {!daily && <p className="text-xs text-muted-foreground">Arka plan: gündüz, puant ve gece saatleri</p>}
      </header>
      {consumption ? (
        <ConsumptionChart points={consumption.points} resolution={consumption.resolution} />
      ) : (
        <Skeleton className="h-72 w-full" />
      )}
    </section>
  )
}