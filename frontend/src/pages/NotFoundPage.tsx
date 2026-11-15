import { Link } from 'react-router'
import { PagePlaceholder } from './PagePlaceholder'

export function NotFoundPage() {
  return (
    <>
      <PagePlaceholder title="Sayfa bulunamadı" description="Aradığın adres yok ya da taşınmış." />
      <Link to="/" className="px-6 underline">Genel bakışa dön</Link>
    </>
  )
}