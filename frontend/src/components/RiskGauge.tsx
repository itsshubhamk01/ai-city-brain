import type { RiskLevel } from '../types'
import { riskColor } from '../lib/utils'
import { SeverityBadge } from './common'

export function RiskGauge({ score, level }: { score: number; level: RiskLevel }) {
  const size = 220
  const strokeWidth = 16
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const pct = Math.min(100, Math.max(0, score)) / 100
  const dashOffset = circumference * (1 - pct)
  const color = riskColor(level)

  return (
    <div className="relative shrink-0" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke="#1C2740" strokeWidth={strokeWidth} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={dashOffset}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 1s ease-out, stroke 0.6s ease' }}
        />
      </svg>
      <div
        className="absolute inset-0 animate-sweep pointer-events-none"
        style={{
          background: `conic-gradient(from 0deg, transparent 0deg, ${color}66 18deg, transparent 36deg)`,
          borderRadius: '9999px',
        }}
      />
      <div className="absolute inset-0 flex flex-col items-center justify-center gap-2">
        <div className="flex flex-col items-center">
          <span className="font-mono text-4xl font-semibold text-ink leading-none">{Math.round(score)}</span>
          <span className="text-[10px] uppercase tracking-widest text-ink-faint mt-1">Risk Score</span>
        </div>
        <SeverityBadge level={level} />
      </div>
    </div>
  )
}
