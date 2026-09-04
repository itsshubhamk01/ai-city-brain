import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './hooks/useAuth'
import { WebSocketProvider } from './hooks/useWebSocket'
import { LocationProvider } from './hooks/useLocationContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AppShell } from './components/AppShell'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CityMapPage from './pages/CityMapPage'
import WeatherDetailPage from './pages/WeatherDetailPage'
import ProfilePage from './pages/ProfilePage'
import CommandCenterPage from './pages/CommandCenterPage'
import MapPage from './pages/MapPage'
import IncidentsPage from './pages/IncidentsPage'
import AgentsPage from './pages/AgentsPage'
import SimulationPage from './pages/SimulationPage'

function LoginRoute() {
  const { user, loading } = useAuth()
  if (loading) return null
  if (user) return <Navigate to="/dashboard" replace />
  return <LoginPage />
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <LocationProvider>
          <WebSocketProvider>
            <Routes>
              {/* Public — no login required */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/explore" element={<Navigate to="/" replace />} />
              <Route path="/login" element={<LoginRoute />} />

              {/* Protected — the real Mumbai experience, reached after signing in */}
              <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
              <Route path="/citymap" element={<ProtectedRoute><CityMapPage /></ProtectedRoute>} />
              <Route path="/weather" element={<ProtectedRoute><WeatherDetailPage /></ProtectedRoute>} />
              <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />

              {/* Protected — the original NovaCity multi-agent architecture demo (fictional data) */}
              <Route
                path="/app"
                element={
                  <ProtectedRoute>
                    <AppShell />
                  </ProtectedRoute>
                }
              >
                <Route index element={<CommandCenterPage />} />
                <Route path="map" element={<MapPage />} />
                <Route path="incidents" element={<IncidentsPage />} />
                <Route path="agents" element={<AgentsPage />} />
                <Route path="simulation" element={<SimulationPage />} />
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </WebSocketProvider>
        </LocationProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
