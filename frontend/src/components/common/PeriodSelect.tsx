import type { ConsumptionPeriod } from "@/types/dashboard";
import { cn } from '@/lib/utils'


const options:{value:ConsumptionPeriod, label:string}[] = [
  { value: 'TODAY', label: 'Bugün' },
  { value: 'LAST_7_DAYS', label: '7 gün' },
  { value: 'LAST_30_DAYS', label: '30 gün' },
]

interface PeriodSelectProps {
    value: ConsumptionPeriod
    onChange: (value: ConsumptionPeriod) => void
}

/** Segmented control for the dashboard period. */
export function PeriodSelect({ value, onChange }: PeriodSelectProps) {
    return(
         <div role="radiogroup" aria-label="Dönem" className="inline-flex rounded-lg bg-muted/60 p-1">
            {options.map((option)=>{
                const selected = option.value === value
                return(
                    <button
                    key={option.value}
                    type="button"
                    role="radio"
                    aria-checked={selected}
                    onClick={()=>onChange(option.value)}
                    className={cn(
              'cursor-pointer rounded-md px-3 py-1 text-xs font-medium transition-colors',
              selected ? 'bg-white/15 text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground',
            )}
                    >
                        {option.label}
                    </button>
                )
            })}
        </div>
    )
}