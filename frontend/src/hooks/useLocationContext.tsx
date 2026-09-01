import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { api } from '../lib/api'
import type { LocationSuggestion, SelectedLocation } from '../types'

const SESSION_KEY = 'aicb_selected_location'

interface LocationContextValue {
  location: SelectedLocation | null
  loading: boolean
  setFromCoords: (lat: number, lng: number, accuracyM: number | null) => Promise<void>
  setFromSuggestion: (suggestion: LocationSuggestion) => void
  clear: () => void
}

const LocationContext = createContext<LocationContextValue | undefined>(undefined)

/**
 * Holds the single "currently selected location" the whole app uses (weather,
 * nearby services, etc). Persisted only to sessionStorage — cleared when the tab
 * closes — rather than indefinitely, per the "don't unnecessarily store precise
 * location history" principle.
 */
export function LocationProvider({ children }: { children: ReactNode }) {
  const [location, setLocation] = useState<SelectedLocation | null>(() => {
    try {
      const raw = sessionStorage.getItem(SESSION_KEY)
      return raw ? (JSON.parse(raw) as SelectedLocation) : null
    } catch {
      return null
    }
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    try {
      if (location) sessionStorage.setItem(SESSION_KEY, JSON.stringify(location))
      else sessionStorage.removeItem(SESSION_KEY)
    } catch {
      /* storage unavailable — non-fatal, location just won't persist across a refresh */
    }
  }, [location])

  const setFromCoords = useCallback(async (lat: number, lng: number, accuracyM: number | null) => {
    setLoading(true)
    try {
      const result = await api.reverseGeocode(lat, lng)
      setLocation({
        lat,
        lng,
        label: result.available && result.city ? `${result.city}, ${result.state ?? ''}`.trim() : 'Current location',
        city: result.city,
        state: result.state,
        source: 'gps',
        accuracyM,
      })
    } catch {
      setLocation({ lat, lng, label: 'Current location', city: null, state: null, source: 'gps', accuracyM })
    } finally {
      setLoading(false)
    }
  }, [])

  const setFromSuggestion = useCallback((suggestion: LocationSuggestion) => {
    setLocation({
      lat: suggestion.lat,
      lng: suggestion.lng,
      label: suggestion.city ? `${suggestion.city}, ${suggestion.state ?? ''}`.trim() : suggestion.displayName,
      city: suggestion.city,
      state: suggestion.state,
      source: 'search',
      accuracyM: null,
    })
  }, [])

  const clear = useCallback(() => setLocation(null), [])

  return (
    <LocationContext.Provider value={{ location, loading, setFromCoords, setFromSuggestion, clear }}>
      {children}
    </LocationContext.Provider>
  )
}

export function useLocationContext(): LocationContextValue {
  const ctx = useContext(LocationContext)
  if (!ctx) throw new Error('useLocationContext must be used within a LocationProvider')
  return ctx
}
