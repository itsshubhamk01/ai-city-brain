import type { ReactNode } from 'react'
import type { AgentType, RiskLevel, Severity } from '../types'
import { AGENT_COLORS, AGENT_LABELS } from '../lib/constants'
import { cx, riskColor, titleCase } from '../lib/utils'
import { useWebSocketStatus } from '../hooks/useWebSocket'

export function SeverityBadge({ level }: { level: RiskLevel | Severity }) {
  const color = riskColor(level)
  return (
    <span
      className="inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide"
      style={{ color, backgroundColor: `${color}1A`, border: `1px solid ${color}40` }}
    >
      <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: color }} />
      {titleCase(level)}
    </span>
  )
}

export function AgentBadge({ type }: { type: AgentType }) {
  const color = AGENT_COLORS[type]
  return (
    <span
      className="inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-medium"
      style={{ color, backgroundColor: `${color}1A` }}
    >
      <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: color }} />
      {AGENT_LABELS[type]}
    </span>
  )
}

export function ConnectionBadge() {
  const status = useWebSocketStatus()
  const label = status === 'open' ? 'Live' : status === 'connecting' ? 'Connecting…' : 'Reconnecting…'
  const color = status === 'open' ? '#3DDC97' : status === 'connecting' ? '#F5A623' : '#FF5D5D'
  return (
    <span className="inline-flex items-center gap-2 text-xs font-medium text-ink-muted">
      <span
        className={cx('h-2 w-2 rounded-full', status === 'open' && 'animate-pulse-dot')}
        style={{ backgroundColor: color }}
      />
      {label}
    </span>
  )
}

export function EmptyState({ title, description }: { title: string; description?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-1 py-12 text-center">
      <p className="text-sm font-medium text-ink-muted">{title}</p>
      {description && <p className="text-xs text-ink-faint max-w-xs">{description}</p>}
    </div>
  )
}

export function Spinner({ className }: { className?: string }) {
  return (
    <svg className={cx('animate-spin h-4 w-4 text-accent', className)} viewBox="0 0 24 24" fill="none">
      <circle className="opacity-20" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
    </svg>
  )
}

export function PanelHeader({ title, action, eyebrow }: { title: string; action?: ReactNode; eyebrow?: string }) {
  return (
    <div className="flex items-center justify-between px-5 py-4 border-b border-hairline">
      <div>
        {eyebrow && <p className="label mb-0.5">{eyebrow}</p>}
        <h2 className="font-display text-sm font-semibold tracking-wide text-ink">{title}</h2>
      </div>
      {action}
    </div>
  )
}

export function MetricBar({ label, value, max = 100, unit = '%', color }: { label: string; value: number; max?: number; unit?: string; color?: string }) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100))
  return (
    <div>
      <div className="flex items-center justify-between mb-1">
        <span className="text-[11px] text-ink-muted">{label}</span>
        <span className="text-[11px] font-mono text-ink">{Math.round(value)}{unit}</span>
      </div>
      <div className="h-1.5 rounded-full bg-hairline overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-700 ease-out"
          style={{ width: `${pct}%`, backgroundColor: color || '#5B8DEF' }}
        />
      </div>
    </div>
  )
}
