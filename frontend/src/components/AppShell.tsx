import { useEffect, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import {
  LayoutDashboard,
  Map as MapIcon,
  Siren,
  Bot,
  SlidersHorizontal,
  LogOut,
  Building2,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { ROLE_LABELS } from '../lib/constants'
import { ConnectionBadge } from './common'
import { formatClock } from '../lib/utils'

const NAV_ITEMS = [
  { to: '/', label: 'Command Center', icon: LayoutDashboard, end: true },
  { to: '/map', label: 'City Map', icon: MapIcon, end: false },
  { to: '/incidents', label: 'Incidents', icon: Siren, end: false },
  { to: '/agents', label: 'AI Decisions', icon: Bot, end: false },
  { to: '/simulation', label: 'Simulation', icon: SlidersHorizontal, end: false },
]

export function AppShell() {
  const { user, logout } = useAuth()
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(id)
  }, [])

  return (
    <div className="flex h-screen bg-base text-ink overflow-hidden">
      <aside className="w-60 shrink-0 border-r border-hairline bg-surface flex flex-col">
        <div className="flex items-center gap-2 px-5 py-5">
          <div className="h-8 w-8 rounded-lg bg-accent/15 flex items-center justify-center">
            <Building2 size={18} className="text-accent" />
          </div>
          <div>
            <p className="font-display text-sm font-semibold leading-none">AI City Brain</p>
            <p className="text-[11px] text-ink-faint mt-0.5">NovaCity Ops</p>
          </div>
        </div>

        <nav className="flex-1 px-3 space-y-1">
          {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive ? 'bg-accent/15 text-accent' : 'text-ink-muted hover:bg-raised hover:text-ink'
                }`
              }
            >
              <Icon size={17} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-3 pb-4 pt-2 border-t border-hairline">
          <div className="px-3 py-2">
            <p className="text-sm font-medium text-ink">{user?.fullName}</p>
            <p className="text-[11px] text-ink-faint">{user ? ROLE_LABELS[user.role] : ''}</p>
          </div>
          <button
            onClick={logout}
            className="flex items-center gap-2 w-full rounded-lg px-3 py-2 text-sm text-ink-muted hover:bg-raised hover:text-signal-critical transition-colors"
          >
            <LogOut size={16} /> Sign out
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-14 shrink-0 border-b border-hairline flex items-center justify-end gap-5 px-6">
          <ConnectionBadge />
          <span className="font-mono text-xs text-ink-muted">{formatClock(now)}</span>
        </header>
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
