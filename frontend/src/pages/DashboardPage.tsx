import { Leaf, Wallet, Zap } from 'lucide-react'
import { LivePowerCard } from '@/components/common/LivePowerCard'
import { PageHeader } from '@/components/common/PageHeader'
import { SectionLabel } from '@/components/common/SectionLabel'
import { Sparkline } from '@/components/common/Sparkline'
import { StatCard } from '@/components/common/StatCard'
import { StatusPill } from '@/components/common/StatusPill'
import { TariffBar } from '@/components/common/TariffBar'

// Sample figures until the page is connected to /api/kpis, /api/consumption and the live topic.
const hourlyKwh = [32, 30, 29, 28, 30, 38, 55, 72, 88, 95, 98, 96, 90, 94, 97, 92, 85, 80, 74, 66, 58, 48, 40, 35]
const livePower = [41.2, 41.8, 42.5, 41.9, 42.8, 43.4, 42.6, 42.1, 42.9, 43.6, 43.1, 42.3]

// A mature tree stores about 22 kg CO₂ a year.
const KG_CO2_PER_TREE_YEAR = 22

export function DashboardPage() {
  const co2Kg = 588
  return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader title="Genel bakış" description="Bugün, tüm tesisler." actions={<span>Son güncelleme 11:22</span>} />

      <LivePowerCard powerKw={42.3} history={livePower} updatedAt="11:22:10" scope="Tüm tesisler" />

      <SectionLabel>Tüketim ve maliyet</SectionLabel>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <StatCard
          icon={Zap}
          tone="consumption"
          title="Tüketim"
          value="1.240"
          unit="kWh"
          caption="Bugün 00:00'dan beri"
          badge={<StatusPill status="success">Veri geliyor</StatusPill>}
        >
          <Sparkline values={hourlyKwh} color="var(--consumption)" />
        </StatCard>
        <StatCard
          icon={Wallet}
          tone="cost"
          title="Maliyet"
          value="7.380"
          unit="₺"
          caption="Vergiler hariç"
          badge={<StatusPill status="warning">Puant %32</StatusPill>}
        >
          <TariffBar
            shares={[
              { band: 'DAY', costTry: 3950 },
              { band: 'PEAK', costTry: 2360 },
              { band: 'NIGHT', costTry: 1070 },
            ]}
          />
        </StatCard>
        <StatCard
          icon={Leaf}
          tone="carbon"
          title="Karbon"
          value="588"
          unit="kg CO₂"
          caption={`≈ ${Math.round(co2Kg / KG_CO2_PER_TREE_YEAR)} ağacın bir yılda tuttuğu karbon`}
        />
      </div>
    </div>
  )
}
