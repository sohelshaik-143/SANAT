import {
  AlertTriangle,
  CheckCircle,
  Clock,
  ShieldAlert,
  ArrowUpRight,
  ArrowDownRight,
  Map as MapIcon
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import React, { useState, useEffect } from 'react';
import { getComplaints, clearAllComplaints } from '../data/mockData';
import apiClient from '../api/apiClient';
import './Dashboard.css';

const StatCard = ({ title, value, icon, trend, trendValue, type }) => (
  <div className="stat-card glass-panel">
    <div className="stat-header">
      <div>
        <h3 className="stat-title">{title}</h3>
        <p className="stat-value">{value}</p>
      </div>
      <div className={`stat-icon-wrapper ${type}`}>
        {icon}
      </div>
    </div>
    <div className={`stat-trend ${trend === 'up' ? 'positive' : 'negative'}`}>
      {trend === 'up' ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
      <span>{trendValue} from last week</span>
    </div>
  </div>
);

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'Resolved': return 'badge-success';
    case 'Escalated': return 'badge-danger';
    case 'In Progress': return 'badge-info';
    default: return 'badge-warning';
  }
};

const Dashboard = () => {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dateFilter, setDateFilter] = useState('Today');

  useEffect(() => {
    const fetchComplaints = async () => {
      try {
        const response = await apiClient.get('/complaints/assigned?status=ASSIGNED&page=0&size=50');
        // Map backend DTO to frontend format
        const apiData = response.data.content.map(c => ({
          id: c.ticketNumber || c.id,
          type: c.category,
          location: c.address,
          aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
          status: c.status,
          date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : new Date().toLocaleString()
        }));
        setComplaints(apiData);
      } catch (err) {
        console.warn('Backend unavailable, falling back to mock data:', err.message);
        setComplaints(getComplaints());
      } finally {
        setLoading(false);
      }
    };

    fetchComplaints();
  }, []);

  const activeCount = complaints.filter(c => c.status && c.status !== 'Resolved').length;
  const verifiedCount = complaints.length > 0 ? (complaints.reduce((acc, curr) => acc + (curr.aiConfidence || 95), 0) / complaints.length).toFixed(1) + '%' : '0.0%';
  const escalatedCount = complaints.filter(c => c.status && c.status.includes('Escalated')).length;
  const resolvedCount = complaints.filter(c => c.status === 'Resolved').length;

  const handleResetData = () => {
    if (window.confirm('⚠️ This will permanently delete all reported issues. Are you sure?')) {
      clearAllComplaints();
      setComplaints([]);
    }
  };

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Command Center</h1>
          <p className="page-subtitle">Real-time civic issue monitoring & AI verification</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select className="input-field" value={dateFilter} onChange={e => setDateFilter(e.target.value)}>
            <option>All Time</option>
            <option>Today</option>
            <option>This Month</option>
          </select>
          <button
            onClick={handleResetData}
            className="btn btn-secondary btn-sm"
            style={{ background: 'rgba(239,68,68,0.12)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.3)', whiteSpace: 'nowrap' }}
          >
            🗑 Reset Data
          </button>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard
          title="Active Issues"
          value={activeCount.toString()}
          icon={<AlertTriangle size={24} />}
          trend="up"
          trendValue="Live Sync"
          type="warning"
        />
        <StatCard
          title="Avg AI Verified"
          value={verifiedCount}
          icon={<ShieldAlert size={24} />}
          trend="up"
          trendValue="System Avg."
          type="info"
        />
        <StatCard
          title="Escalated"
          value={escalatedCount.toString()}
          icon={<Clock size={24} />}
          trend="down"
          trendValue="Live Sync"
          type="danger"
        />
        <StatCard
          title="Resolved"
          value={resolvedCount.toString()}
          icon={<CheckCircle size={24} />}
          trend="up"
          trendValue="Live Sync"
          type="success"
        />
      </div>

      <div className="dashboard-main-content">
        <div className="recent-complaints glass-panel">
          <div className="panel-header">
            <h2 className="panel-title">Live Reports <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 400 }}>({dateFilter})</span></h2>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/issues')}>View All</button>
          </div>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Issue Type</th>
                  <th>Location</th>
                  <th>AI Score</th>
                  <th>Status</th>
                  <th>Time</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {complaints.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center p-8 text-muted">No active cases reported yet.</td>
                  </tr>
                ) : complaints.map(complaint => (
                  <tr key={complaint.id}>
                    <td className="font-medium">{complaint.id}</td>
                    <td>{complaint.type}</td>
                    <td><span className="text-truncate">{complaint.location}</span></td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="progress-bar-bg">
                          <div className="progress-bar-fill" style={{ width: `${complaint.aiConfidence}%` }}></div>
                        </div>
                        <span className="text-xs">{complaint.aiConfidence}%</span>
                      </div>
                    </td>
                    <td><span className={`badge ${getStatusBadgeClass(complaint.status)}`}>{complaint.status}</span></td>
                    <td className="text-muted">{complaint.date}</td>
                    <td>
                      <button className="action-link" onClick={() => navigate(`/complaint/${complaint.id}`)}>Review</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="heatmap-widget glass-panel" style={{ cursor: 'pointer' }} onClick={() => navigate('/map')}>
          <div className="panel-header">
            <h2 className="panel-title">Severity Heatmap</h2>
            <span style={{ fontSize: '0.75rem', color: 'var(--accent-primary)' }}>Click to open map →</span>
          </div>
          <div className="heatmap-placeholder">
            <div className="map-overlay">
              <MapIcon size={48} className="pulse-icon" />
              <p>Live Map Integration Enabled</p>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 4 }}>Tap to view all markers</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
