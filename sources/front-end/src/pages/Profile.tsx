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
  const { user, setSteamId } = useAuth()
  const [profile, setProfile] = useState<UserProfileDto | null>(null)
  const [steamIdInput, setSteamIdInput] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let cancelled = false
    usersApi
      .me()
      .then((data) => {
        if (cancelled) return
        setProfile(data)
        setSteamIdInput(data.steamId ?? '')
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
      const updated = await usersApi.updateSteamId(trimmed === '' ? null : trimmed)
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

  const dirty = steamIdInput.trim() !== (profile?.steamId ?? '')

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
              <div className="flex flex-col gap-1.5">
                <Label>{t('common.email')}</Label>
                <p className="font-mono text-sm text-muted-foreground">
                  {profile?.email ?? user?.email ?? '—'}
                </p>
              </div>
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
