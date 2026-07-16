import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import LanguageSwitcher from '@/components/LanguageSwitcher'
import GameCover from '@/components/GameCover'
import { ApiError, guidesApi, type GuideDetailDto } from '@/lib/api'
import { ArrowLeft, Check, Lock, Pencil, Trash2, Trophy } from 'lucide-react'

const GuideDetail = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [guide, setGuide] = useState<GuideDetailDto | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    setIsLoading(true)
    guidesApi
      .get(Number(id))
      .then((data) => {
        if (!cancelled) setGuide(data)
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
  }, [id, t])

  const handleDelete = async () => {
    if (!guide) return
    setIsDeleting(true)
    try {
      await guidesApi.remove(guide.id)
      navigate('/guides')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('guides.deleteError'))
      setIsDeleting(false)
    }
  }

  const progressPercent =
    guide && guide.linkedAchievements > 0
      ? (guide.unlockedAchievements / guide.linkedAchievements) * 100
      : 0

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-3xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <Link
            to="/guides"
            className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" />
            {t('guides.back')}
          </Link>
          <div className="flex items-center gap-2">
            <LanguageSwitcher className="mr-1" />
            {guide?.isAuthor && (
              <>
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-1.5 font-mono text-xs"
                  render={<Link to={`/guides/${guide.id}/edit`} />}
                >
                  <Pencil className="size-3.5" />
                  {t('guides.edit')}
                </Button>
                <Button
                  size="sm"
                  variant={confirmDelete ? 'destructive' : 'ghost'}
                  disabled={isDeleting}
                  className="gap-1.5 font-mono text-xs"
                  onClick={() => (confirmDelete ? handleDelete() : setConfirmDelete(true))}
                >
                  <Trash2 className="size-3.5" />
                  {confirmDelete ? t('guides.confirmDelete') : t('guides.delete')}
                </Button>
              </>
            )}
          </div>
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          {isLoading && <p className="text-sm text-muted-foreground">{t('guides.loading')}</p>}

          {guide && (
            <>
              <section className="flex flex-col gap-4 sm:flex-row sm:items-end">
                <GameCover
                  appId={guide.appId}
                  name={guide.gameName}
                  className="aspect-460/215 w-full shrink-0 rounded-md sm:w-64"
                />
                <div className="flex flex-1 flex-col gap-2">
                  <p className="font-mono text-xs text-muted-foreground">{guide.gameName}</p>
                  <h1 className="text-3xl font-semibold tracking-tight">{guide.title}</h1>
                  <p className="font-mono text-[11px] text-muted-foreground/70">
                    {t('guides.byAuthor', { author: guide.authorName ?? t('common.anonymous') })}
                  </p>
                  {guide.linkedAchievements > 0 && (
                    <div className="mt-1 flex flex-col gap-1.5">
                      <div className="flex items-center justify-between font-mono text-xs text-muted-foreground uppercase">
                        <span className="flex items-center gap-1.5">
                          <Trophy className="size-3.5 text-amber-400" />
                          {t('guides.progress', {
                            unlocked: guide.unlockedAchievements,
                            total: guide.linkedAchievements,
                          })}
                        </span>
                        <span>{Math.round(progressPercent)}%</span>
                      </div>
                      <div className="h-1.5 overflow-hidden rounded-full bg-border">
                        <div
                          className="h-full rounded-full bg-amber-400"
                          style={{ width: `${progressPercent}%` }}
                        />
                      </div>
                    </div>
                  )}
                </div>
              </section>

              {guide.description && (
                <p className="text-sm whitespace-pre-wrap text-muted-foreground">
                  {guide.description}
                </p>
              )}

              <section className="flex flex-col gap-4">
                <h2 className="font-mono text-xs tracking-widest text-muted-foreground uppercase">
                  {t('guides.steps')}
                </h2>
                <ol className="flex flex-col gap-3">
                  {guide.steps.map((step, index) => {
                    const stepDone =
                      step.achievements.length > 0 && step.achievements.every((a) => a.unlocked)
                    return (
                      <Card key={step.id} className="gap-3 rounded-md border-border bg-card p-4">
                        <div className="flex items-start gap-3">
                          <span
                            className={`flex size-6 shrink-0 items-center justify-center rounded-full font-mono text-xs ${
                              stepDone
                                ? 'bg-amber-400 text-neutral-950'
                                : 'bg-border text-muted-foreground'
                            }`}
                          >
                            {stepDone ? <Check className="size-3.5" /> : index + 1}
                          </span>
                          <div className="flex flex-1 flex-col gap-1">
                            <h3 className="text-sm font-medium">{step.title}</h3>
                            {step.content && (
                              <p className="text-sm whitespace-pre-wrap text-muted-foreground">
                                {step.content}
                              </p>
                            )}
                          </div>
                        </div>
                        {step.achievements.length > 0 && (
                          <div className="ml-9 flex flex-col gap-1.5">
                            {step.achievements.map((ach) => (
                              <div key={ach.apiName} className="flex items-center gap-2">
                                {ach.iconUrl ? (
                                  <img
                                    src={ach.iconUrl}
                                    alt=""
                                    className={`size-6 rounded-sm ${ach.unlocked ? '' : 'opacity-40 grayscale'}`}
                                  />
                                ) : (
                                  <div className="flex size-6 items-center justify-center rounded-sm bg-muted">
                                    <Trophy className="size-3 text-muted-foreground" />
                                  </div>
                                )}
                                <span
                                  className={`flex-1 text-xs ${
                                    ach.unlocked ? 'text-foreground' : 'text-muted-foreground'
                                  }`}
                                >
                                  {ach.displayName ?? ach.apiName}
                                </span>
                                {ach.unlocked ? (
                                  <Check className="size-3.5 text-amber-400" />
                                ) : (
                                  <Lock className="size-3 text-muted-foreground" />
                                )}
                              </div>
                            ))}
                          </div>
                        )}
                      </Card>
                    )
                  })}
                </ol>
              </section>
            </>
          )}
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>{t('common.appName')}</span>
        </footer>
      </div>
    </div>
  )
}

export default GuideDetail
