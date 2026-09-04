import { Link } from 'react-router-dom'
import { MapPin, CloudSun, Map as MapIcon, UserCircle, ArrowRight, Building2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { useLocationContext } from '../hooks/useLocationContext'
import { PageHeader } from '../components/PageHeader'
import { LocationPicker } from '../components/LocationPicker'
import { WeatherPanel } from '../components/WeatherPanel'
import { MumbaiMap } from '../components/MumbaiMap'

const QUICK_LINKS = [
  { to: '/citymap', icon: MapIcon, title: 'Mumbai Map', text: 'Real hospitals, police & fire stations across the city' },
  { to: '/weather', icon: CloudSun, title: 'Weather', text: 'Full current conditions and 7-day forecast' },
  { to: '/profile', icon: UserCircle, title: 'Profile', text: 'Your account details' },
]

export default function DashboardPage() {
  const { user } = useAuth()
  const { location } = useLocationContext()

  return (
    <div className="min-h-screen bg-base text-ink">
      <PageHeader title="Dashboard" subtitle={`Namaste, ${user?.fullName ?? ''}`} />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 space-y-6">
        <div className="panel-3d p-6 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-accent/15 flex items-center justify-center shrink-0">
            <Building2 size={22} className="text-accent" />
          </div>
          <div>
            <h2 className="font-display text-lg font-semibold text-ink">Welcome back, {user?.fullName}</h2>
            <p className="text-sm text-ink-muted mt-0.5">
              Here's everything AI City Brain has for Mumbai, in one place.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-2 space-y-4">
            <div className="panel p-5">
              <p className="label mb-3 flex items-center gap-1.5"><MapPin size={12} /> Your location</p>
              <LocationPicker />
            </div>
            {location && <WeatherPanel lat={location.lat} lng={location.lng} />}
          </div>
          <div className="lg:col-span-3">
            <div className="flex items-center justify-between mb-3">
              <p className="label">Live map preview</p>
              <Link to="/citymap" className="text-xs text-accent-soft hover:underline flex items-center gap-1">
                Open full map <ArrowRight size={12} />
              </Link>
            </div>
            <MumbaiMap selected={location} heightClassName="h-64 sm:h-80" />
          </div>
        </div>

        <div>
          <p className="label mb-3">Explore</p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {QUICK_LINKS.map(({ to, icon: Icon, title, text }) => (
              <Link key={to} to={to} className="panel-3d p-5 block">
                <div className="h-9 w-9 rounded-lg bg-accent/15 flex items-center justify-center mb-3">
                  <Icon size={17} className="text-accent" />
                </div>
                <h3 className="text-sm font-semibold text-ink mb-1">{title}</h3>
                <p className="text-xs text-ink-muted leading-relaxed">{text}</p>
              </Link>
            ))}
          </div>
        </div>
      </main>
    </div>
  )
}
