import { createBrowserRouter } from 'react-router'
import { DashboardPage } from '@/pages/DashboardPage'
import { LoginPage } from '@/pages/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ProfilePage } from '@/pages/ProfilePage'
import { SiteDetailPage } from '@/pages/SiteDetailPage'
import { SitesPage } from '@/pages/SitesPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/', element: <DashboardPage /> },
  { path: '/sites', element: <SitesPage /> },
  { path: '/sites/:siteId', element: <SiteDetailPage /> },
  { path: '/profile', element: <ProfilePage /> },
  { path: '*', element: <NotFoundPage /> },
])