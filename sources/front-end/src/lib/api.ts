const API_BASE = '/api'

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('token')

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new ApiError(res.status, body?.message ?? res.statusText)
  }

  if (res.status === 204) return undefined as T

  return res.json() as Promise<T>
}

export type AuthResponse = {
  token: string
  email: string
  steamId: string | null
}

export type GameDto = {
  appId: number
  name: string
  playtimeMinutes: number
  totalAchievements: number
  unlockedAchievements: number
  completionPercent: number
}

export type GlobalStatsDto = {
  totalGames: number
  totalPlaytimeMinutes: number
  totalAchievements: number
  unlockedAchievements: number
  globalCompletionPercent: number
  mostPlayedGame: GameDto | null
}

export type AchievementDto = {
  apiName: string
  displayName: string
  description: string | null
  iconUrl: string | null
  unlocked: boolean
  unlockedAt: string | null
}

export const authApi = {
  register: (email: string, password: string, steamId?: string) =>
    request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, steamId }),
    }),
  login: (email: string, password: string) =>
    request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
}

export const gamesApi = {
  list: () => request<GameDto[]>('/games'),
  stats: () => request<GlobalStatsDto>('/games/stats'),
  achievements: (appId: number) => request<AchievementDto[]>(`/games/${appId}/achievements`),
}

export const syncApi = {
  sync: () => request<void>('/sync', { method: 'POST' }),
}

export type UserProfileDto = {
  email: string
  steamId: string | null
  createdAt: string
}

export const usersApi = {
  me: () => request<UserProfileDto>('/users/me'),
  updateSteamId: (steamId: string | null) =>
    request<UserProfileDto>('/users/me', {
      method: 'PATCH',
      body: JSON.stringify({ steamId }),
    }),
}

export type GuideSummaryDto = {
  id: number
  appId: number
  gameName: string
  title: string
  authorEmail: string
  stepCount: number
  achievementCount: number
  createdAt: string
}

export type GuideStepAchievementDto = {
  apiName: string
  displayName: string | null
  iconUrl: string | null
  unlocked: boolean
}

export type GuideStepDto = {
  id: number
  position: number
  title: string
  content: string | null
  achievements: GuideStepAchievementDto[]
}

export type GuideDetailDto = {
  id: number
  appId: number
  gameName: string
  title: string
  description: string | null
  authorEmail: string
  isAuthor: boolean
  linkedAchievements: number
  unlockedAchievements: number
  createdAt: string
  updatedAt: string
  steps: GuideStepDto[]
}

export type GuideAchievementInput = {
  apiName: string
  displayName?: string | null
  iconUrl?: string | null
}

export type GuideStepInput = {
  title: string
  content?: string | null
  achievements: GuideAchievementInput[]
}

export type GuideRequest = {
  appId: number
  gameName: string
  title: string
  description?: string | null
  steps: GuideStepInput[]
}

export const guidesApi = {
  list: (appId?: number) => request<GuideSummaryDto[]>(`/guides${appId ? `?appId=${appId}` : ''}`),
  mine: () => request<GuideSummaryDto[]>('/guides/mine'),
  get: (id: number) => request<GuideDetailDto>(`/guides/${id}`),
  create: (body: GuideRequest) =>
    request<GuideDetailDto>('/guides', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: GuideRequest) =>
    request<GuideDetailDto>(`/guides/${id}`, { method: 'PATCH', body: JSON.stringify(body) }),
  remove: (id: number) => request<void>(`/guides/${id}`, { method: 'DELETE' }),
}

export type SteamGameSearchDto = {
  appId: number
  name: string
  imageUrl: string
}

export type SteamAchievementSchemaDto = {
  apiName: string
  displayName: string
  description: string
  iconUrl: string
}

export const steamApi = {
  searchGames: (q: string) =>
    request<SteamGameSearchDto[]>(`/steam/games/search?q=${encodeURIComponent(q)}`),
  achievementSchema: (appId: number) =>
    request<SteamAchievementSchemaDto[]>(`/steam/games/${appId}/achievements`),
}
