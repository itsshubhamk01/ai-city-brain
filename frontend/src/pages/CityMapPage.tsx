import { Hospital, Shield, Flame } from 'lucide-react'
import { useLocationContext } from '../hooks/useLocationContext'
import { useMumbaiInfrastructure } from '../hooks/useMumbaiInfrastructure'
import { PageHeader } from '../components/PageHeader'
import { MumbaiMap } from '../components/MumbaiMap'
import { Spinner } from '../components/common'

export default function CityMapPage() {
  const { location } = useLocationContext()
  const { data, loading } = useMumbaiInfrastructure()

  return (
    <div className="min-h-screen bg-base text-ink flex flex-col">
      <PageHeader title="Mumbai Map" subtitle="Real hospitals, police & fire stations — OpenStreetMap" />

      <div className="flex-1 max-w-6xl mx-auto w-full px-4 sm:px-6 py-6 space-y-4">
        {loading ? (
          <div className="panel h-[60vh] flex items-center justify-center">
            <Spinner className="h-6 w-6" />
          </div>
        ) : data && !data.available ? (
          <div className="panel h-[60vh] flex flex-col items-center justify-center text-center px-6">
            <p className="text-sm text-ink-muted">Live infrastructure data is currently unavailable.</p>
            {data.unavailableReason && <p className="text-xs text-ink-faint mt-1">{data.unavailableReason}</p>}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-3 gap-3">
              <StatChip icon={Hospital} color="#FB7185" label="Hospitals" value={data?.hospitals.length ?? 0} />
              <StatChip icon={Shield} color="#5B8DEF" label="Police stations" value={data?.policeStations.length ?? 0} />
              <StatChip icon={Flame} color="#F5A623" label="Fire stations" value={data?.fireStations.length ?? 0} />
            </div>
            <MumbaiMap
              selected={location}
              hospitals={data?.hospitals}
              policeStations={data?.policeStations}
              fireStations={data?.fireStations}
              heightClassName="h-[65vh]"
              showLegend
            />
          </>
        )}
      </div>
    </div>
  )
}

function StatChip({ icon: Icon, color, label, value }: { icon: typeof Hospital; color: string; label: string; value: number }) {
  return (
    <div className="panel-3d px-4 py-3 flex items-center gap-3">
      <div className="h-8 w-8 rounded-lg flex items-center justify-center shrink-0" style={{ backgroundColor: `${color}22` }}>
        <Icon size={15} style={{ color }} />
      </div>
      <div>
        <p className="font-mono text-sm font-semibold text-ink leading-none">{value}</p>
        <p className="text-[11px] text-ink-faint mt-1">{label}</p>
      </div>
    </div>
  )
}
