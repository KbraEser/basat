import { PageHeader } from '@/components/common/PageHeader'
import { AddSiteDialog } from '@/components/sites/AddSiteDialog'
import { SiteTable } from '@/components/sites/SiteTable'
import { Skeleton } from '@/components/ui/skeleton'
import { useCurrentUser } from '@/hooks/useCurrentUser'
import { useSites } from '@/hooks/useSites'

export function SitesPage() {
  const sites = useSites()

    const { data: user } = useCurrentUser()
    // The backend answers 403 to viewers anyway; hiding the button just avoids a dead end.
    const canManage = user?.role === 'ADMIN' || user?.role === 'ENERGY_MANAGER'
  
  
    return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader
        title="Tesisler"
        description={sites.data ? `${sites.data.length} tesis` : 'Şirketinize ait binalar ve sayaçları.'}
        actions={canManage && <AddSiteDialog /> }
      />

      {sites.isError && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm">
          Tesisler alınamadı. Lütfen sayfayı yenileyin.
        </p>
      )}

      <section className="glass-card rounded-xl p-5">
        {sites.data ? <SiteTable sites={sites.data} /> : <Skeleton className="h-40 w-full" />}
      </section>
    </div>
  )
}