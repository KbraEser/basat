import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router'
import { ConsumptionPanel } from '@/components/common/ConsumptionPanel'
import { KpiCards } from '@/components/common/KpiCards'
import { PageHeader } from '@/components/common/PageHeader'
import { PeriodSelect } from '@/components/common/PeriodSelect'
import { SectionLabel } from '@/components/common/SectionLabel'
import { MeterTable } from '@/components/sites/MeterTable'
import { Skeleton } from '@/components/ui/skeleton'
import { useConsumption, useKpis } from '@/hooks/useDashboard'
import { useMeters } from '@/hooks/useMeters'
import { useSite } from '@/hooks/useSites'
import { ApiError } from '@/lib/api'
import { formatNumber } from '@/lib/format'
import { setPeriod } from '@/store/dashboardSlice'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { siteTypeLabels, type Site } from '@/types/site'

// "Otel · Antalya · 12.500 m²"; parts that were not entered are left out.
function siteSummary(site: Site) {
  const parts = [siteTypeLabels[site.type], site.city, site.floorAreaM2 && `${formatNumber(site.floorAreaM2)} m²`]
  return parts.filter(Boolean).join(' · ')
}

const backLink = (
  <Link to="/sites" className="flex w-fit items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground">
    <ArrowLeft className="size-4" aria-hidden="true" />
    Tesisler
  </Link>
)

export function SiteDetailPage() {
  // The route is /sites/:siteId, so the parameter is always there on this page.
  const siteId = useParams().siteId!
  const period = useAppSelector((state) => state.dashboard.period)
  const dispatch = useAppDispatch()
  const site = useSite(siteId)
  const kpis = useKpis(siteId)
  const consumption = useConsumption(siteId)
  const meters = useMeters(siteId)

  // Another company's site and a deleted one look the same: 404 for both; a mistyped id in the address is 400.
  if (site.error instanceof ApiError && (site.error.status === 404 || site.error.status === 400)) {
    return (
      <div className="flex flex-col gap-4 p-6">
        {backLink}
        <PageHeader title="Tesis bulunamadı" description="Bu tesis silinmiş ya da size ait değil." />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6 p-6">
      {backLink}
      <PageHeader
        title={site.data ? site.data.name : 'Tesis'}
        description={site.data ? siteSummary(site.data) : undefined}
        actions={<PeriodSelect value={period} onChange={(next) => dispatch(setPeriod(next))} />}
      />

      {(site.isError || kpis.isError) && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm">
          Veriler alınamadı. Lütfen sayfayı yenileyin.
        </p>
      )}

      <SectionLabel>Tüketim ve maliyet</SectionLabel>
      <KpiCards period={period} kpis={kpis.data} points={consumption.data?.points} />

      <SectionLabel>Tüketim eğrisi</SectionLabel>
      <ConsumptionPanel consumption={consumption.data} />

      <SectionLabel>Sayaçlar</SectionLabel>
      <section className="glass-card rounded-xl p-5">
        {meters.data ? <MeterTable meters={meters.data} /> : <Skeleton className="h-32 w-full" />}
      </section>
    </div>
  )
}
