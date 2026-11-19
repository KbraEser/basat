interface PageHeaderProps {
  title: string
  description?: string
  actions?: React.ReactNode
}

/** Title, one-line description and optional actions at the top of every page. */
export function PageHeader({ title, description, actions }: PageHeaderProps) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div className="grid gap-1">
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="text-sm text-muted-foreground">{description}</p>}
      </div>
      {actions && <div className="flex items-center gap-3 text-xs text-muted-foreground">{actions}</div>}
    </div>
  )
}