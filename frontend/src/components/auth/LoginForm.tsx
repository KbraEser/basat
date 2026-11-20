import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Lock, Mail } from 'lucide-react'
import type { LoginRequest } from '@/types/auth'
import { AuthField } from './AuthField'

// Same rules as LoginRequest in auth-service, so most mistakes are caught before a request is sent.
const schema: yup.ObjectSchema<LoginRequest> = yup.object({
  email: yup.string().trim().required('E-posta gerekli').email('Geçerli bir e-posta girin'),
  password: yup.string().required('Şifre gerekli'),
})

interface LoginFormProps {
  onSubmit: (values: LoginRequest) => void
  isSubmitting?: boolean
  // Message from the server, e.g. wrong credentials (401) or too many attempts (429).
  errorMessage?: string
}

export function LoginForm({ onSubmit, isSubmitting = false, errorMessage }: LoginFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>({ resolver: yupResolver(schema) })

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="grid gap-6">
      {errorMessage && (
        <p role="alert" className="rounded-lg bg-destructive/15 px-3 py-2 text-sm text-white">
          {errorMessage}
        </p>
      )}
      <AuthField id="email" label="E-posta" type="email" icon={Mail} autoComplete="email" error={errors.email?.message} {...register('email')} />
      <AuthField
        id="password"
        label="Şifre"
        type="password"
        icon={Lock}
        autoComplete="current-password"
        error={errors.password?.message}
        {...register('password')}
      />
      <button
        type="submit"
        disabled={isSubmitting}
        className="mt-2 h-11 rounded-full bg-white font-medium text-[oklch(0.2_0.03_265)] transition hover:bg-white/90 disabled:opacity-60"
      >
        {isSubmitting ? 'Giriş yapılıyor…' : 'Giriş yap'}
      </button>
    </form>
  )
}