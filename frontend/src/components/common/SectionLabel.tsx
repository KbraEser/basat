interface SectionLabelProps {
  children: React.ReactNode
}

/** Small upper-case heading above a group of cards, e.g. "TÜKETİM". */
export function SectionLabel({ children }: SectionLabelProps) {
  return <h2 className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">{children}</h2>
}