import { useState, type ComponentProps } from 'react'
import { Eye, EyeOff, type LucideIcon } from 'lucide-react'

interface AuthFieldProps extends ComponentProps<'input'> {
  id: string
  label: string
  icon: LucideIcon
  error?: string
}

/** Underlined input with an icon on the right, for the glass login and register cards. */
export function AuthField({ id, label, icon: Icon, error, type, ...inputProps }: AuthFieldProps) {
  const isPassword = type === 'password'
  // password fields get an eye button instead of the icon, so users can check what they typed
  const [visible, setVisible] = useState(false)
  const EyeIcon = visible ? EyeOff : Eye

  return (
    <div className="grid gap-1">
      <label htmlFor={id} className="text-sm font-medium text-white/80">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={isPassword && visible ? 'text' : type}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${id}-error` : undefined}
          className="peer w-full border-0 border-b-2 border-white/40 bg-transparent py-2 pr-8 text-white outline-none transition-colors placeholder:text-white/40 focus:border-white aria-invalid:border-destructive"
          {...inputProps}
        />
        {isPassword ? (
          <button
            type="button"
            onClick={() => setVisible((shown) => !shown)}
            aria-label={visible ? 'Şifreyi gizle' : 'Şifreyi göster'}
            aria-pressed={visible}
            className="absolute top-1/2 right-0 -translate-y-1/2 cursor-pointer rounded p-1 text-white/70 transition-colors hover:text-white"
          >
            <EyeIcon className="size-4" aria-hidden="true" />
          </button>
        ) : (
          <Icon className="absolute top-1/2 right-1 size-4 -translate-y-1/2 text-white/70 peer-focus:text-white" aria-hidden="true" />
        )}
      </div>
      {error && (
        <p id={`${id}-error`} className="text-xs text-destructive">
          {error}
        </p>
      )}
    </div>
  )
}
