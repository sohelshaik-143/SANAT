import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map as MapIcon, 
  AlertTriangle, 
  CheckCircle,
  FileText,
  Settings,
  Users,
  LogOut
} from 'lucide-react';
import { getUser } from '../data/mockData';
import './Sidebar.css';

const Sidebar = () => {
  const navigate = useNavigate();
  const user = getUser();

  return (
    <aside className="sidebar glass-panel">
      <div className="sidebar-header">
        <div className="logo-container">
          <div className="logo-icon animate-fade-in">🛡️</div>
          <h2 className="logo-text">CivicGuard<span>AI</span></h2>
        </div>
      </div>

      <div className="sidebar-profile">
        <div className="profile-initial">{user.name.charAt(0)}</div>
        <div className="profile-info">
          <p className="profile-name">{user.name}</p>
          <p className="profile-role">Gov. Official</p>
        </div>
      </div>

      <div className="sidebar-content">
        <nav className="nav-menu">
          <p className="nav-label">COMMAND CENTER</p>
          <NavLink to="/dashboard" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/community" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <Users size={20} />
            <span>Community Pulse</span>
          </NavLink>
        </nav>

        <nav className="nav-menu mt-auto">
          <p className="nav-label">PREFERENCES</p>
          <NavLink to="/settings" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <Settings size={20} />
            <span>Settings</span>
          </NavLink>
          <button className="nav-item logout-btn" onClick={() => navigate('/login')}>
            <LogOut size={20} />
            <span>Logout</span>
          </button>
        </nav>
      </div>
    </aside>
  );
};

export default Sidebar;
