import { Leaf, Wallet, Zap } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { SectionLabel } from '@/components/common/SectionLabel'
import { StatCard } from '@/components/common/StatCard'
import { StatusPill } from '@/components/common/StatusPill'

// Sample figures until the page is connected to /api/kpis.
export function DashboardPage() {
  return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader title="Genel bakış" description="Bugün, tüm tesisler." actions={<span>Son güncelleme 11:22</span>} />

      <SectionLabel>Tüketim ve maliyet</SectionLabel>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <StatCard
          icon={Zap}
          title="Tüketim"
          value="1.240"
          unit="kWh"
          caption="Bugün 00:00'dan beri"
          badge={<StatusPill status="success">Veri geliyor</StatusPill>}
        />
        <StatCard
          icon={Wallet}
          title="Maliyet"
          value="7.380"
          unit="₺"
          caption="Vergiler hariç"
          badge={<StatusPill status="warning">Puant %32</StatusPill>}
          stats={[
            { label: 'Gündüz', value: '3.950 ₺' },
            { label: 'Puant', value: '2.360 ₺' },
            { label: 'Gece', value: '1.070 ₺' },
          ]}
        />
        <StatCard icon={Leaf} title="Karbon" value="588" unit="kg CO₂" caption="Şebeke emisyon faktörü 0,474" />
      </div>
    </div>
  )
}