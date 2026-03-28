import React, { useState } from 'react';
import { Settings as SettingsIcon, Bell, Shield, User, Sun, Moon, Check, Eye, EyeOff } from 'lucide-react';
import { getUser, setUser } from '../data/mockData';
import './Dashboard.css';

const Settings = () => {
  const user = getUser();
  const [name, setName] = useState(user.name || '');
  const [email, setEmail] = useState('officer@civicguard.gov.in');
  const [notifications, setNotifications] = useState({ email: true, sms: false, escalation: true });
  const [darkMode, setDarkMode] = useState(true);
  const [saved, setSaved] = useState(false);

  // Password state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [passwordStatus, setPasswordStatus] = useState(null); // null | 'success' | 'error'
  const [passwordMsg, setPasswordMsg] = useState('');

  const handleSave = (e) => {
    e.preventDefault();
    setUser(name, user.role || 'official');
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const handleUpdatePassword = () => {
    if (!currentPassword || !newPassword) {
      setPasswordStatus('error');
      setPasswordMsg('Both fields are required.');
      return;
    }
    if (newPassword.length < 6) {
      setPasswordStatus('error');
      setPasswordMsg('New password must be at least 6 characters.');
      return;
    }
    if (currentPassword === newPassword) {
      setPasswordStatus('error');
      setPasswordMsg('New password must be different from current.');
      return;
    }
    // Simulate update
    setPasswordStatus('success');
    setPasswordMsg('Password updated successfully!');
    setCurrentPassword('');
    setNewPassword('');
    setTimeout(() => { setPasswordStatus(null); setPasswordMsg(''); }, 3000);
  };

  const toggleDarkMode = (isDark) => {
    setDarkMode(isDark);
    // Apply theme to root
    document.documentElement.style.setProperty('--bg-primary', isDark ? '#0f111a' : '#f1f5f9');
    document.documentElement.style.setProperty('--bg-secondary', isDark ? '#1a1d2d' : '#e2e8f0');
    document.documentElement.style.setProperty('--bg-glass', isDark ? 'rgba(26, 29, 45, 0.7)' : 'rgba(255,255,255,0.7)');
    document.documentElement.style.setProperty('--text-primary', isDark ? '#ffffff' : '#0f172a');
    document.documentElement.style.setProperty('--text-secondary', isDark ? '#94a3b8' : '#475569');
    document.documentElement.style.setProperty('--text-muted', isDark ? '#64748b' : '#94a3b8');
  };

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Settings</h1>
          <p className="page-subtitle">Manage account, notifications, and preferences</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        {/* Profile Section */}
        <div className="glass-panel p-6">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <User size={20} style={{ color: 'var(--accent-primary)' }} />
            <h3 style={{ fontWeight: 700 }}>Profile Information</h3>
          </div>
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Full Name</label>
              <input className="input-field" value={name} onChange={e => setName(e.target.value)} placeholder="Your full name" />
            </div>
            <div className="input-group">
              <label className="input-label">Official Email</label>
              <input className="input-field" type="email" value={email} onChange={e => setEmail(e.target.value)} />
            </div>
            <div className="input-group">
              <label className="input-label">Role</label>
              <input className="input-field" value="Government Official" disabled style={{ opacity: 0.5 }} />
            </div>
            <button type="submit" className="btn btn-primary">
              {saved ? <><Check size={16} /> Saved!</> : 'Save Changes'}
            </button>
          </form>
        </div>

        {/* Notifications Section */}
        <div className="glass-panel p-6">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <Bell size={20} style={{ color: 'var(--accent-primary)' }} />
            <h3 style={{ fontWeight: 700 }}>Notification Preferences</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {[
              { label: 'Email Notifications', key: 'email', desc: 'Receive updates on new assignments via email' },
              { label: 'SMS Alerts', key: 'sms', desc: 'Get SMS when a complaint is escalated' },
              { label: 'Escalation Alerts', key: 'escalation', desc: 'Notify when SLA deadline is breached' },
            ].map(item => (
              <div key={item.key} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid rgba(255,255,255,0.06)', cursor: 'pointer' }} onClick={() => setNotifications(prev => ({ ...prev, [item.key]: !prev[item.key] }))}>
                <div>
                  <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>{item.label}</p>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{item.desc}</p>
                </div>
                <label style={{ position: 'relative', display: 'inline-block', width: 44, height: 24, cursor: 'pointer' }} onClick={e => e.stopPropagation()}>
                  <input
                    type="checkbox"
                    checked={notifications[item.key]}
                    onChange={e => setNotifications(prev => ({ ...prev, [item.key]: e.target.checked }))}
                    style={{ opacity: 0, width: 0, height: 0 }}
                  />
                  <span style={{
                    position: 'absolute', inset: 0, borderRadius: 24,
                    background: notifications[item.key] ? 'var(--accent-primary)' : 'rgba(255,255,255,0.1)',
                    transition: '0.3s'
                  }}>
                    <span style={{
                      position: 'absolute',
                      left: notifications[item.key] ? '22px' : '2px',
                      top: '2px',
                      width: 20, height: 20,
                      borderRadius: '50%',
                      background: 'white',
                      transition: '0.3s',
                      boxShadow: '0 1px 4px rgba(0,0,0,0.4)'
                    }} />
                  </span>
                </label>
              </div>
            ))}
          </div>
        </div>

        {/* Appearance */}
        <div className="glass-panel p-6">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            {darkMode ? <Moon size={20} style={{ color: 'var(--accent-primary)' }} /> : <Sun size={20} style={{ color: '#f59e0b' }} />}
            <h3 style={{ fontWeight: 700 }}>Appearance</h3>
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            {[true, false].map(isDark => (
              <button
                key={String(isDark)}
                className={`btn btn-sm ${darkMode === isDark ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => toggleDarkMode(isDark)}
                style={{ flex: 1, padding: '0.75rem', gap: '0.5rem' }}
              >
                {isDark ? <><Moon size={14} /> Dark Mode</> : <><Sun size={14} /> Light Mode</>}
              </button>
            ))}
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.75rem' }}>
            Theme changes apply instantly to the entire portal.
          </p>
        </div>

        {/* Security */}
        <div className="glass-panel p-6">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <Shield size={20} style={{ color: 'var(--accent-primary)' }} />
            <h3 style={{ fontWeight: 700 }}>Security</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <div className="input-group">
              <label className="input-label">Current Password</label>
              <div style={{ position: 'relative' }}>
                <input className="input-field" type={showCurrent ? 'text' : 'password'} placeholder="••••••••" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} style={{ paddingRight: '2.5rem', width: '100%' }} />
                <button type="button" onClick={() => setShowCurrent(!showCurrent)} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
                  {showCurrent ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div className="input-group">
              <label className="input-label">New Password</label>
              <div style={{ position: 'relative' }}>
                <input className="input-field" type={showNew ? 'text' : 'password'} placeholder="••••••••" value={newPassword} onChange={e => setNewPassword(e.target.value)} style={{ paddingRight: '2.5rem', width: '100%' }} />
                <button type="button" onClick={() => setShowNew(!showNew)} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
                  {showNew ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            {passwordStatus && (
              <div style={{ padding: '8px 12px', borderRadius: 6, background: passwordStatus === 'success' ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)', border: `1px solid ${passwordStatus === 'success' ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}`, color: passwordStatus === 'success' ? 'var(--status-success)' : 'var(--status-danger)', fontSize: '0.82rem' }}>
                {passwordMsg}
              </div>
            )}
            <button className="btn btn-secondary" onClick={handleUpdatePassword}>Update Password</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
