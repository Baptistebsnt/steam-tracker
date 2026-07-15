import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/lib/auth'
import { ApiError } from '@/lib/api'

function Register() {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { register } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [steamId, setSteamId] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      await register(email, password, steamId || undefined)
      navigate('/dashboard')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('common.genericError'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="dark flex min-h-svh items-center justify-center bg-background text-foreground">
      <Card className="w-full max-w-sm border-border bg-card">
        <CardHeader>
          <CardTitle>{t('register.title')}</CardTitle>
          <CardDescription>{t('register.description')}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email">{t('common.email')}</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password">{t('common.password')}</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="steamId">{t('register.steamId')}</Label>
              <Input
                id="steamId"
                type="text"
                value={steamId}
                onChange={(e) => setSteamId(e.target.value)}
              />
            </div>
            {error && <p className="text-sm text-rose-400">{error}</p>}
            <Button type="submit" disabled={isSubmitting} className="mt-2">
              {isSubmitting ? t('register.submitting') : t('register.submit')}
            </Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted-foreground">
            {t('register.haveAccount')}{' '}
            <Link to="/login" className="text-amber-400 hover:underline">
              {t('common.login')}
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export default Register
