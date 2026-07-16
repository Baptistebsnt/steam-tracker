import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import LanguageSwitcher from '@/components/LanguageSwitcher'
import { useAuth } from '@/lib/auth'
import { ApiError, usersApi, type UserProfileDto } from '@/lib/api'
import { ArrowLeft, Check } from 'lucide-react'

function Profile() {
  const { t, i18n } = useTranslation()
  const { setSteamId, setDisplayName } = useAuth()
  const [profile, setProfile] = useState<UserProfileDto | null>(null)
  const [steamIdInput, setSteamIdInput] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [usernameInput, setUsernameInput] = useState('')
  const [usernameError, setUsernameError] = useState<string | null>(null)
  const [usernameSaved, setUsernameSaved] = useState(false)
  const [isSavingUsername, setIsSavingUsername] = useState(false)

  useEffect(() => {
    let cancelled = false
    usersApi
      .me()
      .then((data) => {
        if (cancelled) return
        setProfile(data)
        setSteamIdInput(data.steamId ?? '')
        setUsernameInput(data.username ?? '')
      })
      .catch((err) => {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : t('profile.loadError'))
      })
    return () => {
      cancelled = true
    }
  }, [t])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setSuccess(false)
    setIsSubmitting(true)
    try {
      const trimmed = steamIdInput.trim()
      const updated = await usersApi.update({ steamId: trimmed })
      setProfile(updated)
      setSteamIdInput(updated.steamId ?? '')
      setSteamId(updated.steamId)
      setSuccess(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('profile.saveError'))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleUsernameSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setUsernameError(null)
    setUsernameSaved(false)
    setIsSavingUsername(true)
    try {
      const trimmed = usernameInput.trim()
      const updated = await usersApi.update({ username: trimmed })
      setProfile(updated)
      setUsernameInput(updated.username ?? '')
      setDisplayName(updated.username ?? updated.personaName ?? null)
      setUsernameSaved(true)
    } catch (err) {
      setUsernameError(err instanceof ApiError ? err.message : t('profile.usernameSaveError'))
    } finally {
      setIsSavingUsername(false)
    }
  }

  const dirty = steamIdInput.trim() !== (profile?.steamId ?? '')
  const usernameDirty = usernameInput.trim() !== (profile?.username ?? '')

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-2xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" />
            {t('profile.back')}
          </Link>
          <LanguageSwitcher />
        </header>

        <main className="flex flex-1 flex-col gap-8 py-8">
          <div className="flex flex-col gap-1">
            <h1 className="text-3xl font-semibold tracking-tight">{t('profile.title')}</h1>
            <p className="text-sm text-muted-foreground">{t('profile.subtitle')}</p>
          </div>

          <Card className="border-border bg-card">
            <CardHeader>
              <CardTitle className="text-base">{t('profile.accountSection')}</CardTitle>
              <CardDescription>{t('profile.accountSectionHint')}</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              {profile?.personaName && (
                <div className="flex flex-col gap-1.5">
                  <Label>{t('profile.steamAccount')}</Label>
                  <div className="flex items-center gap-3">
                    {profile.avatarUrl && (
                      <img
                        src={profile.avatarUrl}
                        alt=""
                        className="size-9 rounded-full border border-border"
                      />
                    )}
                    <span className="text-sm font-medium">{profile.personaName}</span>
                  </div>
                </div>
              )}
              <form onSubmit={handleUsernameSubmit} className="flex flex-col gap-1.5">
                <Label htmlFor="username">{t('profile.username')}</Label>
                <Input
                  id="username"
                  autoComplete="off"
                  maxLength={30}
                  placeholder={t('profile.usernamePlaceholder')}
                  value={usernameInput}
                  onChange={(e) => {
                    setUsernameInput(e.target.value)
                    setUsernameSaved(false)
                  }}
                />
                <p className="text-xs text-muted-foreground">{t('profile.usernameHint')}</p>
                {usernameError && <p className="text-sm text-rose-400">{usernameError}</p>}
                {usernameSaved && (
                  <p className="flex items-center gap-1.5 text-sm text-emerald-400">
                    <Check className="size-4" />
                    {t('profile.usernameSaved')}
                  </p>
                )}
                <Button
                  type="submit"
                  disabled={isSavingUsername || !usernameDirty}
                  className="mt-1 self-start"
                >
                  {isSavingUsername ? t('profile.saving') : t('profile.save')}
                </Button>
              </form>
              {profile?.createdAt && (
                <div className="flex flex-col gap-1.5">
                  <Label>{t('profile.memberSince')}</Label>
                  <p className="font-mono text-sm text-muted-foreground">
                    {new Date(profile.createdAt).toLocaleDateString(i18n.language, {
                      day: '2-digit',
                      month: 'long',
                      year: 'numeric',
                    })}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="border-border bg-card">
            <CardHeader>
              <CardTitle className="text-base">{t('profile.steamSection')}</CardTitle>
              <CardDescription>{t('profile.steamSectionHint')}</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="steamId">{t('profile.steamId')}</Label>
                  <Input
                    id="steamId"
                    inputMode="numeric"
                    autoComplete="off"
                    placeholder="76561198000000000"
                    value={steamIdInput}
                    onChange={(e) => {
                      setSteamIdInput(e.target.value)
                      setSuccess(false)
                    }}
                    className="font-mono"
                  />
                  <p className="text-xs text-muted-foreground">{t('profile.steamIdHint')}</p>
                </div>
                {error && <p className="text-sm text-rose-400">{error}</p>}
                {success && (
                  <p className="flex items-center gap-1.5 text-sm text-emerald-400">
                    <Check className="size-4" />
                    {t('profile.saved')}
                  </p>
                )}
                <Button type="submit" disabled={isSubmitting || !dirty} className="mt-1 self-start">
                  {isSubmitting ? t('profile.saving') : t('profile.save')}
                </Button>
              </form>
            </CardContent>
          </Card>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>{t('common.appName')}</span>
        </footer>
      </div>
    </div>
  )
}

export default Profile
