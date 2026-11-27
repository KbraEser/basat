import { Leaf, Wallet, Zap } from 'lucide-react'
import { formatNumber } from '@/lib/format'
import type { ConsumptionPeriod, ConsumptionPoint, KpiResponse } from '@/types/dashboard'
import { Sparkline } from './Sparkline'
import { StatCard } from './StatCard'
import { StatusPill } from './StatusPill'
import { TariffBar } from './TariffBar'

const periodCaptions: Record<ConsumptionPeriod, string> = {
  TODAY: "Bugün 00:00'dan beri",
  LAST_7_DAYS: 'Son 7 günde',
  LAST_30_DAYS: 'Son 30 günde',
}

// A mature tree stores about 22 kg CO₂ a year.
const KG_CO2_PER_TREE_YEAR = 22
// Above this share of the cost in peak hours the badge turns amber.
const PEAK_SHARE_WARNING = 25

// Grey bar the size of the number while the KPIs load; a span, because the number sits inside a <p>
// and shadcn's Skeleton is a <div>, which a <p> may not contain.
const loadingValue = <span className="inline-block h-8 w-28 animate-pulse rounded-md bg-muted align-middle" />

interface KpiCardsProps {
  period: ConsumptionPeriod
  // Undefined while loading: the cards show grey bars instead of numbers.
  kpis?: KpiResponse
  // Consumption over the period for the small chart in the first card.
  points?: ConsumptionPoint[]
}

/** Consumption, cost and carbon cards; the same three cards on the overview and on a site's page. */
export function KpiCards({ period, kpis, points }: KpiCardsProps) {
  const peakCost = kpis?.bands.find((band) => band.band === 'PEAK')?.costTry ?? 0
  const peakShare = kpis && kpis.costTry > 0 ? Math.round((peakCost / kpis.costTry) * 100) : 0

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <StatCard
        icon={Zap}
        tone="consumption"
        title="Tüketim"
        value={kpis ? formatNumber(kpis.consumptionKwh) : loadingValue}
        unit="kWh"
        caption={periodCaptions[period]}
        badge={kpis && kpis.solarKwh > 0 ? <StatusPill status="success">GES {formatNumber(kpis.solarKwh)} kWh</StatusPill> : undefined}
      >
        {points && <Sparkline values={points.map((point) => point.consumptionKwh)} color="var(--consumption)" />}
      </StatCard>
      <StatCard
        icon={Wallet}
        tone="cost"
        title="Maliyet"
        value={kpis ? formatNumber(kpis.costTry) : loadingValue}
        unit="₺"
        caption="Vergiler hariç"
        badge={
          kpis && kpis.costTry > 0 ? (
            <StatusPill status={peakShare > PEAK_SHARE_WARNING ? 'warning' : 'success'}>Puant %{peakShare}</StatusPill>
          ) : undefined
        }
      >
        {kpis && <TariffBar shares={kpis.bands.map((band) => ({ band: band.band, costTry: band.costTry }))} />}
      </StatCard>
      <StatCard
        icon={Leaf}
        tone="carbon"
        title="Karbon"
        value={kpis ? formatNumber(kpis.co2Kg) : loadingValue}
        unit="kg CO₂"
        caption={kpis ? `≈ ${formatNumber(kpis.co2Kg / KG_CO2_PER_TREE_YEAR)} ağacın bir yılda tuttuğu karbon` : undefined}
      />
    </div>
  )
}