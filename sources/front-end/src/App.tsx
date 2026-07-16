import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '@/components/ProtectedRoute'
import Landing from '@/pages/Landing'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import SteamCallback from '@/pages/SteamCallback'
import Dashboard from '@/pages/Dashboard'
import GameDetail from '@/pages/GameDetail'
import Profile from '@/pages/Profile'
import Guides from '@/pages/Guides'
import GuideDetail from '@/pages/GuideDetail'
import GuideEditor from '@/pages/GuideEditor'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/auth/steam/callback" element={<SteamCallback />} />
      <Route path="/guides" element={<Guides />} />
      <Route path="/guides/:id" element={<GuideDetail />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/dashboard/:appId" element={<GameDetail />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/guides/new" element={<GuideEditor />} />
        <Route path="/guides/:id/edit" element={<GuideEditor />} />
      </Route>
    </Routes>
  )
}

export default App
