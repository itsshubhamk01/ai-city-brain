import type { ZoneStatus } from '../types'
import { riskColor, titleCase } from '../lib/utils'
import { MetricBar } from './common'

export function ZoneCard({ zone, onSelect }: { zone: ZoneStatus; onSelect?: (zone: ZoneStatus) => void }) {
  const color = riskColor(zone.riskScore >= 80 ? 'CRITICAL' : zone.riskScore >= 60 ? 'HIGH' : zone.riskScore >= 35 ? 'MODERATE' : 'LOW')
  const powerStrainPct = zone.powerSupplyMw > 0 ? (zone.powerDemandMw / zone.powerSupplyMw) * 100 : 100

  return (
    <button
      onClick={() => onSelect?.(zone)}
      className="panel text-left p-4 flex flex-col gap-3 hover:border-accent/50 transition-colors w-full"
      style={{ borderLeftColor: color, borderLeftWidth: 3 }}
    >
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-display text-sm font-semibold text-ink">{zone.name}</h3>
          <p className="text-[11px] text-ink-faint mt-0.5">{titleCase(zone.kind)} · {zone.population.toLocaleString()} residents</p>
        </div>
        <div className="text-right">
          <span className="font-mono text-lg font-semibold" style={{ color }}>{Math.round(zone.riskScore)}</span>
          <p className="text-[10px] text-ink-faint uppercase tracking-wide">risk</p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-x-4 gap-y-2">
        <MetricBar label="Traffic" value={zone.trafficLevel} color="#F5A623" />
        <MetricBar label="Flood risk" value={zone.floodRiskScore} color="#22D3EE" />
        <MetricBar label="Power strain" value={powerStrainPct} color="#C084FC" />
        <MetricBar label="Hospitals" value={zone.hospitalOccupancyPct} color="#FB7185" />
        <MetricBar label="Waste" value={zone.wasteLevelPct} color="#A3E635" />
        <MetricBar label="AQI" value={zone.aqi} max={150} unit="" color="#8FB0F5" />
      </div>
    </button>
  )
}
