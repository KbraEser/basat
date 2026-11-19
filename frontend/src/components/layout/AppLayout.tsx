import { Outlet, useMatches } from 'react-router'
import { DampedWaves, type Wave } from '@/components/common/DampedWaves'
import { Separator } from '@/components/ui/separator'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { AppSidebar, type SidebarUser } from './AppSidebar'

// Placeholder until the logged-in user comes from GET /api/users/me.
const demoUser: SidebarUser = {
  fullName: 'Ayşe Deniz',
  email: 'ayse@deniz.com',
  companyName: 'Deniz Otelcilik',
}

// Three quiet waves rising out of the bottom-left corner and dying out.
const cornerWaves: Wave[] = [
  { x: -20, y: 900, direction: 'right', length: 900, amplitude: 38, wavelength: 240, phase: 0, slope: -0.35, duration: 6, delay: 0 },
  { x: -20, y: 860, direction: 'right', length: 760, amplitude: 26, wavelength: 200, phase: 1.7, slope: -0.42, duration: 7, delay: -2.5 },
  { x: -20, y: 930, direction: 'right', length: 1050, amplitude: 30, wavelength: 280, phase: 3, slope: -0.25, duration: 8, delay: -5 },
]

interface RouteHandle {
  title?: string
}

export function AppLayout() {
  const matches = useMatches()
  const title = (matches.at(-1)?.handle as RouteHandle | undefined)?.title

  return (
    // shadcn paints opaque backgrounds here; transparent lets the glows on <body> show through
    <SidebarProvider className="has-data-[variant=inset]:bg-transparent">
      <DampedWaves waves={cornerWaves} intensity={0.55} className="pointer-events-none fixed inset-0 -z-10 size-full" />
      <AppSidebar user={demoUser} />
      <SidebarInset className="bg-transparent">
        <header className="flex h-14 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="mr-2 h-4" />
          <span className="font-medium">{title}</span>
        </header>
        <div className="flex flex-1 flex-col">
          <Outlet />
        </div>
      </SidebarInset>
    </SidebarProvider>
  )
}