import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Card } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import LanguageSwitcher from '@/components/LanguageSwitcher'
import GameCover from '@/components/GameCover'
import { useAuth } from '@/lib/auth'
import { ApiError, gamesApi, type AchievementDto, type GameDto } from '@/lib/api'
import { ArrowLeft, Lock, Trophy } from 'lucide-react'

function formatDate(value: string, locale: string) {
  return new Date(value).toLocaleDateString(locale, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function GameDetail() {
  const { appId } = useParams<{ appId: string }>()
  const { t, i18n } = useTranslation()
  const { user } = useAuth()
  const [game, setGame] = useState<GameDto | null>(null)
  const [achievements, setAchievements] = useState<AchievementDto[]>([])
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    if (!appId) return
    const id = Number(appId)
    let cancelled = false

    const load = async () => {
      setError(null)
      setIsLoading(true)
      try {
        const [games, achievementsRes] = await Promise.all([
          gamesApi.list(),
          gamesApi.achievements(id),
        ])
        if (cancelled) return
        setGame(games.find((g) => g.appId === id) ?? null)
        setAchievements(achievementsRes)
      } catch (err) {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : t('gameDetail.loadError'))
      } finally {
        if (!cancelled) setIsLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [appId, t])

  const unlockedCount = achievements.filter((a) => a.unlocked).length
  const sorted = [...achievements].sort((a, b) => Number(b.unlocked) - Number(a.unlocked))

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-5xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" />
            {t('gameDetail.back')}
          </Link>
          <div className="flex items-center gap-2">
            <LanguageSwitcher />
            {user?.email && (
              <span className="font-mono text-xs text-muted-foreground">{user.email}</span>
            )}
          </div>
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          <section className="flex flex-col gap-4 sm:flex-row sm:items-end">
            {appId && (
              <GameCover
                appId={Number(appId)}
                name={game?.name ?? ''}
                className="aspect-460/215 w-full shrink-0 rounded-md sm:w-64"
              />
            )}
            <div className="flex flex-1 flex-col gap-2">
              <h1 className="text-3xl font-semibold tracking-tight">
                {game?.name ?? t('gameDetail.game')}
              </h1>
              {achievements.length > 0 && (
                <div className="flex flex-col gap-2">
                  <div className="flex items-center justify-between font-mono text-xs text-muted-foreground uppercase">
                    <span className="flex items-center gap-1.5">
                      <Trophy className="size-3.5 text-amber-400" />
                      {t('gameDetail.progress', {
                        unlocked: unlockedCount,
                        total: achievements.length,
                      })}
                    </span>
                    <span>{Math.round((unlockedCount / achievements.length) * 100)}%</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-border">
                    <div
                      className="h-full rounded-full bg-amber-400"
                      style={{ width: `${(unlockedCount / achievements.length) * 100}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          </section>

          <section className="flex flex-col gap-3">
            <h2 className="font-mono text-xs tracking-widest text-muted-foreground uppercase">
              {t('gameDetail.achievements')}
            </h2>
            {isLoading && (
              <p className="text-sm text-muted-foreground">{t('gameDetail.loading')}</p>
            )}
            {!isLoading && achievements.length === 0 && !error && (
              <p className="text-sm text-muted-foreground">{t('gameDetail.empty')}</p>
            )}
            <div className="grid gap-2 sm:grid-cols-2">
              {sorted.map((ach) => (
                <Card
                  key={ach.apiName}
                  className={`flex-row items-center gap-3 rounded-md border-border p-3 ${
                    ach.unlocked ? 'bg-card' : 'bg-card/40'
                  }`}
                >
                  <div className="relative shrink-0">
                    {ach.iconUrl ? (
                      <img
                        src={ach.iconUrl}
                        alt=""
                        className={`size-10 rounded-sm ${ach.unlocked ? '' : 'opacity-40 grayscale'}`}
                      />
                    ) : (
                      <div className="flex size-10 items-center justify-center rounded-sm bg-muted">
                        <Trophy className="size-4 text-muted-foreground" />
                      </div>
                    )}
                    {!ach.unlocked && (
                      <div className="absolute inset-0 flex items-center justify-center rounded-sm bg-background/40">
                        <Lock className="size-3.5 text-muted-foreground" />
                      </div>
                    )}
                  </div>
                  <div className="flex min-w-0 flex-col gap-0.5">
                    <h3
                      className={`truncate text-sm font-medium ${
                        ach.unlocked ? 'text-foreground' : 'text-muted-foreground'
                      }`}
                    >
                      {ach.displayName}
                    </h3>
                    {ach.description && (
                      <p className="truncate text-xs text-muted-foreground">{ach.description}</p>
                    )}
                    {ach.unlocked && ach.unlockedAt && (
                      <p className="font-mono text-[11px] text-amber-400/70">
                        {formatDate(ach.unlockedAt, i18n.language)}
                      </p>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          </section>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>{t('common.appName')}</span>
        </footer>
      </div>
    </div>
  )
}

export default GameDetail
