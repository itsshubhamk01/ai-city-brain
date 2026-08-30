import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, clearToken, getToken, registerUnauthorizedHandler, setToken } from '../lib/api'
import type { CurrentUser } from '../types'

interface AuthContextValue {
  user: CurrentUser | null
  loading: boolean
  error: string | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      clearToken()
      setUser(null)
    })
  }, [])

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setLoading(false)
      return
    }
    api
      .me()
      .then(setUser)
      .catch(() => clearToken())
      .finally(() => setLoading(false))
  }, [])

  async function login(username: string, password: string) {
    setError(null)
    try {
      const response = await api.login(username, password)
      setToken(response.token)
      const me = await api.me()
      setUser(me)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Login failed')
      throw e
    }
  }

  function logout() {
    clearToken()
    setUser(null)
  }

  const value = useMemo(() => ({ user, loading, error, login, logout }), [user, loading, error])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
