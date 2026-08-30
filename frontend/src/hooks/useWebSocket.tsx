import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { wsUrl } from '../lib/api'

type Listener = (payload: any) => void
type Status = 'connecting' | 'open' | 'closed'

interface WsContextValue {
  status: Status
  subscribe: (channel: string, listener: Listener) => () => void
}

const WsContext = createContext<WsContextValue | undefined>(undefined)

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>('connecting')
  const listenersRef = useRef<Map<string, Set<Listener>>>(new Map())
  const socketRef = useRef<WebSocket | null>(null)
  const retryRef = useRef(0)

  useEffect(() => {
    let cancelled = false
    let retryTimeout: ReturnType<typeof setTimeout>

    function connect() {
      if (cancelled) return
      setStatus('connecting')
      let socket: WebSocket
      try {
        socket = new WebSocket(wsUrl())
      } catch {
        setStatus('closed')
        return
      }
      socketRef.current = socket

      socket.onopen = () => {
        retryRef.current = 0
        setStatus('open')
      }
      socket.onclose = () => {
        setStatus('closed')
        if (!cancelled) {
          const delay = Math.min(1000 * 2 ** retryRef.current, 15000)
          retryRef.current += 1
          retryTimeout = setTimeout(connect, delay)
        }
      }
      socket.onerror = () => {
        socket.close()
      }
      socket.onmessage = (event) => {
        try {
          const { channel, payload } = JSON.parse(event.data)
          listenersRef.current.get(channel)?.forEach((fn) => fn(payload))
        } catch {
          // malformed frame — ignore
        }
      }
    }

    connect()
    return () => {
      cancelled = true
      clearTimeout(retryTimeout)
      socketRef.current?.close()
    }
  }, [])

  function subscribe(channel: string, listener: Listener) {
    if (!listenersRef.current.has(channel)) listenersRef.current.set(channel, new Set())
    listenersRef.current.get(channel)!.add(listener)
    return () => {
      listenersRef.current.get(channel)?.delete(listener)
    }
  }

  return <WsContext.Provider value={{ status, subscribe }}>{children}</WsContext.Provider>
}

export function useWebSocketChannel<T>(channel: string, onMessage: (payload: T) => void): void {
  const ctx = useContext(WsContext)
  if (!ctx) throw new Error('useWebSocketChannel must be used within a WebSocketProvider')
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => ctx.subscribe(channel, onMessage as Listener), [channel])
}

export function useWebSocketStatus(): Status {
  const ctx = useContext(WsContext)
  if (!ctx) throw new Error('useWebSocketStatus must be used within a WebSocketProvider')
  return ctx.status
}
