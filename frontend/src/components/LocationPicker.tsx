import { useEffect, useState } from 'react'
import { MapPin, Search } from 'lucide-react'
import { api } from '../lib/api'
import { useGeolocation } from '../hooks/useGeolocation'
import { useLocationContext } from '../hooks/useLocationContext'
import type { LocationSuggestion } from '../types'
import { Spinner } from './common'

export function LocationPicker({ onSelected }: { onSelected?: () => void }) {
  const geo = useGeolocation()
  const { setFromCoords, setFromSuggestion, loading: resolving } = useLocationContext()
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<LocationSuggestion[]>([])
  const [searching, setSearching] = useState(false)
  const [searchUnavailable, setSearchUnavailable] = useState(false)

  useEffect(() => {
    if (geo.status === 'granted' && geo.lat != null && geo.lng != null) {
      setFromCoords(geo.lat, geo.lng, geo.accuracyM).then(() => onSelected?.())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [geo.status, geo.lat, geo.lng])

  useEffect(() => {
    if (query.trim().length < 3) {
      setResults([])
      return
    }
    const timer = setTimeout(async () => {
      setSearching(true)
      try {
        const res = await api.searchLocation(query)
        setResults(res.results)
        setSearchUnavailable(!res.available)
      } catch {
        setSearchUnavailable(true)
      } finally {
        setSearching(false)
      }
    }, 500)
    return () => clearTimeout(timer)
  }, [query])

  return (
    <div className="space-y-3 w-full">
      <button
        onClick={geo.request}
        disabled={geo.status === 'requesting' || resolving}
        className="btn-primary w-full"
      >
        <MapPin size={15} />
        {geo.status === 'requesting' || resolving ? 'Detecting your location…' : 'Use my current location'}
      </button>

      {(geo.status === 'denied' || geo.status === 'unavailable' || geo.status === 'timeout') && (
        <p className="text-xs text-signal-watch bg-signal-watch/10 rounded-md px-3 py-2">{geo.errorMessage}</p>
      )}

      <div className="relative">
        <Search size={14} className="absolute left-3 top-3 text-ink-faint" />
        <input
          className="input pl-9"
          placeholder="Or search a city, town, or place in India…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        {searching && <Spinner className="absolute right-3 top-2.5 h-4 w-4" />}
      </div>

      {searchUnavailable && (
        <p className="text-xs text-ink-faint">Location search is temporarily unavailable — please try again shortly.</p>
      )}

      {results.length > 0 && (
        <div className="max-h-64 overflow-y-auto divide-y divide-hairline rounded-lg border border-hairline">
          {results.map((r, i) => (
            <button
              key={i}
              onClick={() => {
                setFromSuggestion(r)
                setQuery('')
                setResults([])
                onSelected?.()
              }}
              className="w-full text-left px-3 py-2.5 hover:bg-raised transition-colors"
            >
              <p className="text-sm text-ink">{r.city || r.displayName.split(',')[0]}</p>
              <p className="text-xs text-ink-faint truncate">{r.displayName}</p>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
