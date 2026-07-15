import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuth } from '@/lib/auth'
import { ApiError, gamesApi, syncApi, type GameDto, type GlobalStatsDto } from '@/lib/api'
import { RefreshCw } from 'lucide-react'

function formatPlaytime(minutes: number) {
  return `${Math.round(minutes / 60)}h`
}

function Dashboard() {
  const { user, logout } = useAuth()
  const [games, setGames] = useState<GameDto[]>([])
  const [stats, setStats] = useState<GlobalStatsDto | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSyncing, setIsSyncing] = useState(false)

  const loadData = async () => {
    setError(null)
    setIsLoading(true)
    try {
      const [gamesRes, statsRes] = await Promise.all([gamesApi.list(), gamesApi.stats()])
      setGames(gamesRes)
      setStats(statsRes)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Impossible de charger tes données.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleSync = async () => {
    setIsSyncing(true)
    setError(null)
    try {
      await syncApi.sync()
      await loadData()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'La synchronisation a échoué.')
    } finally {
      setIsSyncing(false)
    }
  }

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-5xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <div className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />
            {user?.email}
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={handleSync}
              disabled={isSyncing}
              className="gap-1.5 font-mono text-xs"
            >
              <RefreshCw className={isSyncing ? 'size-3.5 animate-spin' : 'size-3.5'} />
              {isSyncing ? 'Synchro…' : 'Synchroniser'}
            </Button>
            <Button variant="ghost" size="sm" className="font-mono text-xs" onClick={logout}>
              Déconnexion
            </Button>
          </div>
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          <dl className="grid grid-cols-2 divide-x divide-border border-y border-border font-mono sm:grid-cols-3">
            {[
              ['jeux suivis', stats ? String(stats.gamesTracked) : '—'],
              ['temps loggé', stats ? formatPlaytime(stats.totalPlaytimeMinutes) : '—'],
            ].map(([label, value]) => (
              <div key={label} className="px-4 py-4 first:pl-0">
                <dt className="text-2xl font-medium text-foreground">{value}</dt>
                <dd className="mt-1 text-xs text-muted-foreground uppercase">{label}</dd>
              </div>
            ))}
          </dl>

          <section className="flex flex-col gap-3">
            <h2 className="font-mono text-xs tracking-widest text-muted-foreground uppercase">
              Ta bibliothèque
            </h2>
            {isLoading && <p className="text-sm text-muted-foreground">Chargement…</p>}
            {!isLoading && games.length === 0 && (
              <p className="text-sm text-muted-foreground">
                Aucun jeu synchronisé pour l'instant. Lance une synchronisation.
              </p>
            )}
            <div className="grid gap-3 sm:grid-cols-2">
              {games.map((game) => (
                <Card key={game.appId} className="gap-1 rounded-md border-border bg-card p-4">
                  <h3 className="text-sm font-medium">{game.name}</h3>
                  <p className="text-sm text-muted-foreground">
                    {formatPlaytime(game.playtimeMinutes)} joué
                  </p>
                </Card>
              ))}
            </div>
          </section>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>steam-tracker</span>
          <span>{user?.steamId ? `steam id: ${user.steamId}` : 'aucun steam id lié'}</span>
        </footer>
      </div>
    </div>
  )
}

export default Dashboard
