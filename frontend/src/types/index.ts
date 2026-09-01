export type Role =
  | 'ADMIN'
  | 'OPERATIONS_MANAGER'
  | 'EMERGENCY_RESPONDER'
  | 'TRAFFIC_MANAGER'
  | 'ANALYST'
  | 'CITIZEN'

export type Severity = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL'
export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL'
export type AgentType = 'TRAFFIC' | 'EMERGENCY' | 'FLOOD' | 'WASTE' | 'ENERGY' | 'HEALTHCARE' | 'CITY_BRAIN'

export interface LoginResponse {
  token: string
  username: string
  fullName: string
  role: Role
  expiresAt: string
}

export interface CurrentUser {
  username: string
  fullName: string
  email: string
  role: Role
}

export interface ZoneStatus {
  id: string
  name: string
  kind: string
  centerLat: number
  centerLng: number
  population: number
  trafficLevel: number
  rainfallMm: number
  floodRiskScore: number
  powerDemandMw: number
  powerSupplyMw: number
  hospitalOccupancyPct: number
  wasteLevelPct: number
  aqi: number
  waterSupplyPct: number
  riskScore: number
}

export interface CityStatus {
  cityId: string
  name: string
  population: number
  zones: ZoneStatus[]
  overallRiskLevel: RiskLevel
  overallRiskScore: number
  activeIncidents: number
  criticalAlerts: number
  trafficAvg: number
  aqiAvg: number
  waterSupplyAvg: number
  powerAvg: number
  hospitalOccupancyAvg: number
  timestamp: string
}

export interface ZoneHistoryPoint {
  recordedAt: string
  trafficLevel: number
  floodRiskScore: number
  riskScore: number
  aqi: number
  powerDemandMw: number
  powerSupplyMw: number
}

export interface Incident {
  id: string
  type: string
  severity: Severity
  status: 'REPORTED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED'
  zoneId: string
  zoneName: string
  description: string
  lat: number
  lng: number
  assignedAgent: AgentType | null
  createdAt: string
  resolvedAt: string | null
}

export interface Alert {
  id: string
  severity: Severity
  title: string
  message: string
  zoneName: string
  source: string
  acknowledged: boolean
  createdAt: string
}

export interface DecisionFeedItem {
  id: string
  agentType: AgentType
  category: 'EVENT' | 'ACTION'
  label: string
  summary: string
  severity: Severity | null
  zoneName: string
  createdAt: string
}

export interface RoadDto {
  id: string
  name: string
  startLat: number
  startLng: number
  endLat: number
  endLng: number
  status: 'OPEN' | 'CONGESTED' | 'CLOSED'
  congestionPct: number
}

export interface HospitalDto {
  id: string
  name: string
  lat: number
  lng: number
  totalBeds: number
  occupiedBeds: number
  occupancyPct: number
}

export interface AmbulanceDto {
  id: string
  code: string
  lat: number
  lng: number
  status: 'AVAILABLE' | 'DISPATCHED' | 'EN_ROUTE' | 'AT_HOSPITAL'
}

export interface FireStationDto {
  id: string
  name: string
  lat: number
  lng: number
  totalUnits: number
  availableUnits: number
}

export interface WasteBinDto {
  id: string
  code: string
  lat: number
  lng: number
  capacityPct: number
}

export interface PowerStationDto {
  id: string
  name: string
  lat: number
  lng: number
  capacityMw: number
  currentLoadMw: number
}

export interface WaterStationDto {
  id: string
  name: string
  lat: number
  lng: number
  reservoirLevelPct: number
}

export interface MapData {
  zones: ZoneStatus[]
  roads: RoadDto[]
  hospitals: HospitalDto[]
  ambulances: AmbulanceDto[]
  fireStations: FireStationDto[]
  wasteBins: WasteBinDto[]
  powerStations: PowerStationDto[]
  waterStations: WaterStationDto[]
  incidents: Incident[]
}

export interface SimulationState {
  running: boolean
  rainfall: number
  trafficIntensity: number
  population: number
  powerDemand: number
  emergencyLevel: number
  lastTick: string | null
}

export interface WhatIfRequest {
  rainfallDeltaPct?: number
  trafficDeltaPct?: number
  powerOutageZoneId?: string
  powerOutageDurationMinutes?: number
  freeformQuery?: string
}

export interface WhatIfResponse {
  floodRiskLevel: RiskLevel
  trafficImpactPct: number
  emergencyResponseDeltaMinutes: number
  hospitalsPotentiallyAffected: number
  recommendedActions: string[]
  narrative: string
}

export interface BriefingResponse {
  briefing: string
  generatedAt: string
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  details: string[]
}

// --- Real location & weather (India platform) ---

export interface IndianState {
  name: string
  type: 'STATE' | 'UNION_TERRITORY'
}

export interface LocationSuggestion {
  displayName: string
  lat: number
  lng: number
  city: string | null
  state: string | null
  country: string | null
  type: string | null
}

export interface SearchResponse {
  query: string
  results: LocationSuggestion[]
  available: boolean
}

export interface ReverseGeocodeResponse {
  lat: number
  lng: number
  displayName: string | null
  city: string | null
  district: string | null
  state: string | null
  country: string | null
  available: boolean
}

export interface CurrentConditions {
  temperatureC: number | null
  feelsLikeC: number | null
  humidityPct: number | null
  windSpeedKmh: number | null
  windDirectionDeg: number | null
  pressureHpa: number | null
  precipitationMm: number | null
  condition: string | null
  isDay: boolean | null
}

export interface DailyForecastDay {
  date: string
  tempMinC: number | null
  tempMaxC: number | null
  condition: string | null
  sunrise: string | null
  sunset: string | null
  uvIndexMax: number | null
  precipitationProbabilityMaxPct: number | null
}

export interface HourlyForecastPoint {
  time: string
  temperatureC: number | null
  precipitationProbabilityPct: number | null
  condition: string | null
}

export interface WeatherResponse {
  lat: number
  lng: number
  available: boolean
  unavailableReason: string | null
  current: CurrentConditions | null
  daily: DailyForecastDay[]
  hourly: HourlyForecastPoint[]
  source: string
  fetchedAt: string
}

export interface SelectedLocation {
  lat: number
  lng: number
  label: string
  city: string | null
  state: string | null
  source: 'gps' | 'search' | 'default'
  accuracyM: number | null
}
