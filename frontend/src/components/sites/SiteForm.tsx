import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { FormField } from '@/components/auth/FormField'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { siteTypeLabels, type SiteRequest, type SiteType } from '@/types/site'

const siteTypes = Object.keys(siteTypeLabels) as SiteType[]

// An empty box means "not given": the backend expects null, not "" or 0.
const emptyToNull = (value: unknown, original: unknown) => (original === '' ? null : value)

// Same, and accepts the Turkish decimal comma: "1200,5" becomes 1200.5.
const toArea = (value: unknown, original: unknown) => {
  if (original === '') return null
  return typeof original === 'string' ? Number(original.replace(',', '.')) : value
}

// Same limits as SiteRequest in monitoring-service.
const schema: yup.ObjectSchema<SiteRequest> = yup.object({
  name: yup.string().trim().required('Tesis adı gerekli').max(200, 'En fazla 200 karakter'),
  type: yup.mixed<SiteType>().oneOf(siteTypes, 'Tesis türünü seçin').required('Tesis türünü seçin'),
  floorAreaM2: yup
    .number()
    .transform(toArea)
    .nullable()
    .defined()
    .typeError('Sayı girin')
    .positive('Alan sıfırdan büyük olmalı'),
  city: yup.string().trim().transform(emptyToNull).nullable().defined().max(100, 'En fazla 100 karakter'),
})

interface SiteFormProps {
  onSubmit: (values: SiteRequest) => void
  isSubmitting?: boolean
  // Message from the server, e.g. a duplicate name (409).
  errorMessage?: string
}

/** Name, type, floor area and city of a site; checks the input before anything is sent. */
export function SiteForm({ onSubmit, isSubmitting = false, errorMessage }: SiteFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SiteRequest>({
    resolver: yupResolver(schema),
    defaultValues: { name: '', type: 'OFFICE', floorAreaM2: null, city: null },
  })

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="grid gap-4">
      {errorMessage && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm">
          {errorMessage}
        </p>
      )}
      <FormField id="site-name" label="Tesis adı" placeholder="Antalya Otel" error={errors.name?.message} {...register('name')} />

      <div className="grid gap-2">
        <Label htmlFor="site-type">Tür</Label>
        <select
          id="site-type"
          className="h-8 rounded-lg border border-input bg-input/30 px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
          {...register('type')}
        >
          {siteTypes.map((type) => (
            <option key={type} value={type}>
              {siteTypeLabels[type]}
            </option>
          ))}
        </select>
        {errors.type && <p className="text-sm text-destructive">{errors.type.message}</p>}
      </div>

      <FormField
        id="site-area"
        label="Alan (m², isteğe bağlı)"
        inputMode="decimal"
        placeholder="1200"
        error={errors.floorAreaM2?.message}
        {...register('floorAreaM2')}
      />
      <FormField id="site-city" label="Şehir (isteğe bağlı)" placeholder="Antalya" error={errors.city?.message} {...register('city')} />

      <Button type="submit" disabled={isSubmitting} className="mt-2 cursor-pointer">
        {isSubmitting ? 'Kaydediliyor…' : 'Kaydet'}
      </Button>
    </form>
  )
}