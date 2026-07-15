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
  lastPlayed: string | null
}

export type GlobalStatsDto = {
  gamesTracked: number
  totalPlaytimeMinutes: number
}

export type AchievementDto = {
  apiName: string
  name: string
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
