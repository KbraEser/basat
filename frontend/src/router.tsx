import { createBrowserRouter } from 'react-router'
import { AppLayout } from '@/components/layout/AppLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { LivePage } from '@/pages/LivePage'
import { LoginPage } from '@/pages/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ProfilePage } from '@/pages/ProfilePage'
import { SiteDetailPage } from '@/pages/SiteDetailPage'
import { SitesPage } from '@/pages/SitesPage'

export const router = createBrowserRouter([
  // Login has no menu, so it sits outside the layout.
  { path: '/login', element: <LoginPage /> },
  {
    element: <AppLayout />,
    children: [
      { path: '/', element: <DashboardPage />, handle: { title: 'Genel bakış' } },
      { path: '/live', element: <LivePage />, handle: { title: 'Canlı tüketim' } },
      { path: '/sites', element: <SitesPage />, handle: { title: 'Tesisler' } },
      { path: '/sites/:siteId', element: <SiteDetailPage />, handle: { title: 'Tesis detayı' } },
      { path: '/profile', element: <ProfilePage />, handle: { title: 'Profil' } },
      { path: '*', element: <NotFoundPage />, handle: { title: 'Sayfa bulunamadı' } },
    ],
  },
])