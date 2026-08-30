import { useEffect, useState } from 'react'
import { CloudRain, Car, Zap, Siren, RotateCcw, Play, Pause, Wand2 } from 'lucide-react'
import { api } from '../lib/api'
import type { CityStatus, SimulationState, WhatIfResponse } from '../types'
import { useAuth } from '../hooks/useAuth'
import { OPERATOR_ROLES, CAN_USE_WHATIF } from '../lib/constants'
import { PanelHeader, SeverityBadge, Spinner } from '../components/common'
import { cx, round } from '../lib/utils'

type SliderKey = 'rainfall' | 'trafficIntensity' | 'population' | 'powerDemand' | 'emergencyLevel'

const SLIDERS: { key: SliderKey; label: string; icon: typeof CloudRain }[] = [
  { key: 'rainfall', label: 'Rainfall Intensity', icon: CloudRain },
  { key: 'trafficIntensity', label: 'Traffic Intensity', icon: Car },
  { key: 'population', label: 'Population Pressure', icon: Zap },
  { key: 'powerDemand', label: 'Power Demand', icon: Zap },
  { key: 'emergencyLevel', label: 'Emergency Level', icon: Siren },
]

const SCENARIOS = [
  { key: 'HEAVY_RAIN', label: 'Heavy Rain', description: 'Push rainfall city-wide — watch Riverside District flood risk.', color: '#22D3EE' },
  { key: 'MAJOR_ACCIDENT', label: 'Major Accident', description: 'Spawn a high-severity crash in Downtown Core.', color: '#F5A623' },
  { key: 'POWER_OUTAGE', label: 'Power Outage', description: 'Cut supply in Industrial Park and watch Energy Agent react.', color: '#C084FC' },
  { key: 'MASS_EMERGENCY', label: 'Mass Emergency', description: 'Two simultaneous medical emergencies across the city.', color: '#FF5D5D' },
  { key: 'NORMAL', label: 'Reset to Normal', description: 'Return every slider to its calm baseline.', color: '#3DDC97' },
]

export default function SimulationPage() {
  const { user } = useAuth()
  const canOperate = user ? OPERATOR_ROLES.includes(user.role) : false
  const canWhatIf = user ? CAN_USE_WHATIF.includes(user.role) : false

  const [state, setState] = useState<SimulationState | null>(null)
  const [sliders, setSliders] = useState<Record<SliderKey, number>>({
    rainfall: 20, trafficIntensity: 35, population: 50, powerDemand: 40, emergencyLevel: 10,
  })
  const [applying, setApplying] = useState(false)
  const [scenarioBusy, setScenarioBusy] = useState<string | null>(null)

  useEffect(() => {
    function refresh() {
      api.simulationState().then((s) => {
        setState(s)
        setSliders({
          rainfall: s.rainfall, trafficIntensity: s.trafficIntensity, population: s.population,
          powerDemand: s.powerDemand, emergencyLevel: s.emergencyLevel,
        })
      })
    }
    refresh()
    const id = setInterval(refresh, 5000)
    return () => clearInterval(id)
  }, [])

  async function applySliders() {
    setApplying(true)
    try {
      const s = await api.controlSimulation(sliders)
      setState(s)
    } finally {
      setApplying(false)
    }
  }

  async function toggleRunning() {
    const s = state?.running ? await api.stopSimulation() : await api.startSimulation()
    setState(s)
  }

  async function runScenario(key: string) {
    setScenarioBusy(key)
    try {
      const s = await api.triggerScenario(key)
      setState(s)
    } finally {
      setScenarioBusy(null)
    }
  }

  if (!state) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-lg font-semibold text-ink">Simulation Control</h1>
          <p className="text-sm text-ink-muted">Drive NovaCity's live digital twin and watch the agents respond</p>
        </div>
        {canOperate && (
          <button onClick={toggleRunning} className={state.running ? 'btn-secondary' : 'btn-primary'}>
            {state.running ? <Pause size={15} /> : <Play size={15} />}
            {state.running ? 'Pause simulation' : 'Resume simulation'}
          </button>
        )}
      </div>

      {!canOperate && (
        <p className="text-xs text-ink-faint bg-raised rounded-lg px-4 py-2.5">
          Your role can view simulation state. Adjusting sliders and scenarios requires an Operations Manager or Administrator account.
        </p>
      )}

      <div className="panel p-5 space-y-5">
        <PanelHeader title="Live Sliders" eyebrow="Simulation control" />
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-5">
          {SLIDERS.map(({ key, label, icon: Icon }) => (
            <div key={key}>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-medium text-ink-muted flex items-center gap-1.5"><Icon size={13} /> {label}</span>
                <span className="font-mono text-xs text-ink">{round(sliders[key])}%</span>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                disabled={!canOperate}
                value={sliders[key]}
                onChange={(e) => setSliders((s) => ({ ...s, [key]: Number(e.target.value) }))}
                className="w-full accent-accent disabled:opacity-40"
              />
            </div>
          ))}
        </div>
        {canOperate && (
          <button onClick={applySliders} disabled={applying} className="btn-primary">
            {applying ? 'Applying…' : 'Apply Changes'}
          </button>
        )}
      </div>

      <div className="panel p-5 space-y-4">
        <PanelHeader title="Scenario Presets" eyebrow="One-click demo scenarios" />
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {SCENARIOS.map((s) => (
            <button
              key={s.key}
              disabled={!canOperate || scenarioBusy !== null}
              onClick={() => runScenario(s.key)}
              className="text-left rounded-lg border border-hairline bg-raised px-4 py-3 hover:border-accent/50 transition-colors disabled:opacity-40"
            >
              <div className="flex items-center gap-2 mb-1">
                {s.key === 'NORMAL' ? <RotateCcw size={14} style={{ color: s.color }} /> : <span className="h-2 w-2 rounded-full" style={{ backgroundColor: s.color }} />}
                <span className="text-sm font-medium text-ink">{s.label}</span>
                {scenarioBusy === s.key && <span className="text-[11px] text-ink-faint ml-auto">running…</span>}
              </div>
              <p className="text-xs text-ink-faint">{s.description}</p>
            </button>
          ))}
        </div>
      </div>

      {canWhatIf && <WhatIfPanel />}
    </div>
  )
}

