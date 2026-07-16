import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

type SteamLoginButtonProps = {
  className?: string
  variant?: 'default' | 'outline'
}

function SteamLoginButton({ className, variant = 'outline' }: SteamLoginButtonProps) {
  const { t } = useTranslation()

  const handleClick = () => {
    window.location.href = '/api/auth/steam/login'
  }

  return (
    <Button
      type="button"
      variant={variant}
      className={cn('gap-2', className)}
      onClick={handleClick}
    >
      <SteamIcon className="size-4" />
      {t('login.withSteam')}
    </Button>
  )
}

function SteamIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" className={className}>
      <path d="M11.98 0C5.66 0 .49 4.88 0 11.09l6.44 2.66a3.4 3.4 0 0 1 1.92-.6l.09.01 2.86-4.15v-.06a4.55 4.55 0 1 1 4.55 4.55h-.11l-4.08 2.92v.11a3.42 3.42 0 0 1-6.83.25L.4 14.76A12 12 0 1 0 11.98 0zM7.54 18.21l-1.48-.61a2.57 2.57 0 0 0 4.74-1.98 2.57 2.57 0 0 0-3.35-1.4l1.53.63a1.89 1.89 0 1 1-1.44 3.36zm10.48-9.66a3.03 3.03 0 1 0-6.06 0 3.03 3.03 0 0 0 6.06 0zm-5.3 0a2.28 2.28 0 1 1 4.55 0 2.28 2.28 0 0 1-4.55 0z" />
    </svg>
  )
}

export default SteamLoginButton
