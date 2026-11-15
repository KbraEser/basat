import type { LucideIcon } from 'lucide-react'
import { Activity, Bell, Building2, FileText, LayoutDashboard, Sparkles } from 'lucide-react'

export interface NavItem {
  title: string
  to: string
  icon: LucideIcon
  // Shown in the menu but not clickable until its week in the plan.
  comingSoon?: boolean
}

export interface NavGroup {
  label: string
  items: NavItem[]
}

export const navGroups: NavGroup[] = [
  {
    label: 'İzleme',
    items: [
      { title: 'Genel bakış', to: '/', icon: LayoutDashboard },
      { title: 'Canlı tüketim', to: '/live', icon: Activity },
      { title: 'Alarmlar', to: '/alerts', icon: Bell, comingSoon: true },
      { title: 'Raporlar', to: '/reports', icon: FileText, comingSoon: true },
      { title: 'Asistan', to: '/assistant', icon: Sparkles, comingSoon: true },
    ],
  },
  {
    label: 'Yönetim',
    items: [{ title: 'Tesisler', to: '/sites', icon: Building2 }],
  },
]