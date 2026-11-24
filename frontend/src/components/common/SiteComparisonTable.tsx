import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { formatNumber } from '@/lib/format'
import { cn } from '@/lib/utils'
import type { SiteKpi } from '@/types/dashboard'
import { siteTypeLabels } from '@/types/site'

interface SiteComparisonTableProps {
  rows: SiteKpi[]
  className?: string
}

/** Sites side by side for the selected period; the bar shows each site's share of the largest consumption. */
export function SiteComparisonTable({ rows, className }: SiteComparisonTableProps) {
  if (rows.length === 0) {
    return <p className={cn('py-8 text-center text-sm text-muted-foreground', className)}>Henüz tesis eklenmemiş.</p>
  }

  // The biggest consumer gets a full bar; Math.max(..., 1) avoids dividing by zero when nothing was used.
  const maxKwh = Math.max(...rows.map((row) => row.consumptionKwh), 1)

  return (
    <Table className={className}>
      <TableHeader>
        <TableRow className="hover:bg-transparent">
          <TableHead>Tesis</TableHead>
          <TableHead className="text-right">Tüketim (kWh)</TableHead>
          <TableHead className="text-right">Maliyet (₺)</TableHead>
          <TableHead className="text-right">kWh / m²</TableHead>
          <TableHead className="text-right">GES (kWh)</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row) => (
          <TableRow key={row.siteId}>
            <TableCell>
              <p className="font-medium">{row.siteName}</p>
              <p className="text-xs text-muted-foreground">{siteTypeLabels[row.siteType]}</p>
            </TableCell>
            <TableCell className="text-right">
              <span className="font-mono tabular-nums">{formatNumber(row.consumptionKwh)}</span>
              <div className="mt-1.5 ml-auto h-1 w-24 rounded-full bg-muted/60">
                <div
                  className="h-full rounded-full bg-consumption"
                  style={{ width: `${(row.consumptionKwh / maxKwh) * 100}%` }}
                />
              </div>
            </TableCell>
            <TableCell className="text-right font-mono tabular-nums">{formatNumber(row.costTry)}</TableCell>
            <TableCell className="text-right font-mono tabular-nums">
              {row.consumptionKwhPerM2 === null ? (
                <span className="text-muted-foreground" title="Tesisin alanı girilmemiş">
                  —
                </span>
              ) : (
                formatNumber(row.consumptionKwhPerM2, 2)
              )}
            </TableCell>
            <TableCell className="text-right font-mono tabular-nums">
              {row.solarKwh > 0 ? formatNumber(row.solarKwh) : <span className="text-muted-foreground">—</span>}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
