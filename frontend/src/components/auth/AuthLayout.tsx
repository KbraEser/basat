import type { ReactNode } from 'react'
import { Zap } from 'lucide-react'
import { AuthBackground } from './AuthBackground'

interface AuthLayoutProps {
  title: string
  description?: string
  children: ReactNode
  // Link under the form, e.g. "Hesabınız yok mu? Kayıt olun".
  footer?: ReactNode
}

/** Glass card on a wave background, shared by the login and register pages; they have no sidebar. */
export function AuthLayout({ title, description, children, footer }: AuthLayoutProps) {
  return (
    <main className="relative flex min-h-svh items-center justify-center p-4">
      <AuthBackground />

      <section className="w-full max-w-md rounded-3xl border border-white/30 bg-white/5 px-8 py-10 shadow-2xl shadow-black/40 backdrop-blur-2xl sm:px-12">
        <header className="mb-8 grid justify-items-center gap-2 text-center">
          <span className="flex items-center gap-2 text-sm font-semibold tracking-wide text-white/80">
            <Zap className="size-4 text-[oklch(0.85_0.17_80)]" aria-hidden="true" />
            Basat
          </span>
          <h1 className="text-4xl font-bold tracking-tight text-white">{title}</h1>
          {description && <p className="text-sm text-white/70">{description}</p>}
        </header>

        {children}

        {footer && <p className="mt-8 text-center text-sm text-white/80">{footer}</p>}
      </section>
    </main>
  )
}