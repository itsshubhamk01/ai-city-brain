import { useCallback, useEffect, useState } from 'react'
import { Plus, X } from 'lucide-react'
import { api } from '../lib/api'
import type { CityStatus, Incident } from '../types'
import { useWebSocketChannel } from '../hooks/useWebSocket'
import { useAuth } from '../hooks/useAuth'
import { CAN_MANAGE_INCIDENTS } from '../lib/constants'
import { EmptyState, PanelHeader, SeverityBadge, Spinner } from '../components/common'
import { timeAgo, titleCase } from '../lib/utils'

const INCIDENT_TYPES = ['TRAFFIC_ACCIDENT', 'FIRE', 'FLOOD', 'MEDICAL_EMERGENCY', 'POWER_OUTAGE', 'WASTE_OVERFLOW', 'INFRASTRUCTURE']
const SEVERITIES = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL']
const NEXT_STATUS: Record<string, string | null> = {
  REPORTED: 'ACKNOWLEDGED',
  ACKNOWLEDGED: 'IN_PROGRESS',
  IN_PROGRESS: 'RESOLVED',
  RESOLVED: null,
}

export default function IncidentsPage() {
  const { user } = useAuth()
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [zones, setZones] = useState<CityStatus['zones']>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const canManage = user ? CAN_MANAGE_INCIDENTS.includes(user.role) : false

  useEffect(() => {
    Promise.all([api.incidents(), api.cityStatus()])
      .then(([inc, status]) => {
        setIncidents(inc)
        setZones(status.zones)
      })
      .finally(() => setLoading(false))
  }, [])

  useWebSocketChannel<Incident>(
    'incident',
    useCallback((payload) => {
      setIncidents((prev) => {
        const withoutThis = prev.filter((i) => i.id !== payload.id)
        return payload.status === 'RESOLVED' ? withoutThis : [payload, ...withoutThis]
      })
    }, [])
  )

  async function advanceStatus(incident: Incident) {
    const next = NEXT_STATUS[incident.status]
    if (!next) return
    const updated = await api.updateIncidentStatus(incident.id, next)
    setIncidents((prev) => (updated.status === 'RESOLVED' ? prev.filter((i) => i.id !== incident.id) : prev.map((i) => (i.id === incident.id ? updated : i))))
  }

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-lg font-semibold text-ink">Incidents</h1>
          <p className="text-sm text-ink-muted">Prioritized by severity, then age</p>
        </div>
        {canManage && (
          <button onClick={() => setShowForm((s) => !s)} className="btn-primary">
            {showForm ? <X size={15} /> : <Plus size={15} />}
            {showForm ? 'Cancel' : 'Report Incident'}
          </button>
        )}
      </div>

      {showForm && (
        <ReportIncidentForm
          zones={zones}
          onCreated={(incident) => {
            setIncidents((prev) => [incident, ...prev])
            setShowForm(false)
          }}
        />
      )}

      <div className="panel">
        <PanelHeader title={`${incidents.length} active`} eyebrow="Live" />
        <div className="divide-y divide-hairline">
          {incidents.length === 0 && <EmptyState title="No active incidents" description="NovaCity is running smoothly." />}
          {incidents.map((incident) => (
            <div key={incident.id} className="px-5 py-4 flex items-center justify-between gap-4">
              <div className="min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <SeverityBadge level={incident.severity} />
                  <span className="text-xs text-ink-faint">{titleCase(incident.status)}</span>
                  {incident.assignedAgent && <span className="text-xs text-ink-faint">· {titleCase(incident.assignedAgent)} Agent</span>}
                </div>
                <p className="text-sm font-medium text-ink truncate">{titleCase(incident.type)} — {incident.zoneName}</p>
                <p className="text-xs text-ink-muted mt-0.5 truncate">{incident.description}</p>
              </div>
              <div className="flex items-center gap-3 shrink-0">
                <span className="text-xs text-ink-faint">{timeAgo(incident.createdAt)}</span>
                {canManage && NEXT_STATUS[incident.status] && (
                  <button onClick={() => advanceStatus(incident)} className="btn-secondary !px-3 !py-1.5 text-xs">
                    Mark {titleCase(NEXT_STATUS[incident.status]!)}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

function ReportIncidentForm({ zones, onCreated }: { zones: CityStatus['zones']; onCreated: (incident: Incident) => void }) {
  const [zoneId, setZoneId] = useState(zones[0]?.id ?? '')
  const [type, setType] = useState(INCIDENT_TYPES[0])
  const [severity, setSeverity] = useState('MODERATE')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function submit() {
    const zone = zones.find((z) => z.id === zoneId)
    if (!zone || !description.trim()) return
    setSubmitting(true)
    try {
      const incident = await api.createIncident({ zoneId, type, severity, description, lat: zone.centerLat, lng: zone.centerLng })
      onCreated(incident)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="panel p-5 space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div>
          <label className="label">Zone</label>
          <select className="input mt-1.5" value={zoneId} onChange={(e) => setZoneId(e.target.value)}>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>{z.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Type</label>
          <select className="input mt-1.5" value={type} onChange={(e) => setType(e.target.value)}>
            {INCIDENT_TYPES.map((t) => (
              <option key={t} value={t}>{titleCase(t)}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Severity</label>
          <select className="input mt-1.5" value={severity} onChange={(e) => setSeverity(e.target.value)}>
            {SEVERITIES.map((s) => (
              <option key={s} value={s}>{titleCase(s)}</option>
            ))}
          </select>
        </div>
      </div>
      <div>
        <label className="label">Description</label>
        <textarea
          className="input mt-1.5 min-h-[72px]"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Describe what's happening…"
        />
      </div>
      <button onClick={submit} disabled={submitting || !description.trim()} className="btn-primary">
        Submit Report
      </button>
    </div>
  )
}
