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
