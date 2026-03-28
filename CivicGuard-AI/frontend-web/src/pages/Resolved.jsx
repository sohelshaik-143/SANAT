import React, { useState, useEffect } from 'react';
import { CheckCircle, MapPin, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { getComplaints } from '../data/mockData';
import apiClient from '../api/apiClient';
import './Dashboard.css';

const Resolved = () => {
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await apiClient.get('/complaints/assigned?status=RESOLVED&page=0&size=100');
        const data = res.data.content
          .filter(c => c.status === 'RESOLVED' || c.status === 'Resolved')
          .map(c => ({
            id: c.ticketNumber || c.id,
            type: c.category || 'Civic Issue',
            location: c.address || 'Unknown',
            status: 'Resolved',
            date: c.resolvedAt ? new Date(c.resolvedAt).toLocaleString() : (c.submittedAt ? new Date(c.submittedAt).toLocaleString() : ''),
            aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
          }));
        setComplaints(data.length > 0 ? data : getComplaints().filter(c => c.status === 'Resolved'));
      } catch {
        setComplaints(getComplaints().filter(c => c.status === 'Resolved'));
      }
    };
    fetch();
  }, []);

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Resolved Issues</h1>
          <p className="page-subtitle">AI-verified closed tickets with proof of resolution</p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <div className="glass-panel" style={{ padding: '0.75rem 1.5rem', textAlign: 'center' }}>
            <p style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--status-success)' }}>{complaints.length}</p>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Total Resolved</p>
          </div>
        </div>
      </div>

      {complaints.length === 0 ? (
        <div className="glass-panel text-center p-8 text-muted">
          <CheckCircle size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
          <p>No resolved issues yet. Issues will appear here once they are closed.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {complaints.map(c => (
            <div key={c.id} className="glass-panel" style={{ padding: '1rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{ width: 42, height: 42, borderRadius: '50%', background: 'rgba(34,197,94,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <ShieldCheck size={20} style={{ color: 'var(--status-success)' }} />
                </div>
                <div>
                  <p style={{ fontWeight: 700, marginBottom: '2px' }}>{c.id}</p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}><MapPin size={10} style={{ display: 'inline', marginRight: 4 }} />{c.location}</p>
                </div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Type</p>
                <p style={{ fontSize: '0.9rem', fontWeight: 600 }}>{c.type}</p>
              </div>
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>AI Score</p>
                <p style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--status-success)' }}>{c.aiConfidence}%</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className="badge badge-success">Resolved</span>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 4 }}>{c.date}</p>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/complaint/${c.id}`)}>View Details</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Resolved;
