import { createContext, useContext, useState, type ReactNode } from 'react'
import { authApi } from '@/lib/api'

type AuthUser = {
  email: string
  steamId: string | null
}

type AuthContextValue = {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, steamId?: string) => Promise<void>
  setSteamId: (steamId: string | null) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readStoredUser(): AuthUser | null {
  const email = localStorage.getItem('email')
  const token = localStorage.getItem('token')
  if (!email || !token) return null
  return { email, steamId: localStorage.getItem('steamId') }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser)

  const persist = (auth: { token: string; email: string; steamId: string | null }) => {
    localStorage.setItem('token', auth.token)
    localStorage.setItem('email', auth.email)
    if (auth.steamId) localStorage.setItem('steamId', auth.steamId)
    else localStorage.removeItem('steamId')
    setUser({ email: auth.email, steamId: auth.steamId })
  }

  const login = async (email: string, password: string) => {
    persist(await authApi.login(email, password))
  }

  const register = async (email: string, password: string, steamId?: string) => {
    persist(await authApi.register(email, password, steamId))
  }

  const setSteamId = (steamId: string | null) => {
    if (steamId) localStorage.setItem('steamId', steamId)
    else localStorage.removeItem('steamId')
    setUser((prev) => (prev ? { ...prev, steamId } : prev))
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('email')
    localStorage.removeItem('steamId')
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated: user !== null, login, register, setSteamId, logout }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
