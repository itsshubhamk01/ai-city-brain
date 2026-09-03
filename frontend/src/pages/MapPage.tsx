import { useEffect, useMemo, useState } from 'react'
import { MapContainer, TileLayer, Circle, Polyline, Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { api } from '../lib/api'
import type { MapData } from '../types'
import { riskColor } from '../lib/utils'
import { Spinner } from '../components/common'

function divIcon(color: string, label: string, size = 22) {
  return L.divIcon({
    className: '',
    html: `<div style="
      width:${size}px;height:${size}px;border-radius:9999px;
      background:${color}22;border:2px solid ${color};
      display:flex;align-items:center;justify-content:center;
      font:600 10px 'Inter',sans-serif;color:${color};
      box-shadow:0 0 0 2px rgba(11,17,32,0.6);
    ">${label}</div>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

function incidentIcon(color: string) {
  return L.divIcon({
    className: '',
    html: `<div style="width:16px;height:16px;position:relative;">
      <div style="position:absolute;inset:0;border-radius:9999px;background:${color};opacity:0.35;animation:pulseDot 1.6s ease-in-out infinite;"></div>
      <div style="position:absolute;inset:4px;border-radius:9999px;background:${color};border:1px solid rgba(255,255,255,0.6);"></div>
    </div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
  })
}

const ROAD_COLORS: Record<string, string> = { OPEN: '#25314D', CONGESTED: '#F5A623', CLOSED: '#FF5D5D' }

export default function MapPage() {
  const [data, setData] = useState<MapData | null>(null)

  useEffect(() => {
    let mounted = true
    function load() {
      api.cityMap().then((d) => mounted && setData(d))
    }
    load()
    const id = setInterval(load, 6000)
    return () => {
      mounted = false
      clearInterval(id)
    }
  }, [])

  const center = useMemo<[number, number]>(() => {
    if (!data || data.zones.length === 0) return [39.9, -105.5]
    const lat = data.zones.reduce((s, z) => s + z.centerLat, 0) / data.zones.length
    const lng = data.zones.reduce((s, z) => s + z.centerLng, 0) / data.zones.length
    return [lat, lng]
  }, [data])

  if (!data) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner className="h-6 w-6" />
      </div>
    )
  }

  return (
    <div className="relative h-full">
      <MapContainer center={center} zoom={13} className="h-full w-full" zoomControl={false}>
        <TileLayer
          className="map-tiles-dark"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        {data.zones.map((zone) => {
          const color = riskColor(zone.riskScore >= 80 ? 'CRITICAL' : zone.riskScore >= 60 ? 'HIGH' : zone.riskScore >= 35 ? 'MODERATE' : 'LOW')
          return (
            <Circle
              key={zone.id}
              center={[zone.centerLat, zone.centerLng]}
              radius={900}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.12, weight: 1.5 }}
            >
              <Popup>
                <strong>{zone.name}</strong>
                <br />
                Risk score: {Math.round(zone.riskScore)}/100
                <br />
                Traffic: {Math.round(zone.trafficLevel)}% · Flood risk: {Math.round(zone.floodRiskScore)}%
              </Popup>
            </Circle>
          )
        })}

        {data.roads.map((road) => (
          <Polyline
            key={road.id}
            positions={[
              [road.startLat, road.startLng],
              [road.endLat, road.endLng],
            ]}
            pathOptions={{
              color: ROAD_COLORS[road.status],
              weight: road.status === 'OPEN' ? 3 : 4,
              dashArray: road.status === 'CLOSED' ? '6 6' : undefined,
            }}
          >
            <Popup>
              {road.name} — {road.status} ({Math.round(road.congestionPct)}% congestion)
            </Popup>
          </Polyline>
        ))}

        {data.hospitals.map((h) => (
          <Marker key={h.id} position={[h.lat, h.lng]} icon={divIcon('#FB7185', 'H')}>
            <Popup>
              <strong>{h.name}</strong>
              <br />
              {h.occupiedBeds}/{h.totalBeds} beds occupied ({Math.round(h.occupancyPct)}%)
            </Popup>
          </Marker>
        ))}

        {data.fireStations.map((f) => (
          <Marker key={f.id} position={[f.lat, f.lng]} icon={divIcon('#F5A623', 'F')}>
            <Popup>
              <strong>{f.name}</strong>
              <br />
              {f.availableUnits}/{f.totalUnits} units available
            </Popup>
          </Marker>
        ))}

        {data.powerStations.map((p) => (
          <Marker key={p.id} position={[p.lat, p.lng]} icon={divIcon('#C084FC', 'P')}>
            <Popup>
              <strong>{p.name}</strong>
              <br />
              {Math.round(p.currentLoadMw)} / {Math.round(p.capacityMw)} MW
            </Popup>
          </Marker>
        ))}

        {data.waterStations.map((w) => (
          <Marker key={w.id} position={[w.lat, w.lng]} icon={divIcon('#22D3EE', 'W')}>
            <Popup>
              <strong>{w.name}</strong>
              <br />
              Reservoir level: {Math.round(w.reservoirLevelPct)}%
            </Popup>
          </Marker>
        ))}

        {data.wasteBins.map((b) => (
          <Marker key={b.id} position={[b.lat, b.lng]} icon={divIcon('#A3E635', 'B', 18)}>
            <Popup>
              {b.code} — {Math.round(b.capacityPct)}% full
            </Popup>
          </Marker>
        ))}

        {data.ambulances.map((a) => (
          <Marker key={a.id} position={[a.lat, a.lng]} icon={divIcon('#5B8DEF', 'A', 18)}>
            <Popup>
              {a.code} — {a.status}
            </Popup>
          </Marker>
        ))}

        {data.incidents.map((i) => (
          <Marker key={i.id} position={[i.lat, i.lng]} icon={incidentIcon(riskColor(i.severity))}>
            <Popup>
              <strong>{i.type.replace(/_/g, ' ')}</strong>
              <br />
              {i.description}
              <br />
              Status: {i.status}
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      <div className="panel absolute top-4 right-4 z-[1000] px-4 py-3 text-xs space-y-1.5">
        <p className="label mb-1">Legend</p>
        {[
          ['Hospital', '#FB7185'],
          ['Fire station', '#F5A623'],
          ['Power station', '#C084FC'],
          ['Water station', '#22D3EE'],
          ['Waste bin', '#A3E635'],
          ['Ambulance', '#5B8DEF'],
        ].map(([label, color]) => (
          <div key={label} className="flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color, border: `1px solid ${color}` }} />
            <span className="text-ink-muted">{label}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
