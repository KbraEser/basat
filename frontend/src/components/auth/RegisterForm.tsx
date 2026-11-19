import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Building2, Lock, Mail, User } from 'lucide-react'
import type { RegisterRequest } from '@/types/auth'
import { AuthField } from './AuthField'

// Same limits as RegisterRequest in auth-service (password 8–72: BCrypt's maximum).
const schema: yup.ObjectSchema<RegisterRequest> = yup.object({
  companyName: yup.string().trim().required('Şirket adı gerekli').max(200, 'En fazla 200 karakter'),
  fullName: yup.string().trim().required('Ad soyad gerekli').max(200, 'En fazla 200 karakter'),
  email: yup.string().trim().required('E-posta gerekli').email('Geçerli bir e-posta girin').max(255, 'En fazla 255 karakter'),
  password: yup.string().required('Şifre gerekli').min(8, 'En az 8 karakter').max(72, 'En fazla 72 karakter'),
})

interface RegisterFormProps {
  onSubmit: (values: RegisterRequest) => void
  isSubmitting?: boolean
  // Message from the server, e.g. the email is already registered (409).
  errorMessage?: string
}

export function RegisterForm({ onSubmit, isSubmitting = false, errorMessage }: RegisterFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterRequest>({ resolver: yupResolver(schema) })

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="grid gap-5">
      {errorMessage && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm text-white">
          {errorMessage}
        </p>
      )}
      <AuthField id="companyName" label="Şirket adı" icon={Building2} autoComplete="organization" error={errors.companyName?.message} {...register('companyName')} />
      <AuthField id="fullName" label="Ad soyad" icon={User} autoComplete="name" error={errors.fullName?.message} {...register('fullName')} />
      <AuthField id="email" label="E-posta" type="email" icon={Mail} autoComplete="email" error={errors.email?.message} {...register('email')} />
      <AuthField
        id="password"
        label="Şifre"
        type="password"
        icon={Lock}
        autoComplete="new-password"
        error={errors.password?.message}
        {...register('password')}
      />
      <button
        type="submit"
        disabled={isSubmitting}
        className="mt-2 h-11 rounded-full bg-white font-medium text-[oklch(0.2_0.03_265)] transition hover:bg-white/90 disabled:opacity-60"
      >
        {isSubmitting ? 'Kaydediliyor…' : 'Hesap oluştur'}
      </button>
    </form>
  )
}