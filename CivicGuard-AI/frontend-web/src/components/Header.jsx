import React, { useState } from 'react';
import { Bell, Search, User, X } from 'lucide-react';
import { getUser } from '../data/mockData';
import './Header.css';

const Header = () => {
  const user = getUser();
  const [showNotifications, setShowNotifications] = useState(false);
  const [readIds, setReadIds] = useState([]);

  const NOTIFICATIONS = [
    { id: 1, msg: 'New complaint assigned: C-2026-1021', time: '2 min ago' },
    { id: 2, msg: 'SLA breach alert: C-2026-0918 (PWD)', time: '14 min ago' },
    { id: 3, msg: 'Complaint C-2026-0812 resolved by Officer', time: '1 hr ago' },
  ];

  const unreadCount = NOTIFICATIONS.filter(n => !readIds.includes(n.id)).length;

  const markAllRead = () => setReadIds(NOTIFICATIONS.map(n => n.id));
  const markRead = (id) => setReadIds(prev => prev.includes(id) ? prev : [...prev, id]);

  return (
    <header className="header glass-panel" style={{ position: 'relative' }}>
      <div className="header-search">
        <Search size={18} className="search-icon" />
        <input 
          type="text" 
          placeholder="Search complaints, IDs, or areas..." 
          className="search-input"
        />
      </div>

      <div className="header-actions">
        <button className="icon-btn relative" onClick={() => setShowNotifications(!showNotifications)} style={{ position: 'relative' }}>
          <Bell size={20} />
          {unreadCount > 0 && <span className="badge-indicator">{unreadCount}</span>}
        </button>
        
        {showNotifications && (
          <div style={{
            position: 'absolute', top: '64px', right: '80px', width: 320,
            background: '#1a1a2e', border: '1px solid rgba(255,255,255,0.1)',
            borderRadius: '12px', boxShadow: '0 20px 50px rgba(0,0,0,0.5)',
            zIndex: 1000, padding: '0', overflow: 'hidden'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 16px', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
              <h4 style={{ fontWeight: 700, fontSize: '0.9rem' }}>Notifications</h4>
              <button onClick={() => setShowNotifications(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}><X size={16} /></button>
            </div>
            {NOTIFICATIONS.map(n => (
              <div key={n.id} onClick={() => markRead(n.id)} style={{ padding: '12px 16px', borderBottom: '1px solid rgba(255,255,255,0.04)', background: !readIds.includes(n.id) ? 'rgba(139,92,246,0.05)' : 'transparent', cursor: 'pointer', transition: 'background 0.2s' }}>
                <p style={{ fontSize: '0.82rem', marginBottom: 4 }}>{n.msg}</p>
                <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{n.time}</p>
              </div>
            ))}
            <div style={{ padding: '10px 16px', textAlign: 'center' }}>
              <span style={{ fontSize: '0.78rem', color: unreadCount > 0 ? 'var(--accent-primary)' : 'var(--text-muted)', cursor: unreadCount > 0 ? 'pointer' : 'default' }} onClick={markAllRead}>
                {unreadCount > 0 ? 'Mark all as read' : 'All caught up ✓'}
              </span>
            </div>
          </div>
        )}
        
        <div className="user-profile">
          <div className="avatar" style={{ background: 'rgba(139,92,246,0.3)', color: '#8b5cf6', fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {user.name ? user.name.charAt(0).toUpperCase() : <User size={18} />}
          </div>
          <div className="user-info">
            <p className="user-name">{user.name || 'Officer'}</p>
            <p className="user-role">Gov. Official</p>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
