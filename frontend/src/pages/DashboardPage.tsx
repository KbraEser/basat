import { ConsumptionPanel } from '@/components/common/ConsumptionPanel'
import { KpiCards } from '@/components/common/KpiCards'
import { LivePowerCard } from '@/components/common/LivePowerCard'
import { PageHeader } from '@/components/common/PageHeader'
import { PeriodSelect } from '@/components/common/PeriodSelect'
import { SectionLabel } from '@/components/common/SectionLabel'
import { SiteComparisonTable } from '@/components/common/SiteComparisonTable'
import { Skeleton } from '@/components/ui/skeleton'
import { useConsumption, useKpis, useSiteKpis } from '@/hooks/useDashboard'
import { useLivePower } from '@/hooks/useLivePower'
import { useSites } from '@/hooks/useSites'
import { setPeriod } from '@/store/dashboardSlice'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import type { ConsumptionPeriod } from '@/types/dashboard'


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
  const sites = useSites()
  const livePower = useLivePower(sites.data?.map((site) => site.id) ?? [])

  return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader
        title="Genel bakış"
        description={periodDescriptions[period]}
        actions={<PeriodSelect value={period} onChange={(next) => dispatch(setPeriod(next))} />}
      />

      <LivePowerCard {...livePower} scope="Tüm tesisler" />

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