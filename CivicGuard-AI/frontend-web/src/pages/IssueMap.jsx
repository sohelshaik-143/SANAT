import React, { useState, useEffect } from 'react';
import { MapPin, AlertTriangle } from 'lucide-react';
import { MapContainer, TileLayer, Marker, Popup, CircleMarker, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { getComplaints } from '../data/mockData';
import apiClient from '../api/apiClient';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const MapUpdater = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    if (center && map) {
      map.flyTo(center, 15, { animate: true, duration: 1.5 });
    }
  }, [center, map]);
  return null;
};

// Fix leaflet default icon paths broken by vite bundling
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const STATUS_COLORS = {
  Resolved: '#22c55e',
  'In Progress': '#3b82f6',
  Escalated: '#ef4444',
  Pending: '#f59e0b',
};

const getStatusColor = (status) => STATUS_COLORS[status] || '#f59e0b';

const CATEGORIES = ['All', 'Road Damage', 'Water Supply', 'Garbage', 'Street Lights', 'Sewage', 'Civic Issue', 'Other'];

const IssueMap = () => {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [selectedIssue, setSelectedIssue] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await apiClient.get('/complaints/assigned?page=0&size=100');
        setComplaints(res.data.content.map(c => ({
          id: c.ticketNumber || c.id,
          type: c.category || 'Other',
          location: c.address || 'Unknown',
          status: c.status || 'Pending',
          description: c.description || '',
          lat: c.latitude && c.latitude !== 0 ? c.latitude : (c.lat || 12.9716),
          lng: c.longitude && c.longitude !== 0 ? c.longitude : (c.lng || 77.5946),
        })));
      } catch {
        const mock = getComplaints().map((c) => ({
          ...c,
          type: c.type || 'Civic Issue',
          lat: c.lat || 12.9716,
          lng: c.lng || 77.5946,
        }));
        setComplaints(mock);
      }
    };
    fetch();
  }, []);

  const filtered = selectedCategory === 'All'
    ? complaints
    : complaints.filter(c => c.type === selectedCategory);

  const pendingIssues = filtered.filter(c => c.status === 'Pending' || c.status === 'In Progress');
  const activeCenterItem = pendingIssues.length > 0 ? pendingIssues[0] : (filtered.length > 0 ? filtered[0] : null);
  const mapCenter = activeCenterItem ? [activeCenterItem.lat, activeCenterItem.lng] : [12.9716, 77.5946];

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Issue Map</h1>
          <p className="page-subtitle">Real-time geographic view of all civic complaints</p>
        </div>
      </div>

      {/* Category Filter Chips */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
        {CATEGORIES.map(cat => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            style={{
              padding: '4px 14px',
              borderRadius: '20px',
              border: selectedCategory === cat ? '1.5px solid var(--accent-primary)' : '1.5px solid rgba(255,255,255,0.1)',
              background: selectedCategory === cat ? 'rgba(139,92,246,0.2)' : 'rgba(255,255,255,0.04)',
              color: selectedCategory === cat ? 'var(--accent-primary)' : 'var(--text-secondary)',
              cursor: 'pointer',
              fontSize: '0.8rem',
              fontWeight: selectedCategory === cat ? 600 : 400,
              transition: 'all 0.2s'
            }}
          >
            {cat}
          </button>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '1.5rem' }}>
        {/* Map */}
        <div style={{ borderRadius: '12px', overflow: 'hidden', height: '540px', position: 'relative', border: '1px solid rgba(255,255,255,0.08)' }}>
          <MapContainer
            center={mapCenter}
            zoom={14}
            style={{ height: '100%', width: '100%' }}
          >
            <MapUpdater center={mapCenter} />
            <TileLayer
              attribution='Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
              url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            />
            {filtered.map(c => (
              <CircleMarker
                key={c.id}
                center={[c.lat, c.lng]}
                radius={10}
                pathOptions={{
                  color: getStatusColor(c.status),
                  fillColor: getStatusColor(c.status),
                  fillOpacity: 0.85,
                  weight: 2,
                }}
                eventHandlers={{ click: () => setSelectedIssue(c) }}
              >
                <Popup>
                  <div style={{ minWidth: 180 }}>
                    <p style={{ fontWeight: 700, marginBottom: 4 }}>{c.id}</p>
                    <p style={{ marginBottom: 2 }}>{c.type}</p>
                    <p style={{ fontSize: '0.8em', color: '#666' }}>{c.location}</p>
                    <span style={{
                      display: 'inline-block', marginTop: 6, padding: '2px 8px',
                      background: getStatusColor(c.status), color: '#fff',
                      borderRadius: '10px', fontSize: '0.75em', fontWeight: 600
                    }}>{c.status}</span>
                  </div>
                </Popup>
              </CircleMarker>
            ))}
          </MapContainer>
        </div>

        {/* Sidebar Panel */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {/* Status Summary */}
          <div className="glass-panel p-4">
            <h3 style={{ fontWeight: 600, marginBottom: '0.75rem', fontSize: '0.9rem' }}>Status Summary</h3>
            {Object.entries(STATUS_COLORS).map(([label, color]) => {
              const count = complaints.filter(c => c.status === label).length;
              const pct = complaints.length > 0 ? Math.round((count / complaints.length) * 100) : 0;
              return (
                <div key={label} style={{ marginBottom: '0.75rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: 4 }}>
                    <span style={{ color }}>{label}</span>
                    <span style={{ fontWeight: 700 }}>{count} ({pct}%)</span>
                  </div>
                  <div style={{ height: 5, borderRadius: 3, background: 'rgba(255,255,255,0.06)' }}>
                    <div style={{ height: '100%', borderRadius: 3, background: color, width: `${pct}%`, transition: 'width 0.4s' }} />
                  </div>
                </div>
              );
            })}
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', paddingTop: 8, borderTop: '1px solid rgba(255,255,255,0.06)', fontWeight: 700 }}>
              <span>Total Markers</span>
              <span>{filtered.length}</span>
            </div>
          </div>

          {/* Legend */}
          <div className="glass-panel p-4">
            <h3 style={{ fontWeight: 600, marginBottom: '0.75rem', fontSize: '0.9rem' }}>Legend</h3>
            {Object.entries(STATUS_COLORS).map(([label, color]) => (
              <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <div style={{ width: 14, height: 14, borderRadius: '50%', background: color, boxShadow: `0 0 6px ${color}` }} />
                <span style={{ fontSize: '0.82rem' }}>{label}</span>
              </div>
            ))}
          </div>

          {/* Selected Issue Detail */}
          {selectedIssue && (
            <div className="glass-panel p-4 animate-fade-in">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                <div>
                  <p style={{ fontWeight: 700, fontSize: '0.95rem' }}>{selectedIssue.id}</p>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{selectedIssue.type}</p>
                </div>
                <button onClick={() => setSelectedIssue(null)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '1rem' }}>✕</button>
              </div>
              <p style={{ fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: 4, marginBottom: 10 }}>
                <MapPin size={12} /> {selectedIssue.location}
              </p>
              <span className={`badge ${{
                Resolved: 'badge-success', Escalated: 'badge-danger', 'In Progress': 'badge-info'
              }[selectedIssue.status] || 'badge-warning'}`}>
                {selectedIssue.status}
              </span>
              <button
                className="btn btn-primary btn-sm"
                style={{ width: '100%', marginTop: '0.75rem' }}
                onClick={() => navigate(`/complaint/${selectedIssue.id}`)}
              >
                View Full Details
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default IssueMap;
