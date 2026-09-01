import type {
  Alert,
  BriefingResponse,
  CityStatus,
  CurrentUser,
  DecisionFeedItem,
  IndianState,
  Incident,
  LoginResponse,
  MapData,
  ReverseGeocodeResponse,
  SearchResponse,
  SimulationState,
  WeatherResponse,
  WhatIfRequest,
  WhatIfResponse,
  ZoneHistoryPoint,
} from '../types'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const TOKEN_KEY = 'aicb_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export class ApiClientError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

type UnauthorizedHandler = () => void
let onUnauthorized: UnauthorizedHandler | null = null
export function registerUnauthorizedHandler(fn: UnauthorizedHandler): void {
  onUnauthorized = fn
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers })

  if (res.status === 401) {
    onUnauthorized?.()
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`
    try {
      const body = await res.json()
      if (body?.message) message = body.message
    } catch {
      /* response had no JSON body — keep the generic message */
    }
    throw new ApiClientError(res.status, message)
  }

  if (res.status === 204) return undefined as T
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const api = {
  login: (username: string, password: string) =>
    request<LoginResponse>('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  me: () => request<CurrentUser>('/api/v1/auth/me'),

  cityStatus: () => request<CityStatus>('/api/v1/city/status'),
  cityMap: () => request<MapData>('/api/v1/city/map'),
  cityBriefing: () => request<BriefingResponse>('/api/v1/city/briefing'),
  zoneHistory: (zoneId: string) => request<ZoneHistoryPoint[]>(`/api/v1/city/zones/${zoneId}/history`),

  incidents: () => request<Incident[]>('/api/v1/incidents'),
  createIncident: (payload: { zoneId: string; type: string; severity: string; description: string; lat: number; lng: number }) =>
    request<Incident>('/api/v1/incidents', { method: 'POST', body: JSON.stringify(payload) }),
  updateIncidentStatus: (id: string, status: string) =>
    request<Incident>(`/api/v1/incidents/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),

  alerts: () => request<Alert[]>('/api/v1/alerts'),
  acknowledgeAlert: (id: string) => request<Alert>(`/api/v1/alerts/${id}/acknowledge`, { method: 'PATCH' }),

  decisions: () => request<DecisionFeedItem[]>('/api/v1/agents/decisions'),

  simulationState: () => request<SimulationState>('/api/v1/simulation/state'),
  startSimulation: () => request<SimulationState>('/api/v1/simulation/start', { method: 'POST' }),
  stopSimulation: () => request<SimulationState>('/api/v1/simulation/stop', { method: 'POST' }),
  controlSimulation: (payload: Partial<Record<'rainfall' | 'trafficIntensity' | 'population' | 'powerDemand' | 'emergencyLevel', number>>) =>
    request<SimulationState>('/api/v1/simulation/control', { method: 'PATCH', body: JSON.stringify(payload) }),
  triggerScenario: (scenarioKey: string) =>
    request<SimulationState>('/api/v1/simulation/scenario', { method: 'POST', body: JSON.stringify({ scenarioKey }) }),

  whatIf: (payload: WhatIfRequest) => request<WhatIfResponse>('/api/v1/whatif', { method: 'POST', body: JSON.stringify(payload) }),

  // --- Real location & weather (public, no auth needed) ---
  indianStates: () => request<IndianState[]>('/api/v1/geo/states'),
  searchLocation: (q: string) => request<SearchResponse>(`/api/v1/geo/search?q=${encodeURIComponent(q)}`),
  reverseGeocode: (lat: number, lng: number) =>
    request<ReverseGeocodeResponse>(`/api/v1/geo/reverse?lat=${lat}&lng=${lng}`),
  weather: (lat: number, lng: number) => request<WeatherResponse>(`/api/v1/weather?lat=${lat}&lng=${lng}`),
}

export function wsUrl(): string {
  const base = import.meta.env.VITE_WS_BASE_URL || API_BASE.replace(/^http/, 'ws')
  return `${base}/ws/city`
}
