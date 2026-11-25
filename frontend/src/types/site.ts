// SiteType enum in monitoring-service
export type SiteType = 'OFFICE' | 'FACTORY' | 'HOTEL' | 'SCHOOL' | 'RETAIL' | 'HOSPITAL' | 'SOLAR_PLANT' | 'OTHER'

// Turkish names for the screen; the sites page will use them too.
export const siteTypeLabels: Record<SiteType, string> = {
  OFFICE: 'Ofis',
  FACTORY: 'Fabrika',
  HOTEL: 'Otel',
  SCHOOL: 'Okul',
  RETAIL: 'Mağaza',
  HOSPITAL: 'Hastane',
  SOLAR_PLANT: 'GES',
  OTHER: 'Diğer',
}


// GET /api/sites; SiteResponse in monitoring-service. Floor area and city are optional.
export interface Site {
  id: string
  name: string
  type: SiteType
  floorAreaM2: number | null
  city: string | null
  createdAt: string
}