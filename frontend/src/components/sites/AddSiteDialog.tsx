import { useState } from 'react'
import { Plus } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { useCreateSite } from '@/hooks/useSites'
import { ApiError } from '@/lib/api'
import { SiteForm } from './SiteForm'

function createErrorMessage(error: Error | null): string | undefined {
  if (!error) return undefined
  if (error instanceof ApiError) {
    if (error.status === 409) return 'Bu adla bir tesisiniz zaten var.'
    if (error.status === 403) return 'Tesis eklemek için yetkiniz yok.'
    if (error.status === 400) return 'Lütfen bilgileri kontrol edin.'
  }
  return 'Tesis eklenemedi. Lütfen biraz sonra tekrar deneyin.'
}

/** "Tesis ekle" button that opens the form in a dialog and closes it once the site is saved. */
export function AddSiteDialog() {
  const [open, setOpen] = useState(false)
  const createMutation = useCreateSite()

  const changeOpen = (next: boolean) => {
    setOpen(next)
    if (!next) createMutation.reset()
  }

  return (
    <Dialog open={open} onOpenChange={changeOpen}>
      <DialogTrigger asChild>
        <Button className="cursor-pointer">
          <Plus aria-hidden="true" />
          Tesis ekle
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Yeni tesis</DialogTitle>
          <DialogDescription>Alan girerseniz tesisler m² başına tüketimle karşılaştırılır.</DialogDescription>
        </DialogHeader>
        <SiteForm
          onSubmit={(values) =>
            createMutation.mutate(values, {
              onSuccess: (site) => {
                changeOpen(false)
                toast.success(`${site.name} eklendi`)
              },
            })
          }
          isSubmitting={createMutation.isPending}
          errorMessage={createErrorMessage(createMutation.error)}
        />
      </DialogContent>
    </Dialog>
  )
}