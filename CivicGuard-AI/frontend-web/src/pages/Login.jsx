import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, Mail, ShieldAlert, User, Smartphone, AlertCircle, Phone, Building2, MapPin } from 'lucide-react';
import { setUser } from '../data/mockData';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [role, setRole] = useState('citizen'); // 'citizen' or 'official'
  
  // Form State
  const [emailOrPhone, setEmailOrPhone] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [designation, setDesignation] = useState('');
  const [department, setDepartment] = useState('');
  const [error, setError] = useState('');

  const validateGovEmail = (email) => {
    return email.endsWith('.gov.in') || email.endsWith('.nic.in');
  };

  const handleAuth = (e) => {
    e.preventDefault();
    setError('');

    // Common Validation for Official Role
    if (role === 'official' && !validateGovEmail(emailOrPhone)) {
      setError("Security Violation: Official portal requires a valid .gov.in or .nic.in email address.");
      return;
    }

    if (!isLogin) {
      // Registration Logic
      if (!fullName || !emailOrPhone || !password) {
        setError("All fields are required for registration.");
        return;
      }
      if (role === 'official' && (!designation || !department)) {
        setError("Designation and Department are required for Government Officials.");
        return;
      }
      // Registration Success
      setUser(fullName, role);
      if (role === 'official') {
        navigate('/dashboard');
      } else {
        navigate('/citizen');
      }
    } else {
      // Login Logic (Fallback username for demo login)
      setUser(emailOrPhone.split('@')[0] || 'User', role);
      if (role === 'official') {
        navigate('/dashboard');
      } else {
        navigate('/citizen');
      }
    }
  };

  const toggleAuthMode = () => {
    setIsLogin(!isLogin);
    setError('');
    // Reset fields
    setPassword('');
    setFullName('');
    setDesignation('');
    setDepartment('');
  };

  return (
    <div className="login-container">
      <div className="login-split-left">
        <div className="branded-content">
          <div className="logo-mega">
            <ShieldAlert size={64} className="text-accent" />
          </div>
          <h1 className="login-title">CivicGuard<span>AI</span></h1>
          <p className="login-subtitle">
            AI-Powered Civic Issue Monitoring & <br/> 
            Auto-Escalation System for India.
          </p>
          
          <div className="feature-list">
            <div className="feature-item">
              <div className="feature-icon">✅</div>
              <span>Automated Department Routing</span>
            </div>
            <div className="feature-item">
              <div className="feature-icon">🤖</div>
              <span>AI Deepfake & Authenticity Verification</span>
            </div>
            <div className="feature-item">
              <div className="feature-icon">⚡</div>
              <span>Multi-Tier Priority Escalation</span>
            </div>
          </div>
        </div>
        <div className="login-bg-decoration"></div>
      </div>
      
      <div className="login-split-right">
        <div className="login-box glass-panel animate-fade-in" style={{maxHeight: '90vh', overflowY: 'auto'}}>
          
          <div className="role-toggle-container">
            <button 
              className={`role-toggle-btn ${role === 'citizen' ? 'active' : ''}`}
              onClick={() => { setRole('citizen'); setError(''); }}
              type="button"
            >
              <User size={16} /> Citizen
            </button>
            <button 
              className={`role-toggle-btn ${role === 'official' ? 'active' : ''}`}
              onClick={() => { setRole('official'); setError(''); }}
              type="button"
            >
              <ShieldAlert size={16} /> Official
            </button>
          </div>

          <h2>
            {isLogin 
              ? (role === 'citizen' ? 'Citizen Portal Access' : 'Government Portal Access') 
              : (role === 'citizen' ? 'Register as Citizen' : 'Officer Registration')}
          </h2>
          <p className="login-caption">
            {isLogin 
              ? (role === 'citizen' ? 'Sign in to report and track civic issues.' : 'Sign in with your authorized government credentials.')
              : (role === 'citizen' ? 'Create an account to submit AI-verified reports.' : 'Apply for Official Workspace access.')}
          </p>

          <form onSubmit={handleAuth} className="login-form">
            {error && (
              <div className="error-alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {!isLogin && (
              <div className="input-group">
                <label className="input-label">Full Name</label>
                <div className="input-with-icon">
                  <User size={18} className="input-icon" />
                  <input 
                    type="text" 
                    className="input-field pl-10" 
                    placeholder="Enter your full name" 
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                  />
                </div>
              </div>
            )}

            <div className="input-group">
              <label className="input-label">
                {role === 'citizen' ? 'Email Address or Phone Number' : 'Official Government Email'}
              </label>
              <div className="input-with-icon">
                {role === 'citizen' ? <Smartphone size={18} className="input-icon" /> : <Mail size={18} className="input-icon" />}
                <input 
                  type="text" 
                  className="input-field pl-10" 
                  placeholder={role === 'citizen' ? "e.g. +91 9876543210" : "e.g. officer@gov.in"} 
                  value={emailOrPhone}
                  onChange={(e) => setEmailOrPhone(e.target.value)}
                  required
                />
              </div>
              {role === 'official' && (
                <span className="text-xs text-muted mt-1 inline-block">Must be a .gov.in or .nic.in domain</span>
              )}
            </div>

            {!isLogin && role === 'official' && (
              <>
                <div className="input-group">
                  <label className="input-label">Department</label>
                  <div className="input-with-icon">
                    <Building2 size={18} className="input-icon" />
                    <input 
                      type="text" 
                      className="input-field pl-10" 
                      placeholder="e.g. Public Works Department" 
                      value={department}
                      onChange={(e) => setDepartment(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="input-group">
                  <label className="input-label">Designation</label>
                  <div className="input-with-icon">
                    <ShieldAlert size={18} className="input-icon" />
                    <input 
                      type="text" 
                      className="input-field pl-10" 
                      placeholder="e.g. Chief Engineer (Level 2)" 
                      value={designation}
                      onChange={(e) => setDesignation(e.target.value)}
                      required
                    />
                  </div>
                </div>
              </>
            )}

            <div className="input-group mb-6">
              <label className="input-label">
                {isLogin 
                  ? (role === 'citizen' ? 'Password / OTP' : 'Secure Password')
                  : 'Create Password'
                }
              </label>
              <div className="input-with-icon">
                <Lock size={18} className="input-icon" />
                <input 
                  type="password" 
                  className="input-field pl-10" 
                  placeholder="••••••••" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              {isLogin && (
                <div className="flex justify-between items-center mt-2">
                  <label className="flex items-center gap-2 text-xs text-muted cursor-pointer">
                    <input type="checkbox" /> Remember me
                  </label>
                  <a href="#" className="text-xs" onClick={(e) => { e.preventDefault(); setError("Demo Mode: Multi-factor authentication is currently managed via simulated State SSO. Contact administrator for password resets."); }}>
                    {role === 'citizen' ? 'Login via OTP instead?' : 'Forgot password?'}
                  </a>
                </div>
              )}
            </div>

            <button type="submit" className="btn btn-primary w-full login-btn">
              {isLogin 
                ? (role === 'citizen' ? 'Sign In & Report Issue' : 'Authenticate & Enter')
                : (role === 'citizen' ? 'Create Citizen Account' : 'Submit Official Registration')
              }
            </button>
            
            {role === 'official' && (
              <p className="security-notice">
                <Lock size={12} className="inline-icon mr-1" />
                Secured via State Portal SSO. Unauthorized access is prohibited.
              </p>
            )}
            
            <p className="security-notice text-center mt-4">
              {isLogin ? "Don't have an account? " : "Already have an account? "}
              <span className="text-accent cursor-pointer underline" onClick={toggleAuthMode}>
                {isLogin ? "Register here" : "Sign In"}
              </span>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
