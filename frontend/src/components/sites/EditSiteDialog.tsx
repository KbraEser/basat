import { useState } from 'react'
import { Pencil } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { useUpdateSite } from '@/hooks/useSites'
import { ApiError } from '@/lib/api'
import type { Site } from '@/types/site'
import { SiteForm } from './SiteForm'

function updateErrorMessage(error: Error | null): string | undefined {
  if (!error) return undefined
  if (error instanceof ApiError) {
    if (error.status === 409) return 'Bu adla başka bir tesisiniz var.'
    if (error.status === 404) return 'Tesis bulunamadı; silinmiş olabilir.'
    if (error.status === 403) return 'Tesisi düzenlemek için yetkiniz yok.'
    if (error.status === 400) return 'Lütfen bilgileri kontrol edin.'
  }
  return 'Tesis kaydedilemedi. Lütfen biraz sonra tekrar deneyin.'
}

interface EditSiteDialogProps {
  site: Site
}

/** Pencil button that opens the site form filled with the current values. */
export function EditSiteDialog({ site }: EditSiteDialogProps) {
  const [open, setOpen] = useState(false)
  const updateMutation = useUpdateSite(site.id)

  const changeOpen = (next: boolean) => {
    setOpen(next)
    if (!next) updateMutation.reset()
  }

  return (
    <Dialog open={open} onOpenChange={changeOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon-sm" className="cursor-pointer" aria-label={`${site.name} tesisini düzenle`}>
          <Pencil aria-hidden="true" />
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tesisi düzenle</DialogTitle>
          <DialogDescription>{site.name}</DialogDescription>
        </DialogHeader>
        <SiteForm
          initialValues={{ name: site.name, type: site.type, floorAreaM2: site.floorAreaM2, city: site.city }}
          onSubmit={(values) =>
            updateMutation.mutate(values, {
              onSuccess: (saved) => {
                changeOpen(false)
                toast.success(`${saved.name} kaydedildi`)
              },
            })
          }
          isSubmitting={updateMutation.isPending}
          errorMessage={updateErrorMessage(updateMutation.error)}
        />
      </DialogContent>
    </Dialog>
  )
}
