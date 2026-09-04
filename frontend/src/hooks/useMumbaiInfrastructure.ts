import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import type { InfrastructureResponse } from '../types'

/** Fetches the real Mumbai hospitals/police/fire-station data once and shares it. */
export function useMumbaiInfrastructure() {
  const [data, setData] = useState<InfrastructureResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    api.mumbaiInfrastructure()
      .then((res) => !cancelled && setData(res))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [])

  return { data, loading }
}
