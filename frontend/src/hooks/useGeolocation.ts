import { useCallback, useState } from 'react'

export type GeolocationStatus = 'idle' | 'requesting' | 'granted' | 'denied' | 'unavailable' | 'timeout'

interface GeolocationState {
  status: GeolocationStatus
  lat: number | null
  lng: number | null
  accuracyM: number | null
  errorMessage: string | null
}

/**
 * Thin wrapper over the real browser Geolocation API. Never fakes a location —
 * every state here (denied, unavailable, timeout) is a genuine outcome the UI
 * should show honestly rather than silently falling back to a guessed city.
 */
export function useGeolocation() {
  const [state, setState] = useState<GeolocationState>({
    status: 'idle',
    lat: null,
    lng: null,
    accuracyM: null,
    errorMessage: null,
  })

  const request = useCallback(() => {
    if (!('geolocation' in navigator)) {
      setState((s) => ({ ...s, status: 'unavailable', errorMessage: 'Geolocation is not supported by this browser.' }))
      return
    }
    if (!window.isSecureContext) {
      setState((s) => ({ ...s, status: 'unavailable', errorMessage: 'Location access requires a secure (HTTPS) connection.' }))
      return
    }

    setState((s) => ({ ...s, status: 'requesting', errorMessage: null }))

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setState({
          status: 'granted',
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          accuracyM: position.coords.accuracy,
          errorMessage: null,
        })
      },
      (error) => {
        let status: GeolocationStatus = 'unavailable'
        let message = 'Could not determine your location.'
        if (error.code === error.PERMISSION_DENIED) {
          status = 'denied'
          message = 'Location permission was denied. You can still search for a place manually.'
        } else if (error.code === error.POSITION_UNAVAILABLE) {
          status = 'unavailable'
          message = 'Your device could not determine a location right now.'
        } else if (error.code === error.TIMEOUT) {
          status = 'timeout'
          message = 'Location request timed out — please try again.'
        }
        setState((s) => ({ ...s, status, errorMessage: message }))
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 60000 }
    )
  }, [])

  const reset = useCallback(() => {
    setState({ status: 'idle', lat: null, lng: null, accuracyM: null, errorMessage: null })
  }, [])

  return { ...state, request, reset }
}
