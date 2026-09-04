import { PageHeader } from '../components/PageHeader'
import { LocationPicker } from '../components/LocationPicker'
import { WeatherPanel } from '../components/WeatherPanel'
import { useLocationContext } from '../hooks/useLocationContext'
import { CloudSun } from 'lucide-react'

export default function WeatherDetailPage() {
  const { location } = useLocationContext()

  return (
    <div className="min-h-screen bg-base text-ink">
      <PageHeader title="Weather" subtitle="Live conditions from Open-Meteo" />

      <main className="max-w-2xl mx-auto px-4 sm:px-6 py-8 space-y-5">
        <div className="panel p-5">
          <p className="label mb-3">Location</p>
          <LocationPicker />
        </div>

        {location ? (
          <WeatherPanel lat={location.lat} lng={location.lng} />
        ) : (
          <div className="panel p-10 text-center">
            <CloudSun size={28} className="mx-auto text-ink-faint mb-3" />
            <p className="text-sm text-ink-muted">Pick a location above to see live weather.</p>
          </div>
        )}
      </main>
    </div>
  )
}
