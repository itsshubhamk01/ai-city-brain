import { useState } from 'react'
import { useLocation, useNavigate, Link } from 'react-router-dom'
import { Building2, Loader2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

const SKYLINE = [
  22, 40, 28, 55, 34, 70, 30, 48, 62, 36, 26, 58, 44, 32, 68, 24, 50, 38, 30, 46,
]

export default function LoginPage() {
  const { login, error } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleLogin(u: string, p: string) {
    setSubmitting(true)
    try {
      await login(u, p)
      const destination = (location.state as { from?: string } | null)?.from ?? '/dashboard'
      navigate(destination, { replace: true })
    } catch {
      // error surfaced via auth context
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="relative min-h-screen bg-base overflow-hidden flex items-center justify-center px-4">
      <div
        className="absolute inset-0 opacity-[0.07]"
        style={{
          backgroundImage:
            'linear-gradient(#5B8DEF 1px, transparent 1px), linear-gradient(90deg, #5B8DEF 1px, transparent 1px)',
          backgroundSize: '48px 48px',
        }}
      />
      <div className="absolute bottom-0 left-0 right-0 flex items-end h-64 opacity-60 pointer-events-none">
        {SKYLINE.map((h, i) => (
          <div key={i} className="flex-1 bg-surface border-t border-hairline relative" style={{ height: `${h}%` }}>
            <div
              className="absolute inset-2 opacity-40"
              style={{
                backgroundImage: 'radial-gradient(#5B8DEF 1px, transparent 1.5px)',
                backgroundSize: '8px 10px',
              }}
            />
          </div>
        ))}
      </div>
      <div className="absolute inset-0 bg-gradient-to-t from-base via-base/60 to-base" />

      <div className="relative z-10 w-full max-w-md">
        <Link to="/" className="flex flex-col items-center mb-8">
          <div className="h-12 w-12 rounded-2xl bg-accent/15 flex items-center justify-center mb-4">
            <Building2 size={24} className="text-accent" />
          </div>
          <h1 className="font-display text-2xl font-semibold text-ink">AI City Brain</h1>
          <p className="text-sm text-ink-muted mt-1">Smart City Digital Twin for Mumbai</p>
        </Link>

        <form
          onSubmit={(e) => {
            e.preventDefault()
            handleLogin(username, password)
          }}
          className="panel p-6 backdrop-blur-sm bg-surface/80 space-y-4"
        >
          <div>
            <label className="label" htmlFor="username">Username</label>
            <input
              id="username"
              className="input mt-1.5"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Your username"
              autoComplete="username"
            />
          </div>
          <div>
            <label className="label" htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="input mt-1.5"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"
              autoComplete="current-password"
            />
          </div>

          {error && <p className="text-xs text-signal-critical">{error}</p>}

          <button type="submit" disabled={submitting} className="btn-primary w-full">
            {submitting && <Loader2 size={15} className="animate-spin" />}
            Sign in
          </button>
        </form>

        <p className="text-center text-xs text-ink-faint mt-5">
          New here?{' '}
          <a href="/#auth" className="text-accent-soft hover:underline">Create an account</a>
        </p>
      </div>
    </div>
  )
}
