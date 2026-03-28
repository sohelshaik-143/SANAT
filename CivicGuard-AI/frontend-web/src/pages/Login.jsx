import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, Mail, ShieldAlert, User, Smartphone, AlertCircle, Building2, Key } from 'lucide-react';
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

  // OTP State (Citizen Only)
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState('');

  const [error, setError] = useState('');

  const validateGovEmail = (email) => {
    return email.endsWith('.gov.in') || email.endsWith('.nic.in');
  };

  const handleSendOtp = () => {
    if (!emailOrPhone || emailOrPhone.length < 5) {
      setError('Please enter a valid email or mobile number.');
      return;
    }
    setError('');
    // Simulate API call to send OTP
    setTimeout(() => {
      setOtpSent(true);
      alert("[MOCK OTP ALERT]\n\nYour OTP code is: 1234\n\n(In a real production environment, this would be delivered via SMS to your phone or via Email to your inbox.)");
    }, 500);
  };

  const handleGoogleAuth = (e) => {
    e.preventDefault();
    setUser('Google Citizen', 'citizen');
    navigate('/citizen');
  };

  const handleAuth = (e) => {
    e.preventDefault();
    setError('');

    if (role === 'official') {
      // Common Validation for Official Role
      if (!validateGovEmail(emailOrPhone)) {
        setError("Security Violation: Official portal requires a valid .gov.in or .nic.in email address.");
        return;
      }

      if (!isLogin) {
        if (!fullName || !emailOrPhone || !password || !designation || !department) {
          setError("All fields are required for Official registration.");
          return;
        }
        setUser(fullName, role);
      } else {
        setUser(emailOrPhone.split('@')[0] || 'Official', role);
      }
      navigate('/dashboard');

    } else {
      // Citizen Flow
      const isEmail = emailOrPhone.includes('@');

      if (!isEmail) {
        if (!otpSent) {
          handleSendOtp();
          return;
        }

        if (otp !== '1234') {
          setError("Invalid OTP. For demo purposes, please enter the code '1234'.");
          return;
        }
      }

      setUser(fullName || (isEmail ? emailOrPhone.split('@')[0] : 'Citizen User'), role);
      navigate('/citizen');
    }
  };

  const toggleAuthMode = () => {
    setIsLogin(!isLogin);
    setError('');
    setPassword('');
    setFullName('');
    setDesignation('');
    setDepartment('');
    setOtpSent(false);
    setOtp('');
  };

  const resetCitizenFlow = () => {
    setOtpSent(false);
    setOtp('');
    setError('');
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
            AI-Powered Civic Issue Monitoring & <br />
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
        <div className="login-box glass-panel animate-fade-in" style={{ maxHeight: '90vh', overflowY: 'auto' }}>

          <div className="role-toggle-container">
            <button
              className={`role-toggle-btn ${role === 'citizen' ? 'active' : ''}`}
              onClick={() => { setRole('citizen'); setError(''); resetCitizenFlow(); }}
              type="button"
            >
              <User size={16} /> Citizen
            </button>
            <button
              className={`role-toggle-btn ${role === 'official' ? 'active' : ''}`}
              onClick={() => { setRole('official'); setError(''); resetCitizenFlow(); }}
              type="button"
            >
              <ShieldAlert size={16} /> Official
            </button>
          </div>

          <h2>
            {role === 'official'
              ? (isLogin ? 'Government Portal Access' : 'Officer Registration')
              : 'Citizen Portal Access'
            }
          </h2>
          <p className="login-caption">
            {role === 'official'
              ? (isLogin ? 'Sign in with your authorized government credentials.' : 'Apply for Official Workspace access.')
              : 'Secure password-less login using Mobile OTP.'
            }
          </p>

          <form onSubmit={handleAuth} className="login-form">
            {error && (
              <div className="error-alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {/* Official Registration - Full Name */}
            {(!isLogin && role === 'official') || (role === 'citizen' && !isLogin) ? (
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
                    required={!isLogin}
                    disabled={role === 'citizen' && otpSent}
                  />
                </div>
              </div>
            ) : null}

            {/* Primary ID Field (Email or Mobile) */}
            <div className="input-group">
              <label className="input-label">
                {role === 'citizen' ? 'Email or Mobile Number' : 'Official Government Email'}
              </label>
              <div className="input-with-icon">
                {role === 'citizen' ? <Smartphone size={18} className="input-icon" /> : <Mail size={18} className="input-icon" />}
                <input
                  type="text"
                  className="input-field pl-10"
                  placeholder={role === 'citizen' ? "user@email.com or +91 9876543210" : "officer@gov.in"}
                  value={emailOrPhone}
                  onChange={(e) => setEmailOrPhone(e.target.value)}
                  required
                  disabled={role === 'citizen' && otpSent}
                />
              </div>
              {role === 'official' && (
                <span className="text-xs text-muted mt-1 inline-block">Must be a .gov.in or .nic.in domain</span>
              )}
            </div>

            {/* Official Registration Extra Fields */}
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

            {/* Password Field (Official Only) */}
            {role === 'official' && (
              <div className="input-group mb-6">
                <label className="input-label">
                  {isLogin ? 'Secure Password' : 'Create Password'}
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
                    <a href="#" className="text-xs text-accent">Forgot password?</a>
                  </div>
                )}
              </div>
            )}

            {/* OTP Field (Citizen Only - Mobile Only) */}
            {role === 'citizen' && !emailOrPhone.includes('@') && otpSent && (
              <div className="input-group mb-6 animate-fade-in">
                <label className="input-label flex justify-between">
                  <span>Enter OTP</span>
                  <span className="text-xs text-accent cursor-pointer" onClick={resetCitizenFlow}>Edit Number</span>
                </label>
                <div className="input-with-icon">
                  <Key size={18} className="input-icon" />
                  <input
                    type="text"
                    className="input-field pl-10 tracking-widest font-bold"
                    placeholder="1234"
                    maxLength={4}
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                    required
                  />
                </div>
                <div className="flex justify-between items-center mt-2">
                  <span className="text-xs text-muted">Sent to {emailOrPhone}</span>
                  <a href="#" className="text-xs text-accent" onClick={(e) => { e.preventDefault(); setOtp(''); }}>Resend OTP</a>
                </div>
              </div>
            )}

            {/* Submit Button */}
            <button type="submit" className={`btn btn-primary w-full login-btn ${role === 'citizen' && !emailOrPhone.includes('@') && !otpSent ? 'mt-4' : ''}`}>
              {role === 'official'
                ? (isLogin ? 'Authenticate & Enter' : 'Submit Official Registration')
                : (emailOrPhone.includes('@') ? 'Login via Email' : (otpSent ? 'Verify OTP & Enter' : 'Send Mobile OTP'))
              }
            </button>

            {role === 'citizen' && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', margin: '1.25rem 0' }}>
                  <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.08)' }}></div>
                  <span style={{ padding: '0 12px', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 500 }}>OR</span>
                  <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.08)' }}></div>
                </div>
                <button
                  type="button"
                  onClick={handleGoogleAuth}
                  className="btn w-full hover:scale-[1.02] transition-all"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem', background: '#ffffff', color: '#000000', border: 'none', fontWeight: 600, padding: '0.75rem' }}
                >
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                  </svg>
                  Continue with Google
                </button>
              </>
            )}

            {role === 'official' && (
              <p className="security-notice">
                <Lock size={12} className="inline-icon mr-1" />
                Secured via State Portal SSO. Unauthorized access is prohibited.
              </p>
            )}

            {/* Toggle Login/Register */}
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
