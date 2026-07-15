import { useNavigate } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuth } from '@/lib/auth'
import { ArrowRight, Radar, Wallet, Hourglass } from 'lucide-react'

const ticker = [
  { label: 'Hades II', delta: '-40%', tone: 'up' as const },
  { label: 'Disco Elysium', delta: 'stale 214d', tone: 'warn' as const },
  { label: 'Balatro', delta: '-20%', tone: 'up' as const },
  { label: 'Backlog', delta: '+3 unplayed', tone: 'warn' as const },
  { label: 'Hollow Knight: Silksong', delta: '-15%', tone: 'up' as const },
  { label: "Baldur's Gate 3", delta: '86h logged', tone: 'flat' as const },
]

const features = [
  {
    icon: Radar,
    tag: '01',
    title: 'Backlog radar',
    body: "Repère les jeux achetés et jamais lancés, classés par temps d'oubli.",
  },
  {
    icon: Wallet,
    tag: '02',
    title: 'Price watch',
    body: "Alerte dès qu'un jeu de ta liste passe sous ton prix cible.",
  },
  {
    icon: Hourglass,
    tag: '03',
    title: 'Playtime ledger',
    body: 'Un relevé précis du temps joué, semaine par semaine, par jeu.',
  },
]

function Landing() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  return (
    <div className="dark min-h-svh bg-background text-foreground">
      <div className="mx-auto flex min-h-svh max-w-5xl flex-col px-6">
        <header className="flex items-center justify-between py-6">
          <div className="flex items-center gap-2 font-mono text-sm tracking-tight text-muted-foreground">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />
            steam-tracker
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="font-mono text-xs"
            onClick={() => navigate(isAuthenticated ? '/dashboard' : '/login')}
          >
            {isAuthenticated ? 'Mon dashboard' : 'Se connecter'}
          </Button>
        </header>

        <main className="flex flex-1 flex-col justify-center gap-16 py-16">
          <section>
            <p className="font-mono text-xs tracking-widest text-amber-400/80 uppercase">
              // backlog, playtime &amp; price watch
            </p>
            <h1 className="mt-4 max-w-2xl text-5xl font-semibold tracking-tight text-balance sm:text-6xl">
              Sache enfin ce qui se passe dans ta bibliothèque.
            </h1>
            <p className="mt-5 max-w-lg text-base text-muted-foreground">
              Steam Tracker surveille tes jeux pendant que tu n'y joues pas&nbsp;: backlog qui prend
              la poussière, baisses de prix, temps de jeu réel.
            </p>
            <div className="mt-8 flex items-center gap-3">
              <Button
                className="gap-2 bg-amber-400 text-neutral-950 hover:bg-amber-300"
                onClick={() => navigate(isAuthenticated ? '/dashboard' : '/register')}
              >
                Connecter mon Steam ID
                <ArrowRight className="size-4" />
              </Button>
              <Button variant="outline">Voir une démo</Button>
            </div>

            <dl className="mt-14 grid grid-cols-3 divide-x divide-border border-y border-border font-mono">
              {[
                ['128', 'jeux suivis'],
                ['612h', 'temps loggé'],
                ['14', 'baisses de prix'],
              ].map(([value, label]) => (
                <div key={label} className="px-4 py-4 first:pl-0">
                  <dt className="text-2xl font-medium text-foreground">{value}</dt>
                  <dd className="mt-1 text-xs text-muted-foreground uppercase">{label}</dd>
                </div>
              ))}
            </dl>
          </section>

          <section
            aria-label="Dernières alertes"
            className="relative overflow-hidden rounded-md border border-border bg-card py-3 mask-[linear-gradient(to_right,transparent,black_5%,black_95%,transparent)]"
          >
            <div className="animate-[ticker_28s_linear_infinite] motion-reduce:animate-none flex w-max gap-10 font-mono text-sm whitespace-nowrap">
              {[...ticker, ...ticker].map((item, i) => (
                <span key={i} className="flex items-center gap-2">
                  <span
                    className={
                      item.tone === 'up'
                        ? 'text-amber-400'
                        : item.tone === 'warn'
                          ? 'text-rose-400/80'
                          : 'text-muted-foreground'
                    }
                  >
                    {item.tone === 'up' ? '▲' : item.tone === 'warn' ? '●' : '—'}
                  </span>
                  <span className="text-foreground">{item.label}</span>
                  <span className="text-muted-foreground">{item.delta}</span>
                </span>
              ))}
            </div>
          </section>

          <section className="grid gap-4 sm:grid-cols-3">
            {features.map(({ icon: Icon, tag, title, body }) => (
              <Card
                key={title}
                className="gap-3 rounded-md border-border bg-card p-5 transition-colors hover:border-amber-400/40"
              >
                <div className="flex items-center justify-between">
                  <Icon className="size-5 text-amber-400" strokeWidth={1.75} />
                  <Badge
                    variant="outline"
                    className="rounded-sm border-border font-mono text-[10px] text-muted-foreground"
                  >
                    {tag}
                  </Badge>
                </div>
                <h3 className="text-sm font-medium">{title}</h3>
                <p className="text-sm text-muted-foreground">{body}</p>
              </Card>
            ))}
          </section>
        </main>

        <Separator className="bg-border" />
        <footer className="flex items-center justify-between py-6 font-mono text-[11px] text-muted-foreground">
          <span>steam-tracker</span>
          <span>dernière synchro&nbsp;: à l'instant</span>
        </footer>
      </div>
    </div>
  )
}

export default Landing
