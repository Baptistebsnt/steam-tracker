import { useState } from 'react'
import { Gamepad2 } from 'lucide-react'
import { cn } from '@/lib/utils'
import { steamHeaderImage } from '@/lib/steam'

type GameCoverProps = {
  appId: number
  name: string
  className?: string
}

/**
 * Steam header artwork for a game, with a graceful fallback for titles that have no
 * artwork on the CDN (e.g. delisted games or tools).
 */
function GameCover({ appId, name, className }: GameCoverProps) {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return (
      <div className={cn('flex items-center justify-center bg-muted', className)}>
        <Gamepad2 className="size-6 text-muted-foreground" strokeWidth={1.5} />
      </div>
    )
  }

  return (
    <img
      src={steamHeaderImage(appId)}
      alt={name}
      loading="lazy"
      onError={() => setFailed(true)}
      className={cn('object-cover', className)}
    />
  )
}

export default GameCover
