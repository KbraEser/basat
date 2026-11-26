import { ConsumptionPanel } from '@/components/common/ConsumptionPanel'
import { KpiCards } from '@/components/common/KpiCards'
import { LivePowerCard } from '@/components/common/LivePowerCard'
import { PageHeader } from '@/components/common/PageHeader'
import { PeriodSelect } from '@/components/common/PeriodSelect'
import { SectionLabel } from '@/components/common/SectionLabel'
import { SiteComparisonTable } from '@/components/common/SiteComparisonTable'
import { Skeleton } from '@/components/ui/skeleton'
import { useConsumption, useKpis, useSiteKpis } from '@/hooks/useDashboard'
import { setPeriod } from '@/store/dashboardSlice'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import type { ConsumptionPeriod } from '@/types/dashboard'

// Sample figures until the live card is connected to the WebSocket topic (F5).
const livePower = [41.2, 41.8, 42.5, 41.9, 42.8, 43.4, 42.6, 42.1, 42.9, 43.6, 43.1, 42.3]

const periodDescriptions: Record<ConsumptionPeriod, string> = {
  TODAY: 'Bugün, tüm tesisler.',
  LAST_7_DAYS: 'Son 7 gün, tüm tesisler.',
  LAST_30_DAYS: 'Son 30 gün, tüm tesisler.',
}

export function DashboardPage() {
  const period = useAppSelector((state) => state.dashboard.period)
  const dispatch = useAppDispatch()
  const kpis = useKpis()
  const consumption = useConsumption()
  const siteKpis = useSiteKpis()

  return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader
        title="Genel bakış"
        description={periodDescriptions[period]}
        actions={<PeriodSelect value={period} onChange={(next) => dispatch(setPeriod(next))} />}
      />

      <LivePowerCard powerKw={42.3} history={livePower} updatedAt="11:22:10" scope="Tüm tesisler" />

      {kpis.isError && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm">
          Veriler alınamadı. Lütfen sayfayı yenileyin.
        </p>
      )}

      <SectionLabel>Tüketim ve maliyet</SectionLabel>
      <KpiCards period={period} kpis={kpis.data} points={consumption.data?.points} />

      <SectionLabel>Tüketim eğrisi</SectionLabel>
      <ConsumptionPanel consumption={consumption.data} />

      <SectionLabel>Tesis karşılaştırması</SectionLabel>
      <section className="glass-card rounded-xl p-5">
        {siteKpis.data ? <SiteComparisonTable rows={siteKpis.data} /> : <Skeleton className="h-40 w-full" />}
      </section>
    </div>
  )
}