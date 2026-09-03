import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { cx } from '../lib/utils'

export function AuthPanel() {
  const { login, register, error } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [mode, setMode] = useState<'signin' | 'register'>('signin')
  const [submitting, setSubmitting] = useState(false)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      if (mode === 'signin') {
        await login(username, password)
      } else {
        await register({ username, password, fullName, email })
      }
      const destination = (location.state as { from?: string } | null)?.from ?? '/'
      navigate(destination, { replace: true })
    } catch {
      // error surfaced via auth context below
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="panel p-5">
      <div className="flex gap-1 mb-4 bg-raised rounded-lg p-1">
        <button
          onClick={() => setMode('signin')}
          className={cx('flex-1 rounded-md py-1.5 text-sm font-medium transition-colors', mode === 'signin' ? 'bg-accent text-white' : 'text-ink-muted')}
        >
          Sign in
        </button>
        <button
          onClick={() => setMode('register')}
          className={cx('flex-1 rounded-md py-1.5 text-sm font-medium transition-colors', mode === 'register' ? 'bg-accent text-white' : 'text-ink-muted')}
        >
          Create account
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-2.5">
        {mode === 'register' && (
          <>
            <input
              className="input"
              placeholder="Full name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />
            <input
              type="email"
              className="input"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </>
        )}
        <input
          className="input"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          required
        />
        <input
          type="password"
          className="input"
          placeholder={mode === 'register' ? 'Password (min. 8 characters)' : 'Password'}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete={mode === 'signin' ? 'current-password' : 'new-password'}
          minLength={mode === 'register' ? 8 : undefined}
          required
        />

        {error && <p className="text-xs text-signal-critical">{error}</p>}

        <button type="submit" disabled={submitting} className="btn-primary w-full">
          {submitting && <Loader2 size={15} className="animate-spin" />}
          {mode === 'signin' ? 'Sign in' : 'Create account'}
        </button>
      </form>
    </div>
  )
}
