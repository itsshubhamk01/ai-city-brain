import { Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useLocationContext } from '../hooks/useLocationContext'
import { LocationPicker } from '../components/LocationPicker'
import { WeatherPanel } from '../components/WeatherPanel'
import { MumbaiMap } from '../components/MumbaiMap'
import { AuthPanel } from '../components/AuthPanel'
import {
  Building2, MapPin, Sparkles, ShieldCheck, CloudSun, UserCheck,
  Brain, Radio, Zap, Network, LogOut,
} from 'lucide-react'

function MumbaiIllustration() {
  return (
    <svg viewBox="0 0 400 260" className="w-full h-auto max-w-md mx-auto">
      <defs>
        <linearGradient id="glow2" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#5B8DEF" stopOpacity="0.35" />
          <stop offset="100%" stopColor="#5B8DEF" stopOpacity="0" />
        </linearGradient>
      </defs>
      <rect x="0" y="0" width="400" height="260" fill="url(#glow2)" opacity="0.4" />
      {[28, 50, 34, 66, 40, 78, 32, 56, 44, 70, 36].map((h, i) => (
        <rect key={i} x={20 + i * 34} y={230 - h * 1.6} width="24" height={h * 1.6} rx="2" fill="#16213A" stroke="#25314D" />
      ))}
      <path d="M 40 200 Q 200 150 360 200" stroke="#25314D" strokeWidth="4" fill="none" />
      <line x1="200" y1="150" x2="200" y2="90" stroke="#5B8DEF" strokeWidth="2" opacity="0.7" />
      {[120, 160, 240, 280].map((x, i) => (
        <line key={i} x1={x} y1={200 - Math.abs(200 - x) * 0.15} x2="200" y2="95" stroke="#5B8DEF" strokeWidth="1" opacity="0.4" />
      ))}
      <circle cx="200" cy="90" r="4" fill="#3DDC97" />
      <circle cx="200" cy="90" r="9" fill="none" stroke="#3DDC97" strokeWidth="2">
        <animate attributeName="r" values="9;17;9" dur="2.5s" repeatCount="indefinite" />
        <animate attributeName="opacity" values="0.8;0;0.8" dur="2.5s" repeatCount="indefinite" />
      </circle>
    </svg>
  )
}

const STAT_BADGES = ['Real live weather', 'Real GPS location', 'Zero fake data', 'Free, always']

const EXPLAIN_ITEMS = [
  {
    icon: MapPin,
    title: 'What it is',
    text: 'A real-time information platform for Mumbai \u2014 your actual location, real live weather, and an interactive city map, all on one page.',
  },
  {
    icon: CloudSun,
    title: 'Why use it',
    text: "Instead of checking five different apps, get Mumbai's live conditions and your position on the map in one glance \u2014 with nothing invented or simulated.",
  },
  {
    icon: ShieldCheck,
    title: "Why it's needed",
    text: 'Mumbai is a huge, fast-changing city. Honest, real-time information \u2014 including saying "unavailable" when data genuinely isn\u2019t there \u2014 helps people decide faster.',
  },
  {
    icon: Sparkles,
    title: 'What you get',
    text: 'Free, live weather and location tools today, with real emergency-service locations and AI-powered insights arriving in upcoming updates.',
  },
]

const BRAIN_ITEMS = [
  { icon: Radio, title: 'Senses', text: 'Your device\u2019s GPS and Mumbai\u2019s live weather sensors act like the city\u2019s senses \u2014 constantly picking up what\u2019s happening right now.' },
  { icon: Network, title: 'Nerves', text: 'That information travels over the internet the way a nerve signal travels through your body \u2014 fast, and to the right place.' },
  { icon: Brain, title: 'The brain', text: 'A central layer \u2014 the server and AI \u2014 receives everything, makes sense of it, and decides what to show you.' },
  { icon: Zap, title: 'The reflex', text: 'Just like pulling your hand back from something hot happens before you consciously think about it, the goal is a city platform that reacts to real conditions quickly, not on a delay.' },
]

export default function LandingPage() {
  const { user, logout } = useAuth()
  const { location } = useLocationContext()

  return (
    <div className="min-h-screen bg-base text-ink">
      <header className="border-b border-hairline">
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-accent/15 flex items-center justify-center">
              <Building2 size={17} className="text-accent" />
            </div>
            <span className="font-display text-sm font-semibold">AI City Brain</span>
          </div>
          <nav className="flex items-center gap-4 text-sm">
            <a href="#about" className="text-ink-muted hover:text-ink hidden sm:inline">About</a>
            <Link to="/app" className="text-ink-muted hover:text-ink hidden sm:inline">Architecture Demo</Link>
            {user ? (
              <div className="flex items-center gap-3">
                <span className="text-ink-muted hidden sm:inline">Namaste, {user.fullName.split(' ')[0]}</span>
                <button onClick={logout} className="btn-secondary !py-1.5">
                  <LogOut size={14} /> Sign out
                </button>
              </div>
            ) : (
              <a href="#auth" className="btn-secondary !py-1.5">Sign in</a>
            )}
          </nav>
        </div>
      </header>

      <section
        className="relative overflow-hidden"
        style={{
          backgroundImage: 'linear-gradient(#5B8DEF 1px, transparent 1px), linear-gradient(90deg, #5B8DEF 1px, transparent 1px)',
          backgroundSize: '48px 48px',
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-b from-base/40 via-base to-base pointer-events-none" />
        <div className="relative max-w-6xl mx-auto px-6 py-14 lg:py-20 grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-accent/10 border border-accent/20 px-3 py-1 text-xs text-accent-soft font-medium mb-5">
              <span className="h-1.5 w-1.5 rounded-full bg-signal-nominal animate-pulse-dot" /> Live \u00b7 Mumbai
            </span>
            <h1 className="font-display text-3xl sm:text-4xl lg:text-5xl font-semibold leading-tight text-ink">
              AI City Brain
            </h1>
            <p className="text-lg sm:text-xl text-accent-soft font-medium mt-2">
              Intelligent Smart City Digital Twin for Mumbai
            </p>
            <p className="text-sm sm:text-base text-ink-muted mt-4 max-w-lg leading-relaxed">
              Real location, real weather, and a real live map of Mumbai \u2014 right here on this page.
              No sign-up needed to look around.
            </p>
            <div className="flex flex-wrap gap-3 mt-7">
              <a href="#explore" className="btn-primary !px-5 !py-2.5">Explore Mumbai Now</a>
              {!user && <a href="#auth" className="btn-secondary !px-5 !py-2.5">Sign in</a>}
            </div>
            <div className="flex flex-wrap gap-2 mt-6">
              {STAT_BADGES.map((s) => (
                <span key={s} className="text-[11px] font-mono text-ink-faint bg-raised rounded-full px-3 py-1 border border-hairline">
                  {s}
                </span>
              ))}
            </div>
          </div>
          <MumbaiIllustration />
        </div>
      </section>

      <section id="explore" className="max-w-6xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-2 space-y-4">
            <div className="panel p-5">
              <p className="label mb-3">Your location</p>
              <LocationPicker />
              {location && (
                <p className="text-xs text-ink-faint mt-3">
                  {location.lat.toFixed(4)}, {location.lng.toFixed(4)}
                  {location.accuracyM != null && ` \u00b7 accuracy ~${Math.round(location.accuracyM)}m`}
                </p>
              )}
            </div>
            {location && <WeatherPanel lat={location.lat} lng={location.lng} />}
          </div>
          <div className="lg:col-span-3">
            <p className="label mb-3">Live map of Mumbai</p>
            <MumbaiMap selected={location} />
          </div>
        </div>
      </section>

      <section id="about" className="border-t border-hairline">
        <div className="max-w-6xl mx-auto px-6 py-14">
          <h2 className="font-display text-lg font-semibold text-ink mb-6 text-center">How AI City Brain helps you</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {EXPLAIN_ITEMS.map(({ icon: Icon, title, text }) => (
              <div key={title} className="panel p-5 hover:border-accent/40 transition-colors">
                <div className="h-9 w-9 rounded-lg bg-accent/15 flex items-center justify-center mb-3">
                  <Icon size={17} className="text-accent" />
                </div>
                <h3 className="text-sm font-semibold text-ink mb-1.5">{title}</h3>
                <p className="text-xs text-ink-muted leading-relaxed">{text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Brain-inspired architecture explainer */}
      <section className="border-t border-hairline bg-surface/40">
        <div className="max-w-6xl mx-auto px-6 py-14">
          <div className="text-center mb-8">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-accent/10 border border-accent/20 px-3 py-1 text-xs text-accent-soft font-medium mb-3">
              <Brain size={12} /> The idea behind the name
            </span>
            <h2 className="font-display text-lg font-semibold text-ink">Why "City Brain"?</h2>
            <p className="text-sm text-ink-muted max-w-xl mx-auto mt-2 leading-relaxed">
              The concept is inspired by academic research on brain-like smart-city architecture \u2014
              the idea that a city can sense, think, and react a little like a living body does.
            </p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {BRAIN_ITEMS.map(({ icon: Icon, title, text }, i) => (
              <div key={title} className="relative panel p-5">
                <span className="absolute -top-2.5 -left-2.5 h-6 w-6 rounded-full bg-accent text-white text-[11px] font-mono flex items-center justify-center">
                  {i + 1}
                </span>
                <div className="h-9 w-9 rounded-lg bg-raised flex items-center justify-center mb-3">
                  <Icon size={17} className="text-accent" />
                </div>
                <h3 className="text-sm font-semibold text-ink mb-1.5">{title}</h3>
                <p className="text-xs text-ink-muted leading-relaxed">{text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {!user && (
        <section id="auth" className="border-t border-hairline">
          <div className="max-w-md mx-auto px-6 py-14">
            <div className="flex items-center gap-2 justify-center mb-5">
              <UserCheck size={16} className="text-accent" />
              <h2 className="font-display text-lg font-semibold text-ink">Sign in or create an account</h2>
            </div>
            <AuthPanel />
          </div>
        </section>
      )}

      <footer className="border-t border-hairline">
        <div className="max-w-6xl mx-auto px-6 py-8 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-xs text-ink-faint">AI City Brain \u2014 Intelligent Smart City Digital Twin for Mumbai</p>
          <div className="flex items-center gap-4">
            <Link to="/app" className="text-xs text-ink-faint hover:text-ink-muted">Multi-Agent Architecture Demo</Link>
            <p className="text-xs text-ink-faint">Created by Shubham Tulashidas Kadam</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
