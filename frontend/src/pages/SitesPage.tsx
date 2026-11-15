import { Link } from 'react-router'
import { PagePlaceholder } from './PagePlaceholder'

export function SitesPage() {
  return (
    <>
      <PagePlaceholder title="Tesisler" description="Tesis listesi, ekleme ve düzenleme burada olacak." />
      <Link to="/sites/demo" className="px-6 underline">Örnek tesis detayına git</Link>
    </>
  )
}