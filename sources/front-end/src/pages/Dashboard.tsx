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
import {
  ApiError,
  gamesApi,
  syncApi,
  type GameDto,
  type GlobalStatsDto,
  type SyncStatus,
} from '@/lib/api'
import { ChevronRight, RefreshCw, Search, Star, Trophy } from 'lucide-react'

function toHours(minutes: number) {
  return Math.round(minutes / 60)
}

type SortKey = 'playtime' | 'completion' | 'achievements' | 'name'

const SORT_OPTIONS: { key: SortKey; labelKey: string }[] = [
  { key: 'playtime', labelKey: 'dashboard.sort.playtime' },
  { key: 'completion', labelKey: 'dashboard.sort.completion' },
  { key: 'achievements', labelKey: 'dashboard.sort.achievements' },
  { key: 'name', labelKey: 'dashboard.sort.name' },
]

function compareGames(a: GameDto, b: GameDto, sortBy: SortKey): number {
  switch (sortBy) {
    case 'name':
      return a.name.localeCompare(b.name)
    case 'completion':
      return b.completionPercent - a.completionPercent
    case 'achievements':
      return b.unlockedAchievements - a.unlockedAchievements
    case 'playtime':
    default:
      return b.playtimeMinutes - a.playtimeMinutes
  }
}

function Dashboard() {
  const { t } = useTranslation()
  const { user, logout } = useAuth()
  const [games, setGames] = useState<GameDto[]>([])
  const [stats, setStats] = useState<GlobalStatsDto | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSyncing, setIsSyncing] = useState(false)
  const [syncStatus, setSyncStatus] = useState<SyncStatus | null>(null)
  const [search, setSearch] = useState('')
  const [sortBy, setSortBy] = useState<SortKey>('playtime')

  const visibleGames = useMemo(() => {
    const query = search.trim().toLowerCase()
    const filtered = query ? games.filter((game) => game.name.toLowerCase().includes(query)) : games
    return [...filtered].sort((a, b) => compareGames(a, b, sortBy))
  }, [games, search, sortBy])

  const loadData = async () => {
    setError(null)
    setIsLoading(true)
    try {
      const [gamesRes, statsRes] = await Promise.all([gamesApi.list(), gamesApi.stats()])
      setGames(gamesRes)
      setStats(statsRes)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('dashboard.loadError'))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Poll the background sync (e.g. the one triggered on first Steam login) so the
  // library refreshes on its own and the user gets feedback while it runs.
  useEffect(() => {
    let cancelled = false
    let timer: ReturnType<typeof setTimeout> | undefined
    let wasRunning = false

    const poll = async () => {
      try {
        const { status } = await syncApi.status()
        if (cancelled) return
        setSyncStatus(status)
        if (status === 'RUNNING') {
          wasRunning = true
          timer = setTimeout(poll, 2500)
        } else if (wasRunning) {
          await loadData()
        }
      } catch {
        // transient status errors are non-fatal — stop polling silently
      }
    }
    poll()

    return () => {
      cancelled = true
      if (timer) clearTimeout(timer)
    }
  }, [])

  const handleSync = async () => {
    setIsSyncing(true)
    setError(null)
    try {
      await syncApi.sync()
      await loadData()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('dashboard.syncError'))
    } finally {
      setIsSyncing(false)
    }
  }

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-5xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <Link
            to="/"
            className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground transition-colors hover:text-foreground"
          >
            <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />
            {t('common.appName')}
          </Link>
          <div className="flex items-center gap-2">
            <Link
              to="/profile"
              className="mr-1 hidden max-w-40 truncate font-mono text-xs text-muted-foreground transition-colors hover:text-foreground sm:inline"
            >
              {user?.displayName ?? user?.email}
            </Link>
            <Button
              size="sm"
              variant="ghost"
              className="font-mono text-xs"
              render={<Link to="/guides" />}
            >
              {t('common.guides')}
            </Button>
            <LanguageSwitcher className="mr-1" />
            <Button
              size="sm"
              variant="outline"
              onClick={handleSync}
              disabled={isSyncing}
              className="gap-1.5 font-mono text-xs"
            >
              <RefreshCw className={isSyncing ? 'size-3.5 animate-spin' : 'size-3.5'} />
              {isSyncing ? t('dashboard.syncing') : t('dashboard.sync')}
            </Button>
            <Button variant="ghost" size="sm" className="font-mono text-xs" onClick={logout}>
              {t('common.logout')}
            </Button>
          </div>
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          {syncStatus === 'RUNNING' && (
            <Card className="flex-row items-center gap-2.5 border-amber-400/30 bg-amber-400/5 p-4 text-sm text-amber-400">
              <RefreshCw className="size-4 animate-spin" />
              {t('dashboard.syncStatus.running')}
            </Card>
          )}
          {syncStatus === 'PRIVATE' && games.length === 0 && (
            <Card className="border-amber-400/30 bg-amber-400/5 p-4 text-sm text-amber-400">
              {t('dashboard.syncStatus.private')}
            </Card>
          )}
          {(syncStatus === 'FAILED' || syncStatus === 'RATE_LIMITED') && games.length === 0 && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {t('dashboard.syncStatus.failed')}
            </Card>
          )}

          <dl className="grid grid-cols-2 divide-x divide-border border-y border-border font-mono sm:grid-cols-4">
            {[
              [t('dashboard.stats.gamesTracked'), stats ? String(stats.totalGames) : '—'],
              [
                t('dashboard.stats.playtimeLogged'),
                stats ? `${toHours(stats.totalPlaytimeMinutes)}h` : '—',
              ],
              [
                t('dashboard.stats.achievements'),
                stats ? `${stats.unlockedAchievements}/${stats.totalAchievements}` : '—',
              ],
              [
                t('dashboard.stats.completion'),
                stats ? `${Math.round(stats.globalCompletionPercent)}%` : '—',
              ],
            ].map(([label, value]) => (
              <div key={label} className="px-4 py-4 first:pl-0">
                <dt className="text-2xl font-medium text-foreground">{value}</dt>
                <dd className="mt-1 text-xs text-muted-foreground uppercase">{label}</dd>
              </div>
            ))}
          </dl>

          {stats?.mostPlayedGame && (
            <Link to={`/dashboard/${stats.mostPlayedGame.appId}`} className="group block">
              <Card className="flex-row items-stretch gap-0 overflow-hidden rounded-md border-border p-0 transition-colors group-hover:border-amber-400/40">
                <GameCover
                  appId={stats.mostPlayedGame.appId}
                  name={stats.mostPlayedGame.name}
                  className="hidden aspect-460/215 w-44 shrink-0 sm:block"
                />
                <div className="flex min-w-0 flex-1 flex-col justify-center gap-1 p-4">
                  <span className="flex items-center gap-1.5 font-mono text-[11px] tracking-widest text-amber-400 uppercase">
                    <Star className="size-3 fill-amber-400" />
                    {t('dashboard.mostPlayed')}
                  </span>
                  <h3 className="truncate text-lg font-medium">{stats.mostPlayedGame.name}</h3>
                  <p className="font-mono text-xs text-muted-foreground">
                    {t('dashboard.playtimePlayed', {
                      hours: toHours(stats.mostPlayedGame.playtimeMinutes),
                    })}
                    {stats.mostPlayedGame.totalAchievements > 0 &&
                      ` · ${stats.mostPlayedGame.unlockedAchievements}/${stats.mostPlayedGame.totalAchievements} ${t('dashboard.stats.achievements')}`}
                  </p>
                </div>
                <ChevronRight className="mr-4 size-5 shrink-0 self-center text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:text-amber-400" />
              </Card>
            </Link>
          )}

          <section className="flex flex-col gap-3">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <h2 className="font-mono text-xs tracking-widest text-muted-foreground uppercase">
                {t('dashboard.library')}
                {games.length > 0 && (
                  <span className="ml-2 text-muted-foreground/60">{visibleGames.length}</span>
                )}
              </h2>
              {games.length > 0 && (
                <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                  <div className="relative">
                    <Search className="pointer-events-none absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2 text-muted-foreground" />
                    <Input
                      type="search"
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      placeholder={t('dashboard.searchPlaceholder')}
                      className="h-8 pl-8 font-mono text-xs sm:w-56"
                    />
                  </div>
                  <div className="flex items-center gap-0.5 rounded-lg border border-border p-0.5 font-mono text-xs">
                    {SORT_OPTIONS.map((option) => (
                      <button
                        key={option.key}
                        type="button"
                        onClick={() => setSortBy(option.key)}
                        className={cn(
                          'rounded-md px-2 py-1 whitespace-nowrap transition-colors',
                          sortBy === option.key
                            ? 'bg-amber-400 text-neutral-950'
                            : 'text-muted-foreground hover:text-foreground',
                        )}
                      >
                        {t(option.labelKey)}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
            {isLoading && <p className="text-sm text-muted-foreground">{t('dashboard.loading')}</p>}
            {!isLoading && games.length === 0 && (
              <p className="text-sm text-muted-foreground">{t('dashboard.empty')}</p>
            )}
            {!isLoading && games.length > 0 && visibleGames.length === 0 && (
              <p className="text-sm text-muted-foreground">{t('dashboard.noResults')}</p>
            )}
            <div className="grid gap-3 sm:grid-cols-2">
              {visibleGames.map((game) => (
                <Link key={game.appId} to={`/dashboard/${game.appId}`} className="group block">
                  <Card className="gap-0 overflow-hidden rounded-md border-border bg-card p-0 transition-colors group-hover:border-amber-400/40">
                    <GameCover
                      appId={game.appId}
                      name={game.name}
                      className="aspect-460/215 w-full"
                    />
                    <div className="flex flex-col gap-2 p-4">
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="text-sm font-medium">{game.name}</h3>
                        <ChevronRight className="size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:text-amber-400" />
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {t('dashboard.playtimePlayed', { hours: toHours(game.playtimeMinutes) })}
                      </p>
                      {game.totalAchievements > 0 && (
                        <div className="mt-1 flex flex-col gap-1.5">
                          <div className="flex items-center justify-between font-mono text-[11px] text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Trophy className="size-3 text-amber-400/70" />
                              {game.unlockedAchievements}/{game.totalAchievements}
                            </span>
                            <span>{Math.round(game.completionPercent)}%</span>
                          </div>
                          <div className="h-1 overflow-hidden rounded-full bg-border">
                            <div
                              className="h-full rounded-full bg-amber-400"
                              style={{ width: `${game.completionPercent}%` }}
                            />
                          </div>
                        </div>
                      )}
                    </div>
                  </Card>
                </Link>
              ))}
            </div>
          </section>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>{t('common.appName')}</span>
          <Link to="/profile" className="transition-colors hover:text-foreground">
            {user?.steamId
              ? t('dashboard.steamIdLinked', { steamId: user.steamId })
              : t('dashboard.steamIdNone')}
          </Link>
        </footer>
      </div>
    </div>
  )
}

export default Dashboard
