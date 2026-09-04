import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import {
  LayoutDashboard,
  Map as MapIcon,
  Siren,
  Bot,
  SlidersHorizontal,
  LogOut,
  Building2,
  Menu,
  X,
  ArrowLeft,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { ROLE_LABELS } from '../lib/constants'
import { ConnectionBadge } from './common'
import { cx, formatClock } from '../lib/utils'

const NAV_ITEMS = [
  { to: '/app', label: 'Command Center', icon: LayoutDashboard, end: true },
  { to: '/app/map', label: 'City Map', icon: MapIcon, end: false },
  { to: '/app/incidents', label: 'Incidents', icon: Siren, end: false },
  { to: '/app/agents', label: 'AI Decisions', icon: Bot, end: false },
  { to: '/app/simulation', label: 'Simulation', icon: SlidersHorizontal, end: false },
]

export function AppShell() {
  const { user, logout } = useAuth()
  const [now, setNow] = useState(new Date())
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(id)
  }, [])

  const closeSidebar = () => setSidebarOpen(false)

  return (
    <div className="flex h-screen bg-base text-ink overflow-hidden">
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-30 bg-black/50 lg:hidden"
          onClick={closeSidebar}
          aria-hidden="true"
        />
      )}

      <aside
        className={cx(
          'w-64 shrink-0 border-r border-hairline bg-surface flex flex-col',
          'fixed inset-y-0 left-0 z-40 transition-transform duration-200 ease-out',
          'lg:static lg:translate-x-0',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex items-center justify-between gap-2 px-5 py-5">
          <Link to="/" className="flex items-center gap-2 min-w-0">
            <div className="h-8 w-8 rounded-lg bg-accent/15 flex items-center justify-center shrink-0">
              <Building2 size={18} className="text-accent" />
            </div>
            <div className="min-w-0">
              <p className="font-display text-sm font-semibold leading-none truncate">AI City Brain</p>
              <p className="text-[11px] text-ink-faint mt-0.5">Architecture Demo (not Mumbai data)</p>
            </div>
          </Link>
          <button
            onClick={closeSidebar}
            className="lg:hidden text-ink-muted hover:text-ink p-1 shrink-0"
            aria-label="Close menu"
          >
            <X size={18} />
          </button>
        </div>
        <div className="px-3 -mt-2 mb-2">
          <Link to="/dashboard" className="flex items-center gap-1.5 text-[11px] text-accent-soft hover:text-accent px-2 py-1">
            <ArrowLeft size={12} /> Back to the real Mumbai page
          </Link>
        </div>

        <nav className="flex-1 px-3 space-y-1 overflow-y-auto">
          {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={closeSidebar}
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
        <header className="h-14 shrink-0 border-b border-hairline flex items-center justify-between lg:justify-end gap-4 px-4 lg:px-6">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-ink-muted hover:text-ink p-1 -ml-1"
            aria-label="Open menu"
          >
            <Menu size={20} />
          </button>
          <div className="flex items-center gap-4 lg:gap-5">
            <ConnectionBadge />
            <span className="hidden sm:inline font-mono text-xs text-ink-muted">{formatClock(now)}</span>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
