import { useCallback, useEffect, useState } from 'react'
import { Sparkles, Siren, AlertTriangle, Gauge, Wind, Droplets } from 'lucide-react'
import { api } from '../lib/api'
import type { Alert, CityStatus, DecisionFeedItem } from '../types'
import { useWebSocketChannel } from '../hooks/useWebSocket'
import { RiskGauge } from '../components/RiskGauge'
import { ZoneCard } from '../components/ZoneCard'
import { AgentBadge, EmptyState, PanelHeader, SeverityBadge, Spinner } from '../components/common'
import { round, timeAgo } from '../lib/utils'

function StatChip({ icon: Icon, label, value }: { icon: typeof Gauge; label: string; value: string }) {
  return (
    <div className="panel px-4 py-3 flex items-center gap-3">
      <div className="h-8 w-8 rounded-lg bg-raised flex items-center justify-center text-accent">
        <Icon size={15} />
      </div>
      <div>
        <p className="font-mono text-sm font-semibold text-ink leading-none">{value}</p>
        <p className="text-[11px] text-ink-faint mt-1">{label}</p>
      </div>
    </div>
  )
}

export default function CommandCenterPage() {
  const [status, setStatus] = useState<CityStatus | null>(null)
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [decisions, setDecisions] = useState<DecisionFeedItem[]>([])
  const [briefing, setBriefing] = useState<string | null>(null)
  const [briefingLoading, setBriefingLoading] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([api.cityStatus(), api.alerts(), api.decisions()])
      .then(([s, a, d]) => {
        setStatus(s)
        setAlerts(a)
        setDecisions(d)
      })
      .finally(() => setLoading(false))
  }, [])

  useWebSocketChannel<CityStatus>('city-status', useCallback((payload) => setStatus(payload), []))
  useWebSocketChannel<DecisionFeedItem[]>('decisions', useCallback((payload) => setDecisions(payload), []))
  useWebSocketChannel<Alert>('alert', useCallback((payload) => setAlerts((prev) => [payload, ...prev].slice(0, 50)), []))

  async function generateBriefing() {
    setBriefingLoading(true)
    try {
      const res = await api.cityBriefing()
      setBriefing(res.briefing)
    } finally {
      setBriefingLoading(false)
    }
  }

  if (loading || !status) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6 max-w-[1600px] mx-auto">
      {/* Hero */}
      <section className="panel p-6 flex flex-col lg:flex-row items-center gap-8">
        <RiskGauge score={status.overallRiskScore} level={status.overallRiskLevel} />

        <div className="flex-1 w-full">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h1 className="font-display text-xl font-semibold text-ink">{status.name}</h1>
              <p className="text-sm text-ink-muted">{status.population.toLocaleString()} residents · updated {timeAgo(status.timestamp)}</p>
            </div>
            <button onClick={generateBriefing} disabled={briefingLoading} className="btn-secondary">
              <Sparkles size={15} className={briefingLoading ? 'animate-pulse' : ''} />
              {briefingLoading ? 'Thinking…' : 'AI Briefing'}
            </button>
          </div>

          {briefing && (
            <div className="rounded-lg bg-accent/10 border border-accent/20 px-4 py-3 mb-4 text-sm text-ink leading-relaxed">
              {briefing}
            </div>
          )}

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <StatChip icon={Siren} label="Active incidents" value={String(status.activeIncidents)} />
            <StatChip icon={AlertTriangle} label="Critical alerts" value={String(status.criticalAlerts)} />
            <StatChip icon={Gauge} label="Avg traffic" value={`${round(status.trafficAvg)}%`} />
            <StatChip icon={Wind} label="Avg AQI" value={String(round(status.aqiAvg))} />
          </div>
        </div>
      </section>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Zones */}
        <div className="xl:col-span-2">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-display text-sm font-semibold text-ink-muted uppercase tracking-wide">Zones</h2>
            <span className="text-xs text-ink-faint flex items-center gap-1">
              <Droplets size={12} /> avg water supply {round(status.waterSupplyAvg)}%
            </span>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {status.zones.map((zone) => (
              <ZoneCard key={zone.id} zone={zone} />
            ))}
          </div>
        </div>

        {/* Feeds */}
        <div className="space-y-6">
          <div className="panel">
            <PanelHeader title="Active Alerts" eyebrow="Live" />
            <div className="max-h-72 overflow-y-auto divide-y divide-hairline">
              {alerts.length === 0 && <EmptyState title="No alerts" description="NovaCity is calm right now." />}
              {alerts.map((a) => (
                <div key={a.id} className="px-5 py-3">
                  <div className="flex items-center justify-between mb-1">
                    <SeverityBadge level={a.severity} />
                    <span className="text-[11px] text-ink-faint">{timeAgo(a.createdAt)}</span>
                  </div>
                  <p className="text-sm font-medium text-ink">{a.title}</p>
                  <p className="text-xs text-ink-muted mt-0.5">{a.message}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="panel">
            <PanelHeader title="AI Decisions" eyebrow="Agent activity" />
            <div className="max-h-96 overflow-y-auto divide-y divide-hairline">
              {decisions.length === 0 && <EmptyState title="No agent activity yet" />}
              {decisions.map((d) => (
                <div key={d.id} className="px-5 py-3">
                  <div className="flex items-center justify-between mb-1">
                    <AgentBadge type={d.agentType} />
                    <span className="text-[11px] text-ink-faint">{timeAgo(d.createdAt)}</span>
                  </div>
                  <p className="text-xs text-ink-muted">{d.summary}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
