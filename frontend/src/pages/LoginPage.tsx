import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { login } from '@/api/auth'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { LoginForm } from '@/components/auth/LoginForm'
import { ApiError } from '@/lib/api'

// Turns the backend's answer into a sentence for the user; the backend sends the same 401 for both wrong fields.
function loginErrorMessage(error: Error | null): string | undefined {
  if (!error) return undefined
  if (error instanceof ApiError) {
    if (error.status === 401) return 'E-posta veya şifre hatalı.'
    if (error.status === 429) return 'Çok fazla deneme yapıldı. Lütfen 15 dakika sonra tekrar deneyin.'
    if (error.status === 400) return 'Lütfen bilgileri kontrol edin.'
  }
  return 'Sunucuya ulaşılamadı. Lütfen biraz sonra tekrar deneyin.'
}

export function LoginPage() {
  const navigate = useNavigate()

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: () => navigate('/', { replace: true }),
  })

  return (
    <AuthLayout
      title="Giriş"
      footer={
        <>
          Hesabınız yok mu?{' '}
          <Link to="/register" className="font-semibold text-white hover:underline">
            Kayıt olun
          </Link>
        </>
      }
    >
      <LoginForm
        onSubmit={(values) => loginMutation.mutate(values)}
        isSubmitting={loginMutation.isPending}
        errorMessage={loginErrorMessage(loginMutation.error)}
      />
    </AuthLayout>
  )
}