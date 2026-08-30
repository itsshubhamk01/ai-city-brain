import type { RiskLevel, Severity } from '../types'
import { RISK_COLORS, SEVERITY_COLORS } from './constants'

export function cx(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ')
}

export function riskColor(level: RiskLevel | Severity): string {
  return RISK_COLORS[level as RiskLevel] ?? SEVERITY_COLORS[level as Severity] ?? '#8B98B8'
}

export function round(value: number, decimals = 0): number {
  const factor = 10 ** decimals
  return Math.round(value * factor) / factor
}

export function formatMetric(value: number, unit = ''): string {
  return `${round(value)}${unit}`
}

export function timeAgo(iso: string | null | undefined): string {
  if (!iso) return '—'
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (seconds < 5) return 'just now'
  if (seconds < 60) return `${seconds}s ago`
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return new Date(iso).toLocaleDateString()
}

export function formatClock(date: Date): string {
  return date.toLocaleTimeString('en-US', { hour12: false })
}

export function titleCase(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}
