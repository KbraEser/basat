import { Link, useNavigate } from 'react-router'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { RegisterForm } from '@/components/auth/RegisterForm'
import type { RegisterRequest } from '@/types/auth'
import { ApiError } from '@/lib/api'
import { login, register } from '@/api/auth'
import { useMutation } from '@tanstack/react-query'


function registerErrorMessage(error: Error | null): string | undefined {
  if (!error) return undefined
  if (error instanceof ApiError) {
    if (error.status === 409) return 'Bu e-posta adresiyle zaten bir hesap var.'
    if (error.status === 400) return 'Lütfen bilgileri kontrol edin.'
  }
  return 'Sunucuya ulaşılamadı. Lütfen biraz sonra tekrar deneyin.'
}

// Registration does not set any cookies, so the new admin is logged in right away with the same credentials.
async function registerAndLogin(values: RegisterRequest) {
  await register(values)
  await login({ email: values.email, password: values.password })
}

export function RegisterPage() {
  const navigate = useNavigate()

  const registerMutation = useMutation({
    mutationFn: registerAndLogin,
    onSuccess: () => navigate('/', { replace: true }),
  })

  return (
    <AuthLayout
      title="Kayıt"
      description="İlk kullanıcı yönetici (ADMIN) olarak oluşturulur."
      footer={
        <>
          Zaten hesabınız var mı?{' '}
          <Link to="/login" className="font-semibold text-white hover:underline">
            Giriş yapın
          </Link>
        </>
      }
    >
      <RegisterForm 
       onSubmit={(values) => registerMutation.mutate(values)} 
       isSubmitting={registerMutation.isPending}
       errorMessage={registerErrorMessage(registerMutation.error)}
      />
    </AuthLayout>
  )
}