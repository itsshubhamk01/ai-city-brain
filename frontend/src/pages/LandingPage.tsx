import { Link } from 'react-router-dom'
import { CloudSun, MapPin, Building2, Sparkles, Siren, ShieldCheck } from 'lucide-react'

const FEATURES = [
  {
    icon: CloudSun,
    title: 'Real-time weather',
    description: 'Live conditions and a 7-day forecast for any location in India, sourced from live meteorological data — never simulated.',
  },
  {
    icon: MapPin,
    title: 'Real location awareness',
    description: 'Uses your actual device location (with permission) or lets you search any city, town, or place across every Indian state and union territory.',
  },
  {
    icon: Siren,
    title: 'Emergency services, honestly sourced',
    description: 'Nearby hospitals, fire and police stations from real, open map data — with clear labeling whenever coverage is incomplete for an area.',
  },
  {
    icon: Sparkles,
    title: 'AI-powered insight',
    description: 'Pattern detection and plain-language analysis of real, collected data — always clearly separated from the underlying facts.',
  },
]

function SkylineIllustration() {
  const bars = [30, 55, 40, 70, 45, 85, 35, 60, 50, 75, 40, 65]
  return (
    <svg viewBox="0 0 400 260" className="w-full h-auto max-w-md mx-auto">
      <defs>
        <linearGradient id="glow" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#5B8DEF" stopOpacity="0.35" />
          <stop offset="100%" stopColor="#5B8DEF" stopOpacity="0" />
        </linearGradient>
      </defs>
      <rect x="0" y="0" width="400" height="260" fill="url(#glow)" opacity="0.4" />
      {bars.map((h, i) => (
        <rect
          key={i}
          x={20 + i * 31}
          y={230 - h * 1.7}
          width="22"
          height={h * 1.7}
          rx="2"
          fill="#16213A"
          stroke="#25314D"
        />
      ))}
      {/* network overlay */}
      {[
        [40, 90], [130, 60], [220, 100], [310, 50], [370, 110], [90, 150], [260, 160],
      ].map(([cx, cy], i) => (
        <circle key={i} cx={cx} cy={cy} r={i % 2 === 0 ? 4 : 3} fill="#5B8DEF" opacity={0.9} />
      ))}
      <g stroke="#5B8DEF" strokeWidth="1" opacity="0.5">
        <line x1="40" y1="90" x2="130" y2="60" />
        <line x1="130" y1="60" x2="220" y2="100" />
        <line x1="220" y1="100" x2="310" y2="50" />
        <line x1="310" y1="50" x2="370" y2="110" />
        <line x1="90" y1="150" x2="220" y2="100" />
        <line x1="220" y1="100" x2="260" y2="160" />
        <line x1="40" y1="90" x2="90" y2="150" />
      </g>
      <circle cx="220" cy="100" r="8" fill="none" stroke="#3DDC97" strokeWidth="2">
        <animate attributeName="r" values="8;16;8" dur="2.5s" repeatCount="indefinite" />
        <animate attributeName="opacity" values="0.8;0;0.8" dur="2.5s" repeatCount="indefinite" />
      </circle>
      <circle cx="220" cy="100" r="4" fill="#3DDC97" />
    </svg>
  )
}

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-base text-ink">
      {/* Header */}
      <header className="border-b border-hairline">
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-accent/15 flex items-center justify-center">
              <Building2 size={17} className="text-accent" />
            </div>
            <span className="font-display text-sm font-semibold">AI City Brain</span>
          </div>
          <nav className="flex items-center gap-5 text-sm">
            <a href="#about" className="text-ink-muted hover:text-ink hidden sm:inline">About</a>
            <Link to="/explore" className="text-ink-muted hover:text-ink">Explore</Link>
            <Link to="/login" className="btn-secondary !py-1.5">Sign in</Link>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section
        className="relative overflow-hidden"
        style={{
          backgroundImage:
            'linear-gradient(#5B8DEF 1px, transparent 1px), linear-gradient(90deg, #5B8DEF 1px, transparent 1px)',
          backgroundSize: '48px 48px',
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-b from-base/40 via-base to-base pointer-events-none" />
        <div className="relative max-w-6xl mx-auto px-6 py-16 lg:py-24 grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-accent/10 border border-accent/20 px-3 py-1 text-xs text-accent-soft font-medium mb-5">
              <span className="h-1.5 w-1.5 rounded-full bg-signal-nominal animate-pulse-dot" /> Live · India-wide
            </span>
            <h1 className="font-display text-3xl sm:text-4xl lg:text-5xl font-semibold leading-tight text-ink">
              AI City Brain
            </h1>
            <p className="text-lg sm:text-xl text-accent-soft font-medium mt-2">
              Intelligent Smart City Digital Twin for India
            </p>
            <p className="text-sm sm:text-base text-ink-muted mt-4 max-w-lg leading-relaxed">
              Real location, real weather, and real emergency-service data for any city, town, or
              village across India — with AI analysis clearly separated from the underlying facts.
              No fabricated numbers, ever.
            </p>
            <div className="flex flex-wrap gap-3 mt-7">
              <Link to="/explore" className="btn-primary !px-5 !py-2.5">
                Explore City Intelligence
              </Link>
              <Link to="/login" className="btn-secondary !px-5 !py-2.5">
                View Live Dashboard
              </Link>
            </div>
          </div>
          <SkylineIllustration />
        </div>
      </section>

      {/* Features */}
      <section className="max-w-6xl mx-auto px-6 py-14">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {FEATURES.map(({ icon: Icon, title, description }) => (
            <div key={title} className="panel p-5">
              <div className="h-9 w-9 rounded-lg bg-accent/15 flex items-center justify-center mb-3">
                <Icon size={17} className="text-accent" />
              </div>
              <h3 className="text-sm font-semibold text-ink mb-1.5">{title}</h3>
              <p className="text-xs text-ink-muted leading-relaxed">{description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* About */}
      <section id="about" className="border-t border-hairline">
        <div className="max-w-3xl mx-auto px-6 py-14">
          <div className="flex items-center gap-2 mb-4">
            <ShieldCheck size={16} className="text-accent" />
            <h2 className="font-display text-lg font-semibold text-ink">About AI City Brain</h2>
          </div>
          <p className="text-sm text-ink-muted leading-relaxed mb-4">
            AI City Brain is a real-time location intelligence platform built for India — from major
            metros down to towns and villages where reliable open data exists. It combines live
            weather, real geolocation, and openly-sourced emergency-service data with an AI analysis
            layer that highlights patterns in what's actually observed, never in what's invented.
          </p>
          <p className="text-sm text-ink-muted leading-relaxed">
            The platform is built to be honest by design: whenever live data genuinely isn't available
            for a location — traffic in smaller towns, for example — the interface says so plainly
            instead of guessing.
          </p>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-hairline">
        <div className="max-w-6xl mx-auto px-6 py-8 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-xs text-ink-faint">AI City Brain — Intelligent Smart City Digital Twin for India</p>
          <p className="text-xs text-ink-faint">Created by Shubham Tulashidas Kadam</p>
        </div>
      </footer>
    </div>
  )
}
