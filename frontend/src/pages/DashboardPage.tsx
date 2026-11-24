import { Leaf, Wallet, Zap } from 'lucide-react'
import { ConsumptionChart } from '@/components/common/ConsumptionChart'
import { LivePowerCard } from '@/components/common/LivePowerCard'
import { PageHeader } from '@/components/common/PageHeader'
import { PeriodSelect } from '@/components/common/PeriodSelect'
import { SectionLabel } from '@/components/common/SectionLabel'
import { SiteComparisonTable } from '@/components/common/SiteComparisonTable'
import { Sparkline } from '@/components/common/Sparkline'
import { StatCard } from '@/components/common/StatCard'
import { StatusPill } from '@/components/common/StatusPill'
import { TariffBar } from '@/components/common/TariffBar'
import { Skeleton } from '@/components/ui/skeleton'
import { useConsumption, useKpis, useSiteKpis } from '@/hooks/useDashboard'
import { formatNumber } from '@/lib/format'
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

const periodCaptions: Record<ConsumptionPeriod, string> = {
  TODAY: "Bugün 00:00'dan beri",
  LAST_7_DAYS: 'Son 7 günde',
  LAST_30_DAYS: 'Son 30 günde',
}

// A mature tree stores about 22 kg CO₂ a year.
const KG_CO2_PER_TREE_YEAR = 22
// Above this share of the cost in peak hours the badge turns amber.
const PEAK_SHARE_WARNING = 25

// Grey bar the size of the number while the KPIs load.
const loadingValue = <Skeleton className="inline-block h-8 w-28 align-middle" />

export function DashboardPage() {
  const period = useAppSelector((state) => state.dashboard.period)
  const dispatch = useAppDispatch()
  const kpis = useKpis()
  const consumption = useConsumption()
  const siteKpis = useSiteKpis()

  const data = kpis.data
  const peakCost = data?.bands.find((band) => band.band === 'PEAK')?.costTry ?? 0
  const peakShare = data && data.costTry > 0 ? Math.round((peakCost / data.costTry) * 100) : 0

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
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <StatCard
          icon={Zap}
          tone="consumption"
          title="Tüketim"
          value={data ? formatNumber(data.consumptionKwh) : loadingValue}
          unit="kWh"
          caption={periodCaptions[period]}
          badge={data && data.solarKwh > 0 ? <StatusPill status="success">GES {formatNumber(data.solarKwh)} kWh</StatusPill> : undefined}
        >
          {consumption.data && (
            <Sparkline values={consumption.data.points.map((point) => point.consumptionKwh)} color="var(--consumption)" />
          )}
        </StatCard>
        <StatCard
          icon={Wallet}
          tone="cost"
          title="Maliyet"
          value={data ? formatNumber(data.costTry) : loadingValue}
          unit="₺"
          caption="Vergiler hariç"
          badge={
            data && data.costTry > 0 ? (
              <StatusPill status={peakShare > PEAK_SHARE_WARNING ? 'warning' : 'success'}>Puant %{peakShare}</StatusPill>
            ) : undefined
          }
        >
          {data && <TariffBar shares={data.bands.map((band) => ({ band: band.band, costTry: band.costTry }))} />}
        </StatCard>
        <StatCard
          icon={Leaf}
          tone="carbon"
          title="Karbon"
          value={data ? formatNumber(data.co2Kg) : loadingValue}
          unit="kg CO₂"
          caption={data ? `≈ ${formatNumber(data.co2Kg / KG_CO2_PER_TREE_YEAR)} ağacın bir yılda tuttuğu karbon` : undefined}
        />
      </div>

      <SectionLabel>Tüketim eğrisi</SectionLabel>
      <section className="glass-card rounded-xl p-5">
        <header className="mb-4 flex flex-wrap items-center justify-between gap-2">
          <h3 className="text-sm font-medium">{period === 'LAST_30_DAYS' ? 'Günlük' : 'Saatlik'} tüketim (kWh)</h3>
          {period !== 'LAST_30_DAYS' && (
            <p className="text-xs text-muted-foreground">Arka plan: gündüz, puant ve gece saatleri</p>
          )}
        </header>
        {consumption.data ? (
          <ConsumptionChart points={consumption.data.points} resolution={consumption.data.resolution} />
        ) : (
          <Skeleton className="h-72 w-full" />
        )}
      </section>

      <SectionLabel>Tesis karşılaştırması</SectionLabel>
      <section className="glass-card rounded-xl p-5">
        {siteKpis.data ? <SiteComparisonTable rows={siteKpis.data} /> : <Skeleton className="h-40 w-full" />}
      </section>
    </div>
  )
}