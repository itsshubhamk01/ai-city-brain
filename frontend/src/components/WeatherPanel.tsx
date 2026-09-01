import { useEffect, useState } from 'react'
import { Sun, Cloud, CloudRain, CloudSnow, CloudLightning, CloudFog, CloudDrizzle, Droplets, Wind, Gauge, Eye } from 'lucide-react'
import { api } from '../lib/api'
import type { WeatherResponse } from '../types'
import { Spinner } from './common'
import { round, timeAgo } from '../lib/utils'

function conditionIcon(condition: string | null) {
  if (!condition) return Cloud
  const c = condition.toLowerCase()
  if (c.includes('clear')) return Sun
  if (c.includes('thunder')) return CloudLightning
  if (c.includes('snow')) return CloudSnow
  if (c.includes('drizzle')) return CloudDrizzle
  if (c.includes('rain')) return CloudRain
  if (c.includes('fog')) return CloudFog
  return Cloud
}

export function WeatherPanel({ lat, lng }: { lat: number; lng: number }) {
  const [weather, setWeather] = useState<WeatherResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    api.weather(lat, lng).then((w) => {
      if (!cancelled) setWeather(w)
    }).finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [lat, lng])

  if (loading) {
    return (
      <div className="panel p-6 flex items-center justify-center h-48">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  if (!weather || !weather.available || !weather.current) {
    return (
      <div className="panel p-6 text-center">
        <p className="text-sm text-ink-muted">Live weather data unavailable for this location.</p>
        {weather?.unavailableReason && <p className="text-xs text-ink-faint mt-1">{weather.unavailableReason}</p>}
      </div>
    )
  }

  const { current } = weather
  const Icon = conditionIcon(current.condition)

  return (
    <div className="panel p-6">
      <div className="flex items-center justify-between mb-5">
        <span className="inline-flex items-center gap-1.5 text-[11px] font-medium text-signal-nominal">
          <span className="h-1.5 w-1.5 rounded-full bg-signal-nominal animate-pulse-dot" />
          LIVE — updated {timeAgo(weather.fetchedAt)}
        </span>
        <span className="text-[11px] text-ink-faint">Source: {weather.source}</span>
      </div>

      <div className="flex items-center gap-4 mb-5">
        <Icon size={44} className="text-accent shrink-0" />
        <div>
          <p className="font-display text-4xl font-semibold text-ink leading-none">
            {current.temperatureC != null ? `${round(current.temperatureC)}°C` : '—'}
          </p>
          <p className="text-sm text-ink-muted mt-1">
            {current.condition ?? 'Condition unavailable'}
            {current.feelsLikeC != null && ` · Feels like ${round(current.feelsLikeC)}°C`}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <Metric icon={Droplets} label="Humidity" value={current.humidityPct != null ? `${current.humidityPct}%` : '—'} />
        <Metric icon={Wind} label="Wind" value={current.windSpeedKmh != null ? `${round(current.windSpeedKmh)} km/h` : '—'} />
        <Metric icon={Gauge} label="Pressure" value={current.pressureHpa != null ? `${round(current.pressureHpa)} hPa` : '—'} />
        <Metric icon={Eye} label="Precipitation" value={current.precipitationMm != null ? `${current.precipitationMm} mm` : '—'} />
      </div>

      {weather.daily.length > 0 && (
        <div className="mt-6 pt-5 border-t border-hairline">
          <p className="label mb-3">7-Day Forecast</p>
          <div className="grid grid-cols-4 sm:grid-cols-7 gap-2">
            {weather.daily.map((day) => {
              const DayIcon = conditionIcon(day.condition)
              const dayLabel = new Date(day.date).toLocaleDateString('en-IN', { weekday: 'short' })
              return (
                <div key={day.date} className="flex flex-col items-center gap-1 text-center">
                  <span className="text-[11px] text-ink-faint">{dayLabel}</span>
                  <DayIcon size={18} className="text-ink-muted" />
                  <span className="text-xs font-mono text-ink">
                    {day.tempMaxC != null ? round(day.tempMaxC) : '—'}°
                  </span>
                  <span className="text-[10px] font-mono text-ink-faint">
                    {day.tempMinC != null ? round(day.tempMinC) : '—'}°
                  </span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

function Metric({ icon: Icon, label, value }: { icon: typeof Droplets; label: string; value: string }) {
  return (
    <div className="rounded-lg bg-raised px-3 py-2.5 flex items-center gap-2.5">
      <Icon size={15} className="text-ink-faint shrink-0" />
      <div>
        <p className="text-[10px] text-ink-faint">{label}</p>
        <p className="text-sm font-mono text-ink">{value}</p>
      </div>
    </div>
  )
}
