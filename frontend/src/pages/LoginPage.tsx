import { Link } from 'react-router'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { LoginForm } from '@/components/auth/LoginForm'
import type { LoginRequest } from '@/types/auth'

export function LoginPage() {
  // TODO(Kübra): send the request to POST /api/auth/login, then go to the dashboard.
  const handleLogin = (values: LoginRequest) => {
    console.log('login', values.email)
  }

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
      <LoginForm onSubmit={handleLogin} />
    </AuthLayout>
  )
}