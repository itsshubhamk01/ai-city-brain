import { LogOut, Mail, ShieldCheck, User } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../components/PageHeader'
import { useAuth } from '../hooks/useAuth'
import { ROLE_LABELS } from '../lib/constants'

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  function handleSignOut() {
    logout()
    navigate('/', { replace: true })
  }

  return (
    <div className="min-h-screen bg-base text-ink">
      <PageHeader title="Profile" subtitle="Your account" />

      <main className="max-w-md mx-auto px-4 sm:px-6 py-10">
        <div className="panel-3d p-6 text-center mb-5">
          <div className="h-16 w-16 rounded-full bg-accent/15 flex items-center justify-center mx-auto mb-3">
            <User size={26} className="text-accent" />
          </div>
          <h2 className="font-display text-lg font-semibold text-ink">{user.fullName}</h2>
          <p className="text-xs text-ink-faint">@{user.username}</p>
        </div>

        <div className="panel divide-y divide-hairline">
          <Row icon={Mail} label="Email" value={user.email} />
          <Row icon={ShieldCheck} label="Role" value={ROLE_LABELS[user.role]} />
        </div>

        <button onClick={handleSignOut} className="btn-danger w-full mt-5">
          <LogOut size={15} /> Sign out
        </button>
      </main>
    </div>
  )
}

function Row({ icon: Icon, label, value }: { icon: typeof Mail; label: string; value: string }) {
  return (
    <div className="px-5 py-4 flex items-center gap-3">
      <Icon size={15} className="text-ink-faint shrink-0" />
      <div className="min-w-0">
        <p className="text-[11px] text-ink-faint">{label}</p>
        <p className="text-sm text-ink truncate">{value}</p>
      </div>
    </div>
  )
}
