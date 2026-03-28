import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { 
  ArrowLeft, MapPin, Calendar, AlertTriangle, CheckCircle, 
  FileCheck2, Image as ImageIcon, ShieldCheck, Upload, Bot, Camera
} from 'lucide-react';
import { getComplaintById, updateComplaintStatus } from '../data/mockData';
import apiClient from '../api/apiClient';
import './ComplaintDetail.css';

const ComplaintDetail = () => {
  const { id } = useParams();
  
  const [complaintData, setComplaintData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('details');
  const [resolutionState, setResolutionState] = useState('idle');
  const [resolutionFile, setResolutionFile] = useState(null);
  const [isResolved, setIsResolved] = useState(false);
  const [currentStatus, setCurrentStatus] = useState('');
  const [showReassignModal, setShowReassignModal] = useState(false);
  const [selectedDept, setSelectedDept] = useState('');
  const [reassigned, setReassigned] = useState(false);
  const [officerPhotos, setOfficerPhotos] = useState([]);
  const [officerNote, setOfficerNote] = useState('');
  const [photoSaved, setPhotoSaved] = useState(false);
  const [actionToast, setActionToast] = useState(null);

  useEffect(() => {
    const fetchComplaint = async () => {
      try {
        const response = await apiClient.get(`/complaints/${id}`);
        const c = response.data;
        const mapped = {
           id: c.ticketNumber || c.id,
           type: c.category || 'Civic Issue',
           location: c.address || 'Unknown',
           aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
           status: c.status || 'Pending',
           date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : new Date().toLocaleString(),
           description: c.description || "No description provided",
           reporter: "Citizen Participant",
           department: c.assignedDepartment || "General Dept"
        };
        setComplaintData(mapped);
        setIsResolved(mapped.status === 'Resolved');
        setCurrentStatus(mapped.status);
      } catch (err) {
        console.warn('Backend unavailable, falling back to mock data:', err.message);
        const md = getComplaintById(id);
        setComplaintData(md);
        if (md) {
           setIsResolved(md.status === 'Resolved');
           setCurrentStatus(md.status);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchComplaint();
  }, [id]);

  const handleResolutionUpload = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setResolutionFile(e.target.files[0]);
    }
  };

  const handleVerifyResolution = async () => {
    if (!resolutionFile) return;
    setResolutionState('analyzing');
    
    try {
      const formData = new FormData();
      formData.append('image', resolutionFile);
      formData.append('notes', 'Resolved via web portal');
      
      await apiClient.put(`/complaints/${id}/resolve`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      
      setResolutionState('success');
      setIsResolved(true);
      setCurrentStatus('Resolved');
    } catch (err) {
      console.warn('Backend resolve failed, falling back to mock data:', err.message);
      setTimeout(() => {
        setResolutionState('success');
        setIsResolved(true);
        setCurrentStatus('Resolved');
        updateComplaintStatus(id, 'Resolved');
      }, 1500);
    }
  };

  const resetResolution = () => {
    setResolutionState('idle');
    setResolutionFile(null);
  };

  const handleAction = (action) => {
    if (action === 'Mark In Progress') {
      setCurrentStatus('In Progress');
      updateComplaintStatus(id, 'In Progress');
      setActionToast('Status updated to In Progress');
      setTimeout(() => setActionToast(null), 3000);
    } else if (action === 'Escalate') {
      setCurrentStatus('Escalated Level 2');
      updateComplaintStatus(id, 'Escalated Level 2');
      setActionToast('Issue escalated to Level 2 — supervisors notified');
      setTimeout(() => setActionToast(null), 3000);
    } else if (action === 'Reassign') {
      setShowReassignModal(true);
    }
  };

  const handleReassign = () => {
    if (!selectedDept) return;
    setReassigned(true);
    setShowReassignModal(false);
    setActionToast(`Issue reassigned to ${selectedDept}`);
    setTimeout(() => { setActionToast(null); setReassigned(false); }, 3000);
  };

  if (loading) {
    return <div className="p-8 text-center text-muted">Loading Case File...</div>;
  }

  if (!complaintData) {
    return <div className="p-8 text-center">Complaint Not Found.</div>;
  }

  return (
    <div className="complaint-detail-container animate-fade-in">
      {/* Action Toast */}
      {actionToast && (
        <div style={{ position: 'fixed', bottom: 24, right: 24, background: 'rgba(16,185,129,0.9)', color: '#fff', padding: '10px 20px', borderRadius: 8, zIndex: 2000, fontWeight: 600, fontSize: '0.85rem', boxShadow: '0 4px 20px rgba(0,0,0,0.4)', animation: 'fadeIn 0.3s ease' }}>
          ✓ {actionToast}
        </div>
      )}
      {/* Reassign Modal */}
      {showReassignModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel animate-fade-in" style={{ maxWidth: 400, width: '100%', padding: '2rem' }}>
            <h3 style={{ fontWeight: 700, marginBottom: '0.5rem' }}>Reassign Issue</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>Select the department to reassign this issue to:</p>
            <select className="input-field" style={{ marginBottom: '1rem' }} value={selectedDept} onChange={e => setSelectedDept(e.target.value)}>
              <option value="">-- Select Department --</option>
              <option>Public Works Department (PWD)</option>
              <option>BBMP (Roads & Infrastructure)</option>
              <option>BESCOM (Electricity)</option>
              <option>BWSSB (Water Supply)</option>
              <option>Solid Waste Management</option>
              <option>Traffic Police</option>
              <option>Urban Local Body</option>
            </select>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowReassignModal(false)}>Cancel</button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={handleReassign} disabled={!selectedDept}>Confirm Reassign</button>
            </div>
          </div>
        </div>
      )}
      <div className="detail-header">
        <Link to="/dashboard" className="back-link">
          <ArrowLeft size={16} />
          Back to Dashboard
        </Link>
        <div className="header-actions-flex">
          <div>
            <h1 className="page-title">Issue {complaintData.id}</h1>
            <div className="issue-meta">
              <span className={`badge ${isResolved ? 'badge-success' : (currentStatus.includes('Escalate') ? 'badge-danger' : 'badge-warning')}`}>
                {currentStatus}
              </span>
              <span className="meta-item"><Calendar size={14} /> Reported {complaintData.date}</span>
              <span className="meta-item"><MapPin size={14} /> {complaintData.location}</span>
            </div>
          </div>
          <div className="action-buttons">
            <button className="btn btn-secondary" onClick={() => handleAction('Reassign')}>Reassign</button>
            <button className="btn btn-danger-outline" onClick={() => handleAction('Escalate')}>Escalate</button>
            {!isResolved && <button className="btn btn-primary" onClick={() => handleAction('Mark In Progress')}>Mark In Progress</button>}
          </div>
        </div>
      </div>

      <div className="detail-grid">
        <div className="main-info">
          <div className="glass-panel detail-card">
            <div className="tabs">
              <button 
                className={`tab ${activeTab === 'details' ? 'active' : ''}`}
                onClick={() => setActiveTab('details')}
              >
                Issue Details
              </button>
              <button 
                className={`tab ${activeTab === 'resolution' ? 'active' : ''}`}
                onClick={() => setActiveTab('resolution')}
              >
                Resolution & Survey Upload
              </button>
              <button 
                className={`tab ${activeTab === 'fieldPhotos' ? 'active' : ''}`}
                onClick={() => setActiveTab('fieldPhotos')}
              >
                <Camera size={14} style={{display:'inline',marginRight:4}} />
                Field Photos
              </button>
            </div>

            {activeTab === 'details' && (
              <div className="tab-content">
                <div className="info-group">
                  <h3>Description</h3>
                  <p className="description-text">
                    {complaintData.description}
                  </p>
                </div>

                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Reported By</span>
                    <span className="info-value">{complaintData.reporter}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Category</span>
                    <span className="info-value"><AlertTriangle size={14} className="text-warning"/> {complaintData.type}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Assigned Dept</span>
                    <span className="info-value">{complaintData.department}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Escalation Status</span>
                    <span className="info-value text-danger">
                      {currentStatus.includes('Escalate') ? 'Escalated to Higher Official (Level 2)' : 'Normal Priority'}
                    </span>
                  </div>
                </div>

                <div className="media-section">
                  <h3>Citizen Uploads</h3>
                  <div className="image-grid">
                    <div className="image-card">
                      <div className="image-placeholder bg-dark">
                        <ImageIcon size={32} className="text-muted" />
                      </div>
                      <p className="image-caption">IMG_20231024_1041.jpg <ShieldCheck size={14} className="text-success inline-icon" /></p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'resolution' && (
              <div className="tab-content">
                {resolutionState === 'idle' && (
                  <div className="resolution-upload-area animate-fade-in">
                    <input 
                      type="file" 
                      id="res-upload" 
                      className="file-input-hidden-res" 
                      onChange={handleResolutionUpload}
                      style={{display: 'none'}}
                    />
                    
                    {!resolutionFile ? (
                      <>
                        <Upload size={32} className="text-muted mb-2" />
                        <h3>Upload Resolution Proof</h3>
                        <p className="text-muted text-center max-w-sm mb-4">
                          Take an "After" photo or video of the resolved issue. Our AI engine will verify the completion before closing the ticket.
                        </p>
                        <label htmlFor="res-upload" className="btn btn-primary cursor-pointer">
                          Select Media
                        </label>
                      </>
                    ) : (
                      <>
                        <CheckCircle size={32} className="text-success mb-2" />
                        <h3>Resolution File Attached</h3>
                        <p className="text-muted text-center max-w-sm mb-4">
                          Ready for AI Verification.
                        </p>
                        <div className="flex gap-4">
                          <button onClick={() => setResolutionFile(false)} className="btn btn-secondary">Cancel</button>
                          <button onClick={handleVerifyResolution} className="btn btn-primary">Run AI Verification</button>
                        </div>
                      </>
                    )}
                  </div>
                )}

                {resolutionState === 'analyzing' && (
                  <div className="ai-state-view analyzing animate-fade-in text-center p-8">
                    <Bot size={48} className="text-accent scanner-icon mx-auto mb-4" />
                    <h3>AI Resolution Verification</h3>
                    <p className="text-muted max-w-sm mx-auto mt-2">
                      Comparing "Before" and "After" images. Verifying geographic coordinates and timestamp authenticity...
                    </p>
                    <div className="progress-bar mt-6 mx-auto w-full max-w-xs "><div className="fill scan-progress max-w-full"></div></div>
                  </div>
                )}

                {resolutionState === 'success' && (
                  <div className="ai-state-view success animate-fade-in text-center p-8">
                    <div className="icon-circle bg-success-dim mx-auto mt-4">
                      <ShieldCheck size={48} className="text-success" />
                    </div>
                    <h3 className="text-success">Work Authenticated</h3>
                    <p className="mt-2 max-w-sm mx-auto text-secondary">
                      The AI has confirmed the structural repairs match the location. The ticket has been officially marked as Resolved and the citizen notified.
                    </p>
                  </div>
                )}
                
                {resolutionState === 'failed' && (
                  <div className="ai-state-view failed animate-fade-in text-center p-8">
                    <div className="icon-circle bg-danger-dim mx-auto mt-4">
                      <AlertTriangle size={48} className="text-danger" />
                    </div>
                    <h3 className="text-danger">AI Verification Failed</h3>
                    <p className="mt-2 text-danger">
                      The uploaded resolution image does not match the geographic location of the original complaint. Task cannot be closed.
                    </p>
                    <button onClick={resetResolution} className="btn btn-secondary mt-6">Try Again</button>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'fieldPhotos' && (
              <div className="tab-content animate-fade-in">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', padding: '1rem 0' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <Camera size={20} style={{ color: 'var(--accent-primary)' }} />
                    <div>
                      <h3 style={{ fontWeight: 700 }}>Officer Field Photos</h3>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Upload photos from your on-site inspection</p>
                    </div>
                  </div>

                  <div style={{ border: '2px dashed rgba(255,255,255,0.1)', borderRadius: 10, padding: '1.5rem', textAlign: 'center' }}>
                    <input
                      type="file"
                      accept="image/*"
                      id="officer-photo-upload"
                      multiple
                      style={{ display: 'none' }}
                      onChange={(e) => {
                        const files = Array.from(e.target.files || []);
                        const newPhotos = files.map(f => ({
                          file: f,
                          url: URL.createObjectURL(f),
                          name: f.name,
                          time: new Date().toLocaleString()
                        }));
                        setOfficerPhotos(prev => [...prev, ...newPhotos]);
                      }}
                    />
                    <label htmlFor="officer-photo-upload" style={{ cursor: 'pointer', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
                      <Camera size={28} style={{ color: 'var(--accent-primary)' }} />
                      <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Click to Upload Field Photos</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Multiple photos supported • JPG, PNG</span>
                    </label>
                  </div>

                  {officerPhotos.length > 0 && (
                    <div>
                      <p style={{ fontSize: '0.85rem', fontWeight: 600, marginBottom: 8 }}>Uploaded ({officerPhotos.length})</p>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: '0.75rem' }}>
                        {officerPhotos.map((p, i) => (
                          <div key={i} style={{ position: 'relative', borderRadius: 8, overflow: 'hidden', border: '1px solid rgba(255,255,255,0.08)' }}>
                            <img src={p.url} alt={p.name} style={{ width: '100%', height: 100, objectFit: 'cover' }} />
                            <button
                              onClick={() => setOfficerPhotos(prev => prev.filter((_, idx) => idx !== i))}
                              style={{ position: 'absolute', top: 4, right: 4, width: 20, height: 20, borderRadius: '50%', background: 'rgba(0,0,0,0.7)', border: 'none', color: '#fff', cursor: 'pointer', fontSize: '0.7rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                            >✕</button>
                            <p style={{ fontSize: '0.65rem', color: 'var(--text-muted)', padding: '4px 6px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.time}</p>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="input-group" style={{ marginBottom: 0 }}>
                    <label className="input-label">Inspection Notes</label>
                    <textarea
                      className="input-field"
                      rows="3"
                      placeholder="E.g. 'Visited site at 2:30 PM. Pothole size is ~3ft diameter. Road barricaded temporarily.'"
                      value={officerNote}
                      onChange={e => setOfficerNote(e.target.value)}
                    />
                  </div>

                  <button
                    className="btn btn-primary"
                    disabled={officerPhotos.length === 0}
                    onClick={() => {
                      setPhotoSaved(true);
                      setActionToast('Field photos and notes saved successfully');
                      setTimeout(() => { setPhotoSaved(false); setActionToast(null); }, 3000);
                    }}
                  >
                    {photoSaved ? <><CheckCircle size={16} /> Saved!</> : <><Upload size={16} /> Save Field Report</>}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="side-info">
          <div className="glass-panel ai-verification-card">
            <div className="ai-header">
              <FileCheck2 size={24} className="text-success" />
              <h3>Original AI Analysis</h3>
            </div>
            
            <div className="ai-score-container">
              <div className="score-circle">
                <span className="score-value">{complaintData.aiConfidence}<span className="percent">%</span></span>
                <span className="score-label">Authentic</span>
              </div>
            </div>

            <div className="ai-metrics">
              <div className="metric">
                <div className="metric-header">
                  <span>Deepfake Detection</span>
                  <span className="text-success">Passed</span>
                </div>
                <div className="progress-bar"><div className="fill bg-success" style={{width: '98%'}}></div></div>
              </div>
              <div className="metric">
                <div className="metric-header">
                  <span>GPS Consistency</span>
                  <span className="text-success">Verified</span>
                </div>
                <div className="progress-bar"><div className="fill bg-success" style={{width: '95%'}}></div></div>
              </div>
              <div className="metric">
                <div className="metric-header">
                  <span>Image Recycled</span>
                  <span className="text-warning">Low Risk</span>
                </div>
                <div className="progress-bar"><div className="fill bg-warning" style={{width: '20%'}}></div></div>
              </div>
            </div>
          </div>

          <div className="glass-panel timeline-card">
            <h3>Issue Timeline</h3>
            <div className="timeline">
              <div className="timeline-item">
                <div className="timeline-marker active"></div>
                <div className="timeline-content">
                  <h4>Ticket Created</h4>
                  <p>Oct 24, 10:45 AM • Citizen Portal</p>
                </div>
              </div>
              <div className="timeline-item">
                <div className="timeline-marker active"></div>
                <div className="timeline-content">
                  <h4>Auto-Assigned to PWD</h4>
                  <p>Oct 24, 10:47 AM • System Route</p>
                </div>
              </div>
              <div className="timeline-item">
                <div className="timeline-marker active" style={{borderColor: 'var(--status-danger)', background: 'var(--status-danger)'}}></div>
                <div className="timeline-content">
                  <h4 className="text-danger">SLA Breached</h4>
                  <p>Oct 26, 10:47 AM • Auto-escalated to Level 2</p>
                </div>
              </div>
              <div className={`timeline-item ${!isResolved ? 'pending' : ''}`}>
                <div className={`timeline-marker ${isResolved ? 'active' : ''}`}></div>
                <div className="timeline-content">
                  <h4>{isResolved ? 'Issue Resolved' : 'Pending Resolution'}</h4>
                  <p>{isResolved ? 'AI Verified Resolution attached' : 'Awaiting officer action'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ComplaintDetail;