function WhatIfPanel() {
  const [rainfallDelta, setRainfallDelta] = useState(0)
  const [trafficDelta, setTrafficDelta] = useState(0)
  const [zoneId, setZoneId] = useState<string>('')
  const [duration, setDuration] = useState(30)
  const [query, setQuery] = useState('')
  const [zones, setZones] = useState<CityStatus['zones']>([])
  const [result, setResult] = useState<WhatIfResponse | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.cityStatus().then((s) => setZones(s.zones))
  }, [])

  async function run() {
    setLoading(true)
    try {
      const res = await api.whatIf({
        rainfallDeltaPct: rainfallDelta || undefined,
        trafficDeltaPct: trafficDelta || undefined,
        powerOutageZoneId: zoneId || undefined,
        powerOutageDurationMinutes: zoneId ? duration : undefined,
        freeformQuery: query || undefined,
      })
      setResult(res)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="panel p-5 space-y-4">
      <PanelHeader title="What If…?" eyebrow="Projects impact without touching live state" action={<Wand2 size={14} className="text-accent" />} />

      <div>
        <label className="label">Describe a scenario (optional)</label>
        <input className="input mt-1.5" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="e.g. What if it rains all weekend?" />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="label">Rainfall change: {rainfallDelta > 0 ? '+' : ''}{rainfallDelta}%</label>
          <input type="range" min={-50} max={150} value={rainfallDelta} onChange={(e) => setRainfallDelta(Number(e.target.value))} className="w-full accent-accent mt-1.5" />
        </div>
        <div>
          <label className="label">Traffic change: {trafficDelta > 0 ? '+' : ''}{trafficDelta}%</label>
          <input type="range" min={-50} max={150} value={trafficDelta} onChange={(e) => setTrafficDelta(Number(e.target.value))} className="w-full accent-accent mt-1.5" />
        </div>
        <div>
          <label className="label">Simulate power outage in</label>
          <select className="input mt-1.5" value={zoneId} onChange={(e) => setZoneId(e.target.value)}>
            <option value="">— none —</option>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>{z.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Outage duration (minutes)</label>
          <input type="number" min={5} max={240} className="input mt-1.5" value={duration} onChange={(e) => setDuration(Number(e.target.value))} disabled={!zoneId} />
        </div>
      </div>

      <button onClick={run} disabled={loading} className="btn-primary">
        {loading ? 'Projecting…' : 'Run What-If'}
      </button>

      {result && (
        <div className="rounded-lg border border-hairline bg-raised p-4 space-y-3">
          <div className="flex items-center gap-4 flex-wrap">
            <div className="flex items-center gap-2">
              <span className="text-xs text-ink-faint">Flood risk</span>
              <SeverityBadge level={result.floodRiskLevel} />
            </div>
            <div className="text-xs text-ink-faint">Traffic impact: <span className="font-mono text-ink">{result.trafficImpactPct > 0 ? '+' : ''}{round(result.trafficImpactPct)}%</span></div>
            <div className="text-xs text-ink-faint">Response delay: <span className="font-mono text-ink">+{result.emergencyResponseDeltaMinutes}m</span></div>
            <div className="text-xs text-ink-faint">Hospitals affected: <span className="font-mono text-ink">{result.hospitalsPotentiallyAffected}</span></div>
          </div>
          <p className="text-sm text-ink leading-relaxed">{result.narrative}</p>
          <ul className="space-y-1">
            {result.recommendedActions.map((a, i) => (
              <li key={i} className={cx('text-xs text-ink-muted pl-3 relative', "before:content-['—'] before:absolute before:left-0")}>{a}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
