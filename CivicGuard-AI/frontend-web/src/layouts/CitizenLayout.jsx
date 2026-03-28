import React from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { ShieldAlert, User, LogOut, Home, FileText, Users } from 'lucide-react';
import './CitizenLayout.css';

const CitizenLayout = () => {
  const navigate = useNavigate();

  return (
    <div className="citizen-layout-wrapper">
      <header className="citizen-header glass-panel">
        <div className="logo-container" onClick={() => navigate('/citizen')} style={{cursor: 'pointer'}}>
          <div className="logo-icon">🛡️</div>
          <h2 className="logo-text">CivicGuard<span>Citizen</span></h2>
        </div>

        <nav className="citizen-nav">
          <Link to="/citizen" className="nav-link"><Home size={18} /> Dashboard</Link>
          <Link to="/citizen/community" className="nav-link"><Users size={18} /> Community</Link>
          <a href="#reports" className="nav-link" onClick={(e) => {
            e.preventDefault();
            if (window.location.pathname !== '/citizen') {
              navigate('/citizen');
              setTimeout(() => document.querySelector('.history-section')?.scrollIntoView({behavior: 'smooth'}), 100);
            } else {
              document.querySelector('.history-section')?.scrollIntoView({behavior: 'smooth'});
            }
          }}><FileText size={18} /> My Reports</a>
        </nav>

        <div className="citizen-actions">
          <div className="user-profile">
            <div className="avatar citizen-avatar">
              <User size={18} />
            </div>
            <span className="user-name">Rajesh K.</span>
          </div>
          <button className="icon-btn" onClick={() => navigate('/login')}>
            <LogOut size={18} />
          </button>
        </div>
      </header>

      <main className="citizen-content-area">
        <Outlet />
      </main>

      {/* Decorative gradients */}
      <div className="citizen-bg-gradient top"></div>
      <div className="citizen-bg-gradient bottom"></div>
    </div>
  );
};

export default CitizenLayout;
