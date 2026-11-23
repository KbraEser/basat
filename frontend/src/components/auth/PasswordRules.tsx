import { Check, Circle } from 'lucide-react'
import { cn } from '@/lib/utils'
import { passwordRules } from './passwordPolicy'

interface PasswordRulesProps {
  // The password typed so far; each rule turns green as soon as it is met.
  value: string
}

/** Live checklist under the register password field. */
export function PasswordRules({ value }: PasswordRulesProps) {
  return (
    <ul className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs" aria-label="Şifre kuralları">
      {passwordRules.map((rule) => {
        const met = rule.test(value)
        const Icon = met ? Check : Circle
        return (
          <li key={rule.label} className={cn('flex items-center gap-1.5 transition-colors', met ? 'text-success' : 'text-white/50')}>
            <Icon className="size-3" aria-hidden="true" />
            {rule.label}
            <span className="sr-only">{met ? ' (sağlandı)' : ' (eksik)'}</span>
          </li>
        )
      })}
    </ul>
  )
}
