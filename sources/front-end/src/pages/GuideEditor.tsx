import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import LanguageSwitcher from '@/components/LanguageSwitcher'
import GameCover from '@/components/GameCover'
import GameSearchSelect from '@/components/GameSearchSelect'
import AchievementPicker from '@/components/AchievementPicker'
import {
  ApiError,
  guidesApi,
  steamApi,
  type GuideRequest,
  type SteamAchievementSchemaDto,
} from '@/lib/api'
import { ArrowLeft, ChevronDown, ChevronUp, Plus, Trophy, X } from 'lucide-react'

type EditorAchievement = { apiName: string; displayName: string | null; iconUrl: string | null }
type EditorStep = { key: string; title: string; content: string; achievements: EditorAchievement[] }

const newStep = (): EditorStep => ({
  key: crypto.randomUUID(),
  title: '',
  content: '',
  achievements: [],
})

const textareaClass =
  'w-full rounded-lg border border-input bg-transparent px-2.5 py-1.5 text-sm outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30'

const GuideEditor = () => {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const { t } = useTranslation()

  const [game, setGame] = useState<{ appId: number; name: string } | null>(null)
  const [schema, setSchema] = useState<SteamAchievementSchemaDto[]>([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [steps, setSteps] = useState<EditorStep[]>([newStep()])
  const [openPickerKey, setOpenPickerKey] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(isEdit)
  const [isSaving, setIsSaving] = useState(false)

  // Load the existing guide in edit mode.
  useEffect(() => {
    if (!id) return
    let cancelled = false
    setIsLoading(true)
    guidesApi
      .get(Number(id))
      .then((guide) => {
        if (cancelled) return
        setGame({ appId: guide.appId, name: guide.gameName })
        setTitle(guide.title)
        setDescription(guide.description ?? '')
        setSteps(
          guide.steps.map((step) => ({
            key: crypto.randomUUID(),
            title: step.title,
            content: step.content ?? '',
            achievements: step.achievements.map((a) => ({
              apiName: a.apiName,
              displayName: a.displayName,
              iconUrl: a.iconUrl,
            })),
          })),
        )
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : t('guideEditor.loadError'))
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id, t])

  // Fetch the achievement schema whenever the target game changes.
  useEffect(() => {
    if (!game) {
      setSchema([])
      return
    }
    let cancelled = false
    steamApi
      .achievementSchema(game.appId)
      .then((data) => {
        if (!cancelled) setSchema(data)
      })
      .catch(() => {
        if (!cancelled) setSchema([])
      })
    return () => {
      cancelled = true
    }
  }, [game])

  const updateStep = (key: string, patch: Partial<EditorStep>) => {
    setSteps((prev) => prev.map((step) => (step.key === key ? { ...step, ...patch } : step)))
  }

  const toggleAchievement = (key: string, achievement: SteamAchievementSchemaDto) => {
    setSteps((prev) =>
      prev.map((step) => {
        if (step.key !== key) return step
        const exists = step.achievements.some((a) => a.apiName === achievement.apiName)
        return {
          ...step,
          achievements: exists
            ? step.achievements.filter((a) => a.apiName !== achievement.apiName)
            : [
                ...step.achievements,
                {
                  apiName: achievement.apiName,
                  displayName: achievement.displayName,
                  iconUrl: achievement.iconUrl,
                },
              ],
        }
      }),
    )
  }

  const moveStep = (index: number, direction: -1 | 1) => {
    setSteps((prev) => {
      const target = index + direction
      if (target < 0 || target >= prev.length) return prev
      const next = [...prev]
      ;[next[index], next[target]] = [next[target], next[index]]
      return next
    })
  }

  const removeStep = (key: string) => {
    setSteps((prev) => (prev.length > 1 ? prev.filter((step) => step.key !== key) : prev))
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    if (!game) {
      setError(t('guideEditor.gameRequired'))
      return
    }
    setIsSaving(true)
    const body: GuideRequest = {
      appId: game.appId,
      gameName: game.name,
      title: title.trim(),
      description: description.trim() || null,
      steps: steps.map((step) => ({
        title: step.title.trim(),
        content: step.content.trim() || null,
        achievements: step.achievements.map((a) => ({
          apiName: a.apiName,
          displayName: a.displayName,
          iconUrl: a.iconUrl,
        })),
      })),
    }
    try {
      const saved = isEdit ? await guidesApi.update(Number(id), body) : await guidesApi.create(body)
      navigate(`/guides/${saved.id}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('guideEditor.saveError'))
      setIsSaving(false)
    }
  }

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
          <LanguageSwitcher />
        </header>

        <main className="flex flex-1 flex-col gap-6 py-8">
          <h1 className="text-3xl font-semibold tracking-tight">
            {isEdit ? t('guideEditor.editTitle') : t('guideEditor.newTitle')}
          </h1>

          {error && (
            <Card className="border-rose-400/30 bg-rose-400/5 p-4 text-sm text-rose-400">
              {error}
            </Card>
          )}

          {isLoading ? (
            <p className="text-sm text-muted-foreground">{t('guideEditor.loading')}</p>
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
              {/* Game selection */}
              <div className="flex flex-col gap-2">
                <Label>{t('guideEditor.game')}</Label>
                {game ? (
                  <div className="flex items-center gap-3 rounded-md border border-border p-3">
                    <GameCover
                      appId={game.appId}
                      name={game.name}
                      className="h-10 w-20 shrink-0 rounded-sm"
                    />
                    <span className="flex-1 truncate text-sm font-medium">{game.name}</span>
                    {!isEdit && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="font-mono text-xs"
                        onClick={() => setGame(null)}
                      >
                        {t('guideEditor.changeGame')}
                      </Button>
                    )}
                  </div>
                ) : (
                  <GameSearchSelect onSelect={setGame} />
                )}
              </div>

              {game && (
                <>
                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="title">{t('guideEditor.guideTitle')}</Label>
                    <Input
                      id="title"
                      required
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      placeholder={t('guideEditor.guideTitlePlaceholder')}
                    />
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="description">{t('guideEditor.description')}</Label>
                    <textarea
                      id="description"
                      rows={3}
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder={t('guideEditor.descriptionPlaceholder')}
                      className={textareaClass}
                    />
                  </div>

                  {/* Steps */}
                  <div className="flex flex-col gap-3">
                    <div className="flex items-center justify-between">
                      <Label>{t('guideEditor.steps')}</Label>
                      <span className="font-mono text-xs text-muted-foreground">
                        {steps.length}
                      </span>
                    </div>

                    {steps.map((step, index) => (
                      <Card key={step.key} className="gap-3 rounded-md border-border bg-card p-4">
                        <div className="flex items-center gap-2">
                          <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-border font-mono text-xs text-muted-foreground">
                            {index + 1}
                          </span>
                          <Input
                            required
                            value={step.title}
                            onChange={(e) => updateStep(step.key, { title: e.target.value })}
                            placeholder={t('guideEditor.stepTitlePlaceholder')}
                            className="flex-1"
                          />
                          <div className="flex items-center gap-0.5">
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon-sm"
                              disabled={index === 0}
                              onClick={() => moveStep(index, -1)}
                              aria-label={t('guideEditor.moveUp')}
                            >
                              <ChevronUp className="size-4" />
                            </Button>
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon-sm"
                              disabled={index === steps.length - 1}
                              onClick={() => moveStep(index, 1)}
                              aria-label={t('guideEditor.moveDown')}
                            >
                              <ChevronDown className="size-4" />
                            </Button>
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon-sm"
                              disabled={steps.length === 1}
                              onClick={() => removeStep(step.key)}
                              aria-label={t('guideEditor.removeStep')}
                            >
                              <X className="size-4" />
                            </Button>
                          </div>
                        </div>

                        <textarea
                          rows={2}
                          value={step.content}
                          onChange={(e) => updateStep(step.key, { content: e.target.value })}
                          placeholder={t('guideEditor.stepContentPlaceholder')}
                          className={textareaClass}
                        />

                        {/* Linked achievements */}
                        <div className="flex flex-col gap-2">
                          {step.achievements.length > 0 && (
                            <div className="flex flex-wrap gap-1.5">
                              {step.achievements.map((ach) => (
                                <span
                                  key={ach.apiName}
                                  className="flex items-center gap-1 rounded-full bg-amber-400/10 py-0.5 pr-1 pl-2 font-mono text-[11px] text-amber-400"
                                >
                                  {ach.displayName ?? ach.apiName}
                                  <button
                                    type="button"
                                    onClick={() =>
                                      toggleAchievement(step.key, {
                                        apiName: ach.apiName,
                                        displayName: ach.displayName ?? '',
                                        description: '',
                                        iconUrl: ach.iconUrl ?? '',
                                      })
                                    }
                                    className="rounded-full p-0.5 hover:bg-amber-400/20"
                                    aria-label={t('guideEditor.unlinkAchievement')}
                                  >
                                    <X className="size-3" />
                                  </button>
                                </span>
                              ))}
                            </div>
                          )}
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            className="gap-1.5 self-start font-mono text-xs"
                            onClick={() =>
                              setOpenPickerKey((prev) => (prev === step.key ? null : step.key))
                            }
                          >
                            <Trophy className="size-3.5" />
                            {t('guideEditor.linkAchievements')}
                          </Button>
                          {openPickerKey === step.key && (
                            <AchievementPicker
                              schema={schema}
                              selected={step.achievements.map((a) => a.apiName)}
                              onToggle={(achievement) => toggleAchievement(step.key, achievement)}
                            />
                          )}
                        </div>
                      </Card>
                    ))}

                    <Button
                      type="button"
                      variant="outline"
                      className="gap-1.5 self-start font-mono text-xs"
                      onClick={() => setSteps((prev) => [...prev, newStep()])}
                    >
                      <Plus className="size-3.5" />
                      {t('guideEditor.addStep')}
                    </Button>
                  </div>

                  <Separator className="bg-border" />
                  <div className="flex items-center gap-2">
                    <Button type="submit" disabled={isSaving} className="font-mono text-xs">
                      {isSaving ? t('guideEditor.saving') : t('guideEditor.save')}
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      className="font-mono text-xs"
                      render={<Link to="/guides" />}
                    >
                      {t('guideEditor.cancel')}
                    </Button>
                  </div>
                </>
              )}
            </form>
          )}
        </main>
      </div>
    </div>
  )
}

export default GuideEditor
