import { Link } from 'react-router'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { RegisterForm } from '@/components/auth/RegisterForm'
import type { RegisterRequest } from '@/types/auth'

export function RegisterPage() {
  // TODO(Kübra): send the request to POST /api/auth/register, then log in with the same credentials.
  const handleRegister = (values: RegisterRequest) => {
    console.log('register', values.companyName)
  }

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
      <RegisterForm onSubmit={handleRegister} />
    </AuthLayout>
  )
}