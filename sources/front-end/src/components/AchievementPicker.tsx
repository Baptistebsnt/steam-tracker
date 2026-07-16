import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { type SteamAchievementSchemaDto } from '@/lib/api'
import { Check, Trophy } from 'lucide-react'

type AchievementPickerProps = {
  schema: SteamAchievementSchemaDto[]
  selected: string[]
  onToggle: (achievement: SteamAchievementSchemaDto) => void
}

const AchievementPicker = ({ schema, selected, onToggle }: AchievementPickerProps) => {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')

  const selectedSet = useMemo(() => new Set(selected), [selected])
  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return schema
    return schema.filter(
      (a) => a.displayName.toLowerCase().includes(q) || a.apiName.toLowerCase().includes(q),
    )
  }, [schema, query])

  if (schema.length === 0) {
    return <p className="text-xs text-muted-foreground">{t('guideEditor.noSchema')}</p>
  }

  return (
    <div className="flex flex-col gap-2 rounded-md border border-border p-2">
      <Input
        type="search"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={t('guideEditor.achievementSearchPlaceholder')}
        className="h-7 text-xs"
      />
      <div className="flex max-h-56 flex-col gap-0.5 overflow-y-auto">
        {filtered.map((achievement) => {
          const isSelected = selectedSet.has(achievement.apiName)
          return (
            <button
              key={achievement.apiName}
              type="button"
              onClick={() => onToggle(achievement)}
              className={cn(
                'flex items-center gap-2 rounded-sm p-1.5 text-left transition-colors hover:bg-muted',
                isSelected && 'bg-amber-400/10',
              )}
            >
              {achievement.iconUrl ? (
                <img src={achievement.iconUrl} alt="" className="size-6 shrink-0 rounded-sm" />
              ) : (
                <div className="flex size-6 shrink-0 items-center justify-center rounded-sm bg-muted">
                  <Trophy className="size-3 text-muted-foreground" />
                </div>
              )}
              <span className="flex-1 truncate text-xs">{achievement.displayName}</span>
              <span
                className={cn(
                  'flex size-4 shrink-0 items-center justify-center rounded-sm border',
                  isSelected ? 'border-amber-400 bg-amber-400 text-neutral-950' : 'border-border',
                )}
              >
                {isSelected && <Check className="size-3" />}
              </span>
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default AchievementPicker
