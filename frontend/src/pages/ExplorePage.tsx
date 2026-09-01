import { useEffect, useState } from 'react'
import { MapPin, Navigation, Siren, TrafficCone, ChevronDown } from 'lucide-react'
import { useLocationContext } from '../hooks/useLocationContext'
import { LocationPicker } from '../components/LocationPicker'
import { WeatherPanel } from '../components/WeatherPanel'
import { cx } from '../lib/utils'

export default function ExplorePage() {
  const { location, clear } = useLocationContext()
  const [pickerOpen, setPickerOpen] = useState(!location)

  useEffect(() => {
    if (!location) setPickerOpen(true)
  }, [location])

  return (
    <div className="min-h-screen bg-base text-ink">
      <header className="border-b border-hairline px-6 py-4 flex items-center justify-between max-w-5xl mx-auto">
        <a href="/" className="font-display text-sm font-semibold text-ink">AI City Brain</a>
        <a href="/login" className="text-xs text-ink-muted hover:text-ink">Sign in →</a>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-8 space-y-6">
        <div>
          <h1 className="font-display text-xl font-semibold text-ink">City Intelligence</h1>
          <p className="text-sm text-ink-muted mt-1">Real, live conditions for any location in India — never simulated.</p>
        </div>

        {/* Location selector */}
        <div className="panel p-5">
          <button
            onClick={() => setPickerOpen((o) => !o)}
            className="w-full flex items-center justify-between text-left"
          >
            <div className="flex items-center gap-3 min-w-0">
              <div className="h-9 w-9 rounded-lg bg-accent/15 flex items-center justify-center shrink-0">
                <MapPin size={16} className="text-accent" />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-medium text-ink truncate">
                  {location ? location.label : 'Choose a location to get started'}
                </p>
                {location && (
                  <p className="text-xs text-ink-faint">
                    {location.lat.toFixed(4)}, {location.lng.toFixed(4)}
                    {location.accuracyM != null && ` · accuracy ~${Math.round(location.accuracyM)}m`}
                    {location.source === 'gps' && ' · from your device'}
                    {location.source === 'search' && ' · searched'}
                  </p>
                )}
              </div>
            </div>
            <ChevronDown size={18} className={cx('text-ink-faint transition-transform shrink-0', pickerOpen && 'rotate-180')} />
          </button>

          {pickerOpen && (
            <div className="mt-4 pt-4 border-t border-hairline">
              <LocationPicker onSelected={() => setPickerOpen(false)} />
              {location && (
                <button onClick={clear} className="text-xs text-ink-faint hover:text-signal-critical mt-3">
                  Clear saved location
                </button>
              )}
            </div>
          )}
        </div>

        {location && (
          <>
            {/* Weather */}
            <WeatherPanel lat={location.lat} lng={location.lng} />

            {/* Honest placeholders for what's not built yet */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="panel p-5 flex items-start gap-3">
                <TrafficCone size={18} className="text-ink-faint mt-0.5 shrink-0" />
                <div>
                  <p className="text-sm font-medium text-ink">Traffic</p>
                  <p className="text-xs text-ink-muted mt-1">
                    Live traffic data is currently unavailable for this location. No free, comprehensive
                    real-time traffic source covers India at this depth yet — we show this honestly
                    rather than estimating.
                  </p>
                </div>
              </div>
              <div className="panel p-5 flex items-start gap-3">
                <Siren size={18} className="text-ink-faint mt-0.5 shrink-0" />
                <div>
                  <p className="text-sm font-medium text-ink">Nearby hospitals, fire &amp; police</p>
                  <p className="text-xs text-ink-muted mt-1">
                    Real OpenStreetMap-sourced emergency service locations are coming in the next
                    update for this location.
                  </p>
                </div>
              </div>
            </div>
          </>
        )}

        {!location && !pickerOpen && (
          <div className="panel p-10 text-center">
            <Navigation size={28} className="mx-auto text-ink-faint mb-3" />
            <p className="text-sm text-ink-muted">Pick a location above to see live conditions.</p>
          </div>
        )}
      </main>
    </div>
  )
}
