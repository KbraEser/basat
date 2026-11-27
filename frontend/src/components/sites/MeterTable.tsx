import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { StatusPill } from '@/components/common/StatusPill'
import { cn } from '@/lib/utils'
import { meterTypeLabels, type Meter } from '@/types/meter'

const dateLabel = new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' })

interface MeterTableProps {
  meters: Meter[]
  className?: string
}

/** Meters of a site with their serial numbers; the serial number is what the meter sends its readings with. */
export function MeterTable({ meters, className }: MeterTableProps) {
  if (meters.length === 0) {
    return <p className={cn('py-8 text-center text-sm text-muted-foreground', className)}>Bu tesiste henüz sayaç yok.</p>
  }

  return (
    <Table className={className}>
      <TableHeader>
        <TableRow className="hover:bg-transparent">
          <TableHead>Sayaç</TableHead>
          <TableHead>Seri no</TableHead>
          <TableHead>Tür</TableHead>
          <TableHead className="text-right">Eklenme</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {meters.map((meter) => (
          <TableRow key={meter.id}>
            <TableCell className="font-medium">{meter.name}</TableCell>
            <TableCell className="font-mono text-xs">{meter.serialNumber}</TableCell>
            <TableCell>
              {/* amber for solar, the same colour as its line on the chart */}
              <StatusPill status={meter.type === 'SOLAR' ? 'warning' : 'neutral'}>{meterTypeLabels[meter.type]}</StatusPill>
            </TableCell>
            <TableCell className="text-right text-muted-foreground">{dateLabel.format(new Date(meter.createdAt))}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
