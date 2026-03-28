import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Login from './pages/Login'
import ComplaintDetail from './pages/ComplaintDetail'
import MainLayout from './layouts/MainLayout'
import CitizenDashboard from './pages/CitizenDashboard'
import CitizenLayout from './layouts/CitizenLayout'
import ActiveIssues from './pages/ActiveIssues'
import IssueMap from './pages/IssueMap'
import Resolved from './pages/Resolved'
import Reports from './pages/Reports'
import Settings from './pages/Settings'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        {/* Official Routes inside MainLayout */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="complaint/:id" element={<ComplaintDetail />} />
          <Route path="issues" element={<ActiveIssues />} />
          <Route path="map" element={<IssueMap />} />
          <Route path="resolved" element={<Resolved />} />
          <Route path="reports" element={<Reports />} />
          <Route path="settings" element={<Settings />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>

        {/* Citizen Routes inside CitizenLayout */}
        <Route path="/citizen" element={<CitizenLayout />}>
          <Route index element={<CitizenDashboard />} />
          <Route path="*" element={<Navigate to="/citizen" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
