import { useCallback, useEffect, useMemo, useState } from 'react'
import { Activity, Zap } from 'lucide-react'
import { api } from '../lib/api'
import type { AgentType, DecisionFeedItem } from '../types'
import { useWebSocketChannel } from '../hooks/useWebSocket'
import { AGENT_COLORS, AGENT_LABELS } from '../lib/constants'
import { AgentBadge, EmptyState, PanelHeader, SeverityBadge, Spinner } from '../components/common'
import { cx, timeAgo } from '../lib/utils'

const AGENT_TYPES = Object.keys(AGENT_LABELS) as AgentType[]

export default function AgentsPage() {
  const [decisions, setDecisions] = useState<DecisionFeedItem[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<AgentType | 'ALL'>('ALL')

  useEffect(() => {
    api.decisions().then(setDecisions).finally(() => setLoading(false))
  }, [])

  useWebSocketChannel<DecisionFeedItem[]>('decisions', useCallback((payload) => setDecisions(payload), []))

  const filtered = useMemo(
    () => (filter === 'ALL' ? decisions : decisions.filter((d) => d.agentType === filter)),
    [decisions, filter]
  )

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-4xl mx-auto space-y-4">
      <div>
        <h1 className="font-display text-lg font-semibold text-ink">AI Decisions</h1>
        <p className="text-sm text-ink-muted">What every specialized agent has observed and done, in real time</p>
      </div>

      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setFilter('ALL')}
          className={cx('px-3 py-1.5 rounded-full text-xs font-medium transition-colors', filter === 'ALL' ? 'bg-accent text-white' : 'bg-raised text-ink-muted hover:text-ink')}
        >
          All agents
        </button>
        {AGENT_TYPES.map((type) => (
          <button
            key={type}
            onClick={() => setFilter(type)}
            className="px-3 py-1.5 rounded-full text-xs font-medium transition-colors"
            style={
              filter === type
                ? { backgroundColor: AGENT_COLORS[type], color: '#0B1120' }
                : { backgroundColor: '#16213A', color: '#8B98B8' }
            }
          >
            {AGENT_LABELS[type]}
          </button>
        ))}
      </div>

      <div className="panel">
        <PanelHeader
          title={`${filtered.length} events`}
          eyebrow="Live"
          action={<Activity size={14} className="text-signal-nominal" />}
        />
        <div className="divide-y divide-hairline max-h-[70vh] overflow-y-auto">
          {filtered.length === 0 && <EmptyState title="No activity from this agent yet" />}
          {filtered.map((d) => (
            <div key={d.id} className="px-5 py-3.5 flex items-start gap-3">
              <div className="h-6 w-6 rounded-md bg-raised flex items-center justify-center shrink-0 mt-0.5">
                <Zap size={12} style={{ color: AGENT_COLORS[d.agentType] }} />
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2 flex-wrap mb-1">
                  <AgentBadge type={d.agentType} />
                  <span className="text-[11px] text-ink-faint uppercase tracking-wide">{d.category}</span>
                  {d.severity && <SeverityBadge level={d.severity} />}
                  <span className="text-[11px] text-ink-faint ml-auto">{timeAgo(d.createdAt)}</span>
                </div>
                <p className="text-sm text-ink">{d.summary}</p>
                <p className="text-[11px] text-ink-faint mt-0.5">{d.zoneName}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
