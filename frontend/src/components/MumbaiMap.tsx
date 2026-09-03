import { useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const MUMBAI_CENTER: [number, number] = [19.076, 72.8777]

function markerIcon(color: string) {
  return L.divIcon({
    className: '',
    html: `<div style="
      width:20px;height:20px;border-radius:9999px;
      background:${color};border:3px solid rgba(255,255,255,0.85);
      box-shadow:0 0 0 3px ${color}33;
    "></div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  })
}

function Recenter({ lat, lng }: { lat: number; lng: number }) {
  const map = useMap()
  useEffect(() => {
    map.flyTo([lat, lng], 13, { duration: 1.2 })
  }, [lat, lng, map])
  return null
}

export function MumbaiMap({ selected }: { selected?: { lat: number; lng: number; label: string } | null }) {
  return (
    <div className="rounded-xl overflow-hidden border border-hairline h-72 sm:h-96">
      <MapContainer center={selected ?? MUMBAI_CENTER} zoom={selected ? 13 : 11} className="h-full w-full" zoomControl={true}>
        <TileLayer
          className="map-tiles-dark"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {selected && (
          <>
            <Marker position={[selected.lat, selected.lng]} icon={markerIcon('#5B8DEF')}>
              <Popup>{selected.label}</Popup>
            </Marker>
            <Recenter lat={selected.lat} lng={selected.lng} />
          </>
        )}
      </MapContainer>
    </div>
  )
}
