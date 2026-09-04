import { useNavigate } from 'react-router-dom'
import { NavLink } from 'react-router-dom'
import { ArrowLeft, LayoutDashboard, Map as MapIcon, CloudSun, UserCircle } from 'lucide-react'
import { cx } from '../lib/utils'

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/citymap', icon: MapIcon, label: 'Map' },
  { to: '/weather', icon: CloudSun, label: 'Weather' },
  { to: '/profile', icon: UserCircle, label: 'Profile' },
]

export function PageHeader({ title, subtitle }: { title: string; subtitle?: string }) {
  const navigate = useNavigate()

  return (
    <header className="border-b border-hairline bg-surface/60 backdrop-blur-sm sticky top-0 z-20">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-3.5 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <button
            onClick={() => navigate(-1)}
            className="btn-secondary !p-2 shrink-0"
            aria-label="Go back"
          >
            <ArrowLeft size={16} />
          </button>
          <div className="min-w-0">
            <h1 className="font-display text-sm sm:text-base font-semibold text-ink truncate">{title}</h1>
            {subtitle && <p className="text-xs text-ink-muted truncate">{subtitle}</p>}
          </div>
        </div>

        <nav className="flex items-center gap-1 shrink-0">
          {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cx(
                  'h-9 w-9 rounded-lg flex items-center justify-center transition-colors',
                  isActive ? 'bg-accent/15 text-accent' : 'text-ink-muted hover:bg-raised hover:text-ink'
                )
              }
              aria-label={label}
              title={label}
            >
              <Icon size={16} />
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  )
}
