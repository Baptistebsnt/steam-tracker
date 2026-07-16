import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Input } from '@/components/ui/input'
import { steamApi, type SteamGameSearchDto } from '@/lib/api'
import { Search } from 'lucide-react'

type GameSearchSelectProps = {
  onSelect: (game: { appId: number; name: string }) => void
}

const GameSearchSelect = ({ onSelect }: GameSearchSelectProps) => {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SteamGameSearchDto[]>([])
  const [isSearching, setIsSearching] = useState(false)

  useEffect(() => {
    const term = query.trim()
    if (term.length < 2) {
      setResults([])
      setIsSearching(false)
      return
    }
    setIsSearching(true)
    let cancelled = false
    const handle = setTimeout(() => {
      steamApi
        .searchGames(term)
        .then((data) => {
          if (!cancelled) setResults(data)
        })
        .catch(() => {
          if (!cancelled) setResults([])
        })
        .finally(() => {
          if (!cancelled) setIsSearching(false)
        })
    }, 300)
    return () => {
      cancelled = true
      clearTimeout(handle)
    }
  }, [query])

  return (
    <div className="flex flex-col gap-2">
      <div className="relative">
        <Search className="pointer-events-none absolute top-1/2 left-2.5 size-3.5 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={t('guideEditor.gameSearchPlaceholder')}
          className="pl-8"
        />
      </div>
      {isSearching && <p className="text-xs text-muted-foreground">{t('guideEditor.searching')}</p>}
      {results.length > 0 && (
        <div className="flex max-h-72 flex-col gap-1 overflow-y-auto rounded-md border border-border p-1">
          {results.map((game) => (
            <button
              key={game.appId}
              type="button"
              onClick={() => onSelect({ appId: game.appId, name: game.name })}
              className="flex items-center gap-3 rounded-sm p-1.5 text-left transition-colors hover:bg-muted"
            >
              {game.imageUrl ? (
                <img
                  src={game.imageUrl}
                  alt=""
                  className="h-8 w-16 shrink-0 rounded-sm object-cover"
                />
              ) : (
                <div className="h-8 w-16 shrink-0 rounded-sm bg-muted" />
              )}
              <span className="truncate text-sm">{game.name}</span>
              <span className="ml-auto shrink-0 font-mono text-[11px] text-muted-foreground">
                {game.appId}
              </span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

export default GameSearchSelect
