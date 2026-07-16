import { createContext, useContext, useState, type ReactNode } from 'react'
import { authApi, type AuthResponse } from '@/lib/api'

type AuthUser = {
  email: string
  steamId: string | null
  displayName: string | null
  avatarUrl: string | null
}

type AuthContextValue = {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, steamId?: string) => Promise<void>
  loginWithSteam: (auth: AuthResponse) => void
  setSteamId: (steamId: string | null) => void
  setDisplayName: (displayName: string | null) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readStoredUser(): AuthUser | null {
  const email = localStorage.getItem('email')
  const token = localStorage.getItem('token')
  if (!email || !token) return null
  return {
    email,
    steamId: localStorage.getItem('steamId'),
    displayName: localStorage.getItem('displayName'),
    avatarUrl: localStorage.getItem('avatarUrl'),
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser)

  const persist = (auth: AuthResponse) => {
    localStorage.setItem('token', auth.token)
    localStorage.setItem('email', auth.email)
    if (auth.steamId) localStorage.setItem('steamId', auth.steamId)
    else localStorage.removeItem('steamId')
    if (auth.displayName) localStorage.setItem('displayName', auth.displayName)
    else localStorage.removeItem('displayName')
    if (auth.avatarUrl) localStorage.setItem('avatarUrl', auth.avatarUrl)
    else localStorage.removeItem('avatarUrl')
    setUser({
      email: auth.email,
      steamId: auth.steamId,
      displayName: auth.displayName,
      avatarUrl: auth.avatarUrl,
    })
  }

  const login = async (email: string, password: string) => {
    persist(await authApi.login(email, password))
  }

  const register = async (email: string, password: string, steamId?: string) => {
    persist(await authApi.register(email, password, steamId))
  }

  const loginWithSteam = (auth: AuthResponse) => {
    persist(auth)
  }

  const setSteamId = (steamId: string | null) => {
    if (steamId) localStorage.setItem('steamId', steamId)
    else localStorage.removeItem('steamId')
    setUser((prev) => (prev ? { ...prev, steamId } : prev))
  }

  const setDisplayName = (displayName: string | null) => {
    if (displayName) localStorage.setItem('displayName', displayName)
    else localStorage.removeItem('displayName')
    setUser((prev) => (prev ? { ...prev, displayName } : prev))
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('email')
    localStorage.removeItem('steamId')
    localStorage.removeItem('displayName')
    localStorage.removeItem('avatarUrl')
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        login,
        register,
        loginWithSteam,
        setSteamId,
        setDisplayName,
        logout,
      }}
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
