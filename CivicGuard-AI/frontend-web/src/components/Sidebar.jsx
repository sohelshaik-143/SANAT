import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map as MapIcon, 
  AlertTriangle, 
  CheckCircle,
  FileText,
  Settings,
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
          <p className="nav-label">MAIN MENU</p>
          <NavLink to="/dashboard" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/issues" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <AlertTriangle size={20} />
            <span>Active Issues</span>
          </NavLink>
          <NavLink to="/map" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <MapIcon size={20} />
            <span>Issue Map</span>
          </NavLink>
          <NavLink to="/resolved" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <CheckCircle size={20} />
            <span>Resolved</span>
          </NavLink>
          <NavLink to="/reports" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <FileText size={20} />
            <span>Reports (RTI)</span>
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
