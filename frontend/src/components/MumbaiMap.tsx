import { useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { InfrastructurePoi } from '../types'

const MUMBAI_CENTER: [number, number] = [19.076, 72.8777]

const TYPE_COLORS: Record<string, string> = {
  HOSPITAL: '#FB7185',
  POLICE: '#5B8DEF',
  FIRE_STATION: '#F5A623',
}
const TYPE_LABELS: Record<string, string> = {
  HOSPITAL: 'H',
  POLICE: 'P',
  FIRE_STATION: 'F',
}

function markerIcon(color: string, label?: string, size = 20) {
  return L.divIcon({
    className: '',
    html: `<div style="
      width:${size}px;height:${size}px;border-radius:9999px;
      background:${color}${label ? '' : ''};border:3px solid rgba(255,255,255,0.85);
      box-shadow:0 0 0 3px ${color}33;
      display:flex;align-items:center;justify-content:center;
      font:700 ${Math.round(size * 0.5)}px 'Inter',sans-serif;color:#0B1120;
    ">${label ?? ''}</div>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

function Recenter({ lat, lng }: { lat: number; lng: number }) {
  const map = useMap()
  useEffect(() => {
    map.flyTo([lat, lng], 13, { duration: 1.2 })
  }, [lat, lng, map])
  return null
}

interface MumbaiMapProps {
  selected?: { lat: number; lng: number; label: string } | null
  hospitals?: InfrastructurePoi[]
  policeStations?: InfrastructurePoi[]
  fireStations?: InfrastructurePoi[]
  heightClassName?: string
  showLegend?: boolean
}

export function MumbaiMap({
  selected,
  hospitals = [],
  policeStations = [],
  fireStations = [],
  heightClassName = 'h-72 sm:h-96',
  showLegend = false,
}: MumbaiMapProps) {
  return (
    <div className={`relative rounded-xl overflow-hidden border border-hairline ${heightClassName}`}>
      <MapContainer center={selected ?? MUMBAI_CENTER} zoom={selected ? 13 : 11} className="h-full w-full" zoomControl={true}>
        <TileLayer
          className="map-tiles-dark"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        {selected && (
          <>
            <Marker position={[selected.lat, selected.lng]} icon={markerIcon('#3DDC97', undefined, 22)}>
              <Popup>{selected.label}</Popup>
            </Marker>
            <Recenter lat={selected.lat} lng={selected.lng} />
          </>
        )}

        {hospitals.map((h) => (
          <Marker key={h.id} position={[h.lat, h.lng]} icon={markerIcon(TYPE_COLORS.HOSPITAL, TYPE_LABELS.HOSPITAL)}>
            <Popup>{h.name}</Popup>
          </Marker>
        ))}
        {policeStations.map((p) => (
          <Marker key={p.id} position={[p.lat, p.lng]} icon={markerIcon(TYPE_COLORS.POLICE, TYPE_LABELS.POLICE)}>
            <Popup>{p.name}</Popup>
          </Marker>
        ))}
        {fireStations.map((f) => (
          <Marker key={f.id} position={[f.lat, f.lng]} icon={markerIcon(TYPE_COLORS.FIRE_STATION, TYPE_LABELS.FIRE_STATION)}>
            <Popup>{f.name}</Popup>
          </Marker>
        ))}
      </MapContainer>

      {showLegend && (
        <div className="panel absolute top-3 right-3 z-[1000] px-3 py-2.5 text-xs space-y-1.5">
          <p className="label mb-1">Legend</p>
          {[
            ['Your location', '#3DDC97'],
            ['Hospital', TYPE_COLORS.HOSPITAL],
            ['Police station', TYPE_COLORS.POLICE],
            ['Fire station', TYPE_COLORS.FIRE_STATION],
          ].map(([label, color]) => (
            <div key={label} className="flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} />
              <span className="text-ink-muted">{label}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
