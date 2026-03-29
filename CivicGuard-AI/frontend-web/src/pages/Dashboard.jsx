import {
  AlertTriangle,
  CheckCircle,
  Clock,
  ShieldAlert,
  ArrowUpRight,
  ArrowDownRight,
  Map as MapIcon,
  Users,
  LayoutGrid,
  History,
  TrendingUp,
  Search,
  Filter
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import React, { useState, useEffect } from 'react';

import { getComplaints, clearAllComplaints, getOfficers } from '../data/mockData';
import apiClient from '../api/apiClient';
import IssueMap from './IssueMap';
import Reports from './Reports';
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
  switch(status) {

    case 'Resolved': return 'badge-success';
    case 'ESCALATED':
    case 'Escalated': return 'badge-danger';
    case 'IN_PROGRESS':
    case 'In Progress': return 'badge-info';
    default: return 'badge-warning';
  }
};

const Dashboard = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('feed');
  const [complaints, setComplaints] = useState([]);
  const [officers, setOfficers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [complaintsRes, officersRes] = await Promise.all([
          apiClient.get('/complaints/assigned?status=ASSIGNED&page=0&size=50'),
          apiClient.get('/dashboard/officers')
        ]);
        
        const mappedComplaints = complaintsRes.data.content.map(c => ({
          id: c.ticketNumber || c.id,
          type: c.category,
          location: c.address,
          aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
          status: c.status,
          date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : new Date().toLocaleString()
        }));
        setComplaints(mappedComplaints);
        setOfficers(officersRes.data);
      } catch (err) {
        console.warn('Backend unavailable, falling back to mock data');
        setComplaints(getComplaints());
        setOfficers(getOfficers());
      } finally {
        setLoading(false);
      }
    };


    fetchData();
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

  const filteredComplaints = complaints.filter(c => 
    c.id.toLowerCase().includes(searchTerm.toLowerCase()) || 
    c.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.location.toLowerCase().includes(searchTerm.toLowerCase())
  );


  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Command Center</h1>
          <p className="page-subtitle">Unified official oversight and AI verification console</p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select className="input-field">
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
            <h2 className="panel-title">Live Reports</h2>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/issues')}>View All</button>
          </div>

        <div className="tab-navigation glass-panel">
          <button className={`nav-tab ${activeTab === 'feed' ? 'active' : ''}`} onClick={() => setActiveTab('feed')}><LayoutGrid size={16}/> Live Feed</button>
          <button className={`nav-tab ${activeTab === 'map' ? 'active' : ''}`} onClick={() => setActiveTab('map')}><MapIcon size={16}/> Heatmap</button>
          <button className={`nav-tab ${activeTab === 'team' ? 'active' : ''}`} onClick={() => setActiveTab('team')}><Users size={16}/> Official Team</button>
          <button className={`nav-tab ${activeTab === 'analytics' ? 'active' : ''}`} onClick={() => setActiveTab('analytics')}><TrendingUp size={16}/> Insights</button>
        </div>
      </div>

      {activeTab === 'feed' && (
        <>
          <div className="stat-grid mb-8">
            <StatCard title="Total Issues" value={complaints.length} icon={<AlertTriangle size={24} />} trend="down" trendValue="5%" type="warning" />
            <StatCard title="AI Confirmed" value="98.2%" icon={<ShieldAlert size={24} />} trend="up" trendValue="1.5%" type="info" />
            <StatCard title="Escalations" value="12" icon={<Clock size={24} />} trend="up" trendValue="2%" type="danger" />
            <StatCard title="Goal Status" value="On Track" icon={<CheckCircle size={24} />} trend="up" trendValue="10%" type="success" />

          </div>

          <div className="recent-complaints glass-panel p-6">
            <div className="panel-header mb-6">
              <h2 className="panel-title">Active Complaint Queue</h2>
              <div className="flex gap-4">
                <div className="search-bar relative">
                  <Search size={14} className="absolute left-3 top-3 text-muted" />
                  <input type="text" placeholder="Search by ID, type or area..." className="input-field pl-10 text-xs w-64" value={searchTerm} onChange={e => setSearchTerm(e.target.value)} />
                </div>
                <button className="btn btn-secondary btn-sm"><Filter size={14} /> Filter</button>
              </div>
            </div>
            
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Ticket ID</th>
                    <th>Issue Category</th>
                    <th>Geographic Area</th>
                    <th>AI Confidence</th>
                    <th>Status</th>
                    <th>Submission</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComplaints.length === 0 ? (
                    <tr><td colSpan="7" className="text-center p-12 text-muted">No cases matching your criteria.</td></tr>

                  ) : filteredComplaints.map(c => (
                    <tr key={c.id}>
                      <td className="font-bold text-accent">{c.id}</td>
                      <td>{c.type}</td>
                      <td className="max-w-[200px] truncate">{c.location}</td>
                      <td>
                        <div className="flex items-center gap-2">
                          <div className="w-16 h-1.5 bg-white/5 rounded-full overflow-hidden">
                            <div className="h-full bg-accent" style={{width: `${c.aiConfidence}%`}}></div>
                          </div>
                          <span className="text-[10px] font-bold">{c.aiConfidence}%</span>

                        </div>
                      </td>
                      <td><span className={`badge ${getStatusBadgeClass(c.status)}`}>{c.status}</span></td>
                      <td className="text-[10px] text-muted">{c.date}</td>
                      <td><button className="btn btn-primary btn-sm px-4" onClick={() => navigate(`/complaint/${c.id}`)}>Audit</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}


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

      {activeTab === 'map' && <div className="h-[600px] rounded-2xl overflow-hidden shadow-2xl"><IssueMap /></div>}

      {activeTab === 'team' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {officers.map(officer => (
            <div key={officer.officerId} className="glass-panel p-6 hover-glow transition-all">
              <div className="flex items-start gap-4 mb-4">
                <div className="w-12 h-12 rounded-full bg-accent/20 flex items-center justify-center font-bold text-accent">{officer.name[0]}</div>
                <div>
                  <h3 className="font-bold">{officer.name}</h3>
                  <p className="text-xs text-muted">{officer.designation} • {officer.department}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4 pt-4 border-t border-white/5">
                <div>
                  <p className="text-[10px] text-muted uppercase font-bold">Resolved</p>
                  <p className="text-lg font-bold text-success">{officer.totalResolved}</p>
                </div>
                <div>
                  <p className="text-[10px] text-muted uppercase font-bold">Performance</p>
                  <p className="text-lg font-bold">{officer.performanceScore}%</p>
                </div>
              </div>
              <button className="btn btn-secondary w-full mt-6 text-xs py-2">View Performance Details</button>

            </div>
          ))}
        </div>
      )}

      {activeTab === 'analytics' && <Reports />}
    </div>
  </div>
  );
};

export default Dashboard;
