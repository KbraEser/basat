import { Skeleton } from '@/components/ui/skeleton'

/** Grey placeholder in the shape of the app (menu, header, cards) while the signed-in user is loading. */
export function AppLayoutSkeleton() {
  return (
    <div className="flex min-h-svh" aria-busy="true" aria-label="Yükleniyor">
      {/* sidebar: logo, menu items, user at the bottom */}
      <aside className="hidden w-64 shrink-0 flex-col gap-6 p-4 md:flex">
        <div className="flex items-center gap-3">
          <Skeleton className="size-8 rounded-lg" />
          <div className="grid flex-1 gap-1.5">
            <Skeleton className="h-3.5 w-20" />
            <Skeleton className="h-3 w-28" />
          </div>
        </div>
        <div className="grid gap-2">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} className="h-8 w-full" />
          ))}
        </div>
        <div className="mt-auto flex items-center gap-3">
          <Skeleton className="size-8 rounded-lg" />
          <div className="grid flex-1 gap-1.5">
            <Skeleton className="h-3.5 w-24" />
            <Skeleton className="h-3 w-32" />
          </div>
        </div>
      </aside>

      <main className="flex flex-1 flex-col">
        {/* header bar */}
        <div className="flex h-14 items-center gap-3 border-b px-4">
          <Skeleton className="size-6" />
          <Skeleton className="h-4 w-28" />
        </div>
        {/* page title and cards */}
        <div className="flex flex-col gap-6 p-6">
          <div className="grid gap-2">
            <Skeleton className="h-7 w-48" />
            <Skeleton className="h-4 w-64" />
          </div>
          <Skeleton className="h-36 w-full rounded-xl" />
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {Array.from({ length: 3 }, (_, i) => (
              <Skeleton key={i} className="h-44 rounded-xl" />
            ))}
          </div>
        </div>
      </main>
    </div>
  )
}
