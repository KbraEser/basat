import { Link } from 'react-router'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { formatNumber } from '@/lib/format'
import { cn } from '@/lib/utils'
import { siteTypeLabels, type Site } from '@/types/site'

const dateLabel = new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' })

interface SiteTableProps {
  sites: Site[]
  // Buttons at the end of each row, e.g. edit and delete; left out for users who cannot change sites.
  renderActions?: (site: Site) => React.ReactNode
  className?: string
}

/** Company's sites with type, city and floor area; each name opens the site's page. */
export function SiteTable({ sites, renderActions, className }: SiteTableProps) {
  if (sites.length === 0) {
    return (
      <p className={cn('py-8 text-center text-sm text-muted-foreground', className)}>
        Henüz tesis yok. İlk tesisinizi ekleyerek başlayın.
      </p>
    )
  }

  return (
    <Table className={className}>
      <TableHeader>
        <TableRow className="hover:bg-transparent">
          <TableHead>Tesis</TableHead>
          <TableHead>Tür</TableHead>
          <TableHead>Şehir</TableHead>
          <TableHead className="text-right">Alan (m²)</TableHead>
          <TableHead className="text-right">Eklenme</TableHead>
          {renderActions && <TableHead className="w-0" aria-label="İşlemler" />}
        </TableRow>
      </TableHeader>
      <TableBody>
        {sites.map((site) => (
          <TableRow key={site.id}>
            <TableCell>
              <Link to={`/sites/${site.id}`} className="font-medium hover:underline">
                {site.name}
              </Link>
            </TableCell>
            <TableCell>{siteTypeLabels[site.type]}</TableCell>
            <TableCell>{site.city ?? <span className="text-muted-foreground">—</span>}</TableCell>
            <TableCell className="text-right font-mono tabular-nums">
              {site.floorAreaM2 === null ? <span className="text-muted-foreground">—</span> : formatNumber(site.floorAreaM2)}
            </TableCell>
            <TableCell className="text-right text-muted-foreground">{dateLabel.format(new Date(site.createdAt))}</TableCell>
            {renderActions && (
              <TableCell>
                <div className="flex justify-end gap-1">{renderActions(site)}</div>
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}