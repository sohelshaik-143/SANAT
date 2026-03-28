import React, { useState, useEffect } from 'react';
import { AlertTriangle, Search, Filter, MapPin } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { getComplaints } from '../data/mockData';
import apiClient from '../api/apiClient';
import './Dashboard.css';

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'Resolved': return 'badge-success';
    case 'Escalated': return 'badge-danger';
    case 'In Progress': return 'badge-info';
    default: return 'badge-warning';
  }
};

const ActiveIssues = () => {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await apiClient.get('/complaints/assigned?page=0&size=100');
        const data = res.data.content.map(c => ({
          id: c.ticketNumber || c.id,
          type: c.category || 'Civic Issue',
          location: c.address || 'Unknown',
          status: c.status || 'Pending',
          date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : '',
          aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
        }));
        setComplaints(data);
      } catch {
        setComplaints(getComplaints());
      }
    };
    fetch();
  }, []);

  const filtered = complaints.filter(c => {
    const matchSearch = c.id.toLowerCase().includes(search.toLowerCase()) ||
      c.location.toLowerCase().includes(search.toLowerCase());
    const matchStatus = filterStatus === 'All' || c.status === filterStatus;
    return matchSearch && matchStatus;
  });

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Active Issues</h1>
          <p className="page-subtitle">All assigned and in-progress civic reports</p>
        </div>
      </div>

      <div className="glass-panel p-4 mb-6" style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', opacity: 0.5 }} />
          <input
            className="input-field"
            style={{ paddingLeft: '36px' }}
            placeholder="Search by ID or location..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <select className="input-field" style={{ width: 'auto' }} value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
          <option value="All">All Statuses</option>
          <option value="Pending">Pending</option>
          <option value="In Progress">In Progress</option>
          <option value="Escalated">Escalated</option>
          <option value="Resolved">Resolved</option>
        </select>
      </div>

      <div className="recent-complaints glass-panel">
        <div className="panel-header">
          <h2 className="panel-title"><AlertTriangle size={18} style={{display:'inline', marginRight: 8}} />All Reports ({filtered.length})</h2>
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
              {filtered.length === 0 ? (
                <tr><td colSpan="7" className="text-center p-8 text-muted">No issues found.</td></tr>
              ) : filtered.map(c => (
                <tr key={c.id}>
                  <td className="font-medium">{c.id}</td>
                  <td>{c.type}</td>
                  <td><span className="text-truncate"><MapPin size={12} style={{display:'inline',marginRight:4}} />{c.location}</span></td>
                  <td>
                    <div className="flex items-center gap-2">
                      <div className="progress-bar-bg">
                        <div className="progress-bar-fill" style={{ width: `${c.aiConfidence}%` }}></div>
                      </div>
                      <span className="text-xs">{c.aiConfidence}%</span>
                    </div>
                  </td>
                  <td><span className={`badge ${getStatusBadgeClass(c.status)}`}>{c.status}</span></td>
                  <td className="text-muted">{c.date}</td>
                  <td>
                    <button className="action-link" onClick={() => navigate(`/complaint/${c.id}`)}>Review</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default ActiveIssues;
