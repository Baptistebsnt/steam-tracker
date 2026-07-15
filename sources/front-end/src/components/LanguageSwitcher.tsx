import { useTranslation } from 'react-i18next'
import { supportedLanguages } from '@/i18n'
import { cn } from '@/lib/utils'

function LanguageSwitcher({ className }: { className?: string }) {
  const { i18n } = useTranslation()
  const current = supportedLanguages.find((lng) => i18n.resolvedLanguage === lng) ?? 'fr'

  return (
    <div className={cn('flex items-center gap-1 font-mono text-xs', className)}>
      {supportedLanguages.map((lng) => (
        <button
          key={lng}
          type="button"
          onClick={() => i18n.changeLanguage(lng)}
          aria-current={current === lng}
          className={cn(
            'px-1.5 uppercase transition-colors',
            current === lng ? 'text-amber-400' : 'text-muted-foreground hover:text-foreground',
          )}
        >
          {lng}
        </button>
      ))}
    </div>
  )
}

export default LanguageSwitcher
