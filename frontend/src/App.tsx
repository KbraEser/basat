import { Zap } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

// Temporary start page: proves Tailwind, shadcn and the theme colours work. Replaced by the router next.
export default function App() {
  return (
    <main className="flex min-h-svh items-center justify-center bg-muted/40 p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="flex items-center gap-2">
            <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Zap className="size-4" aria-hidden="true" />
            </span>
            <CardTitle>Basat</CardTitle>
          </div>
          <CardDescription>Frontend kurulumu çalışıyor.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap items-center gap-2">
          <Badge className="bg-tariff-day text-foreground">Gündüz</Badge>
          <Badge className="bg-tariff-peak text-white">Puant</Badge>
          <Badge className="bg-tariff-night text-white">Gece</Badge>
          <Button className="ml-auto">Devam</Button>
        </CardContent>
      </Card>
    </main>
  )
}
