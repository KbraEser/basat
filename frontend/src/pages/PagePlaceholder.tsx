interface PagePlaceholderProps {
  title: string
  description: string
}

// Temporary page body until each page gets its real components.
export function PagePlaceholder({ title, description }: PagePlaceholderProps) {
  return (
    <div className="flex flex-col gap-1 p-6">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="text-muted-foreground">{description}</p>
    </div>
  )
}