import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import LanguageSwitcher from '@/components/LanguageSwitcher'
import GameCover from '@/components/GameCover'
import { useAuth } from '@/lib/auth'
import { cn } from '@/lib/utils'
import { ApiError, guidesApi, type GuideSummaryDto } from '@/lib/api'
import { ArrowLeft, ListChecks, Plus, Search, Trophy } from 'lucide-react'

type Scope = 'all' | 'mine'

const Guides = () => {
  const { t } = useTranslation()
  const { isAuthenticated } = useAuth()
  const [guides, setGuides] = useState<GuideSummaryDto[]>([])
  const [scope, setScope] = useState<Scope>('all')
  const [search, setSearch] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setError(null)
    const fetcher = scope === 'mine' ? guidesApi.mine() : guidesApi.list()
    fetcher
      .then((data) => {
        if (!cancelled) setGuides(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : t('guides.loadError'))
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [t, scope])

  const visibleGuides = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) return guides
    return guides.filter(
      (guide) =>
        guide.title.toLowerCase().includes(query) || guide.gameName.toLowerCase().includes(query),
    )
  }, [guides, search])

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-5xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <Link
            to="/"
            className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" />
            {t('common.appName')}
          </Link>
          <div className="flex items-center gap-2">
            <LanguageSwitcher className="mr-1" />
            {isAuthenticated && (
              <Button
                size="sm"
                className="gap-1.5 bg-amber-400 font-mono text-xs text-neutral-950 hover:bg-amber-300"
                render={<Link to="/guides/new" />}
              >
                <Plus className="size-3.5" />
                {t('guides.new')}
              </Button>
            )}
          </div>
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          <div className="flex flex-col gap-1">
            <h1 className="text-3xl font-semibold tracking-tight">{t('guides.title')}</h1>
            <p className="text-sm text-muted-foreground">{t('guides.subtitle')}</p>
          </div>

          {isAuthenticated && (
            <div className="flex items-center gap-0.5 self-start rounded-lg border border-border p-0.5 font-mono text-xs">
              {(['all', 'mine'] as const).map((value) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setScope(value)}
                  className={cn(
                    'rounded-md px-3 py-1 transition-colors',
                    scope === value
                      ? 'bg-amber-400 text-neutral-950'
                      : 'text-muted-foreground hover:text-foreground',
                  )}
                >
                  {value === 'all' ? t('guides.scopeAll') : t('guides.scopeMine')}
                </button>
              ))}
            </div>
          )}

          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          <div className="relative sm:max-w-sm">
            <Search className="pointer-events-none absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={t('guides.searchPlaceholder')}
              className="h-8 pl-8 font-mono text-xs"
            />
          </div>

          {isLoading && <p className="text-sm text-muted-foreground">{t('guides.loading')}</p>}
          {!isLoading && guides.length === 0 && (
            <p className="text-sm text-muted-foreground">{t('guides.empty')}</p>
          )}
          {!isLoading && guides.length > 0 && visibleGuides.length === 0 && (
            <p className="text-sm text-muted-foreground">{t('guides.noResults')}</p>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            {visibleGuides.map((guide) => (
              <Link key={guide.id} to={`/guides/${guide.id}`} className="group block">
                <Card className="gap-0 overflow-hidden rounded-md border-border bg-card p-0 transition-colors group-hover:border-amber-400/40">
                  <GameCover
                    appId={guide.appId}
                    name={guide.gameName}
                    className="aspect-460/215 w-full"
                  />
                  <div className="flex flex-col gap-2 p-4">
                    <div className="flex flex-col gap-0.5">
                      <h3 className="text-sm font-medium">{guide.title}</h3>
                      <p className="font-mono text-xs text-muted-foreground">{guide.gameName}</p>
                    </div>
                    <div className="flex items-center gap-4 font-mono text-[11px] text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <ListChecks className="size-3 text-amber-400/70" />
                        {t('guides.stepCount', { count: guide.stepCount })}
                      </span>
                      <span className="flex items-center gap-1">
                        <Trophy className="size-3 text-amber-400/70" />
                        {guide.achievementCount}
                      </span>
                    </div>
                    <p className="font-mono text-[11px] text-muted-foreground/70">
                      {t('guides.byAuthor', { author: guide.authorEmail })}
                    </p>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>{t('common.appName')}</span>
        </footer>
      </div>
    </div>
  )
}

export default Guides
