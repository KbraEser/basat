import { cn } from '@/lib/utils'

export type Status = 'success' | 'warning' | 'danger' | 'neutral'

// Tailwind only ships classes it can see in the code, so each status lists its full class names.
const styles: Record<Status, string> = {
  success: 'border-success/30 bg-success/10 text-success',
  warning: 'border-warning/30 bg-warning/10 text-warning',
  danger: 'border-destructive/30 bg-destructive/10 text-destructive',
  neutral: 'border-border bg-muted/40 text-muted-foreground',
}

interface StatusPillProps {
  status: Status
  children: React.ReactNode
  className?: string
}

/** Small rounded label with a coloured dot, e.g. "● Veri geliyor". */
export function StatusPill({ status, children, className }: StatusPillProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-medium whitespace-nowrap',
        styles[status],
        className,
      )}
    >
      <span className="size-1.5 rounded-full bg-current" aria-hidden="true" />
      {children}
    </span>
  )
}