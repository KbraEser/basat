import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { useDeleteSite } from '@/hooks/useSites'
import { ApiError } from '@/lib/api'
import type { Site } from '@/types/site'

function deleteErrorMessage(error: Error | null): string | undefined {
  if (!error) return undefined
  if (error instanceof ApiError) {
    if (error.status === 409) return 'Bu tesiste hâlâ sayaç var. Önce sayaçları kaldırın.'
    if (error.status === 404) return 'Tesis bulunamadı; zaten silinmiş olabilir.'
    if (error.status === 403) return 'Tesisi silmek için yetkiniz yok.'
  }
  return 'Tesis silinemedi. Lütfen biraz sonra tekrar deneyin.'
}

interface DeleteSiteDialogProps {
  site: Site
}

/** Bin button that asks for confirmation before deleting the site. */
export function DeleteSiteDialog({ site }: DeleteSiteDialogProps) {
  const [open, setOpen] = useState(false)
  const deleteMutation = useDeleteSite()
  const errorMessage = deleteErrorMessage(deleteMutation.error)

  const changeOpen = (next: boolean) => {
    setOpen(next)
    if (!next) deleteMutation.reset()
  }

  const confirmDelete = (event: React.MouseEvent) => {
    // AlertDialogAction closes the dialog on click; keep it open until the backend answers.
    event.preventDefault()
    deleteMutation.mutate(site.id, {
      onSuccess: () => {
        changeOpen(false)
        toast.success(`${site.name} silindi`)
      },
    })
  }

  return (
    <AlertDialog open={open} onOpenChange={changeOpen}>
      <AlertDialogTrigger asChild>
        <Button variant="ghost" size="icon-sm" className="cursor-pointer text-destructive" aria-label={`${site.name} tesisini sil`}>
          <Trash2 aria-hidden="true" />
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{site.name} silinsin mi?</AlertDialogTitle>
          <AlertDialogDescription>Bu işlem geri alınamaz.</AlertDialogDescription>
        </AlertDialogHeader>
        {errorMessage && (
          <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm">
            {errorMessage}
          </p>
        )}
        <AlertDialogFooter>
          <AlertDialogCancel className="cursor-pointer">Vazgeç</AlertDialogCancel>
          <AlertDialogAction
            variant="destructive"
            className="cursor-pointer"
            disabled={deleteMutation.isPending}
            onClick={confirmDelete}
          >
            {deleteMutation.isPending ? 'Siliniyor…' : 'Sil'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
