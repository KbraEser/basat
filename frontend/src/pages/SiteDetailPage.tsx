import { useParams } from 'react-router'
import { PagePlaceholder } from './PagePlaceholder'

export function SiteDetailPage() {
  const { siteId } = useParams()
  return <PagePlaceholder title="Tesis detayı" description={`Tesis ${siteId}: sayaçlar ve tüketim grafiği burada olacak.`} />
}