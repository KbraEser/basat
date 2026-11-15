import { Outlet, useMatches } from 'react-router'
import { Separator } from '@/components/ui/separator'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { AppSidebar, type SidebarUser } from './AppSidebar'

// Placeholder until the logged-in user comes from GET /api/users/me.
const demoUser: SidebarUser = {
  fullName: 'Ayşe Deniz',
  email: 'ayse@deniz.com',
  companyName: 'Deniz Otelcilik',
}

interface RouteHandle {
  title?: string
}

export function AppLayout() {
  const matches = useMatches()
  const title = (matches.at(-1)?.handle as RouteHandle | undefined)?.title

  return (
    <SidebarProvider>
      <AppSidebar user={demoUser} />
      <SidebarInset>
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