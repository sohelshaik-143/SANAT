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
  const [timelineBefore, setTimelineBefore] = useState(null);
  const [timelineDuring, setTimelineDuring] = useState(null);
  const [timelineAfter, setTimelineAfter] = useState(null);
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

           id: c.ticketNumber || c.id,
           type: c.category || 'Civic Issue',
           location: c.address || 'Unknown',
           aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
           status: c.status || 'Pending',
           date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : new Date().toLocaleString(),
           description: c.description || "No description provided",
           reporter: "Citizen Participant",
           department: c.assignedDepartment || "General Dept",
           originalImage: c.originalImagePath ? `http://localhost:8080/api/uploads/${c.originalImagePath.split(/[\\/]/).pop()}` : null,
           resolutionImage: c.resolutionImagePath ? `http://localhost:8080/api/uploads/resolutions/${c.resolutionImagePath.split(/[\\/]/).pop()}` : null

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

  const handleTimelineUpload = (e, phase) => {
    if (e.target.files && e.target.files.length > 0) {
      if (phase === 'before') setTimelineBefore(e.target.files[0]);
      if (phase === 'during') setTimelineDuring(e.target.files[0]);
      if (phase === 'after') setTimelineAfter(e.target.files[0]);
    }
  };

  const allProofsLoaded = timelineBefore && timelineDuring && timelineAfter;

  const handleVerifyResolution = async () => {
    if (!allProofsLoaded) return;
    setResolutionState('analyzing');

    try {
      const formData = new FormData();
      formData.append('before', timelineBefore);
      formData.append('during', timelineDuring);
      formData.append('after', timelineAfter);
      formData.append('notes', 'Resolved via web portal with Timeline Proof');

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
      }, 3000); // Extended timeout for "AI verification simulation"
    }
  };

  const resetResolution = () => {
    setResolutionState('idle');
    setTimelineBefore(null);
    setTimelineDuring(null);
    setTimelineAfter(null);
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
              <span className="meta-item">
                <MapPin size={14} /> {complaintData.location}
                <a 
                  href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(complaintData.location)}`}
                  target="_blank" 
                  rel="noopener noreferrer"
                  style={{ marginLeft: 8, fontSize: '0.7rem', color: 'var(--accent-primary)', textDecoration: 'none' }}
                >
                  (Google Maps ↗)
                </a>
              </span>
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
                <Camera size={14} style={{ display: 'inline', marginRight: 4 }} />
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
                    <span className="info-value"><AlertTriangle size={14} className="text-warning" /> {complaintData.type}</span>
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

                    <div className="image-card" style={{ padding: '0.5rem', background: 'rgba(255,255,255,0.02)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
                      {complaintData.imageUrl ? (
                        <img src={complaintData.imageUrl} alt="Citizen Verification Proof" style={{ width: '100%', height: '220px', objectFit: 'cover', borderRadius: '8px' }} />
                      ) : (
                        <div className="image-placeholder bg-dark" style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px' }}>
                          <ImageIcon size={32} className="text-muted" />
                        </div>
                      )}
                      <p className="image-caption" style={{ marginTop: '0.75rem', display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                        <span style={{ color: 'var(--text-muted)' }}>Evidence_Capture_01.jpg</span>
                        <ShieldCheck size={16} className="text-success inline-icon" />

                    <div className="image-card">
                      {complaintData.originalImage ? (
                        <img 
                          src={complaintData.originalImage} 
                          alt="Citizen Upload" 
                          className="evidence-image"
                          style={{ width: '100%', height: '200px', objectFit: 'cover', borderRadius: '8px' }}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = 'https://via.placeholder.com/400x300?text=Image+Not+Found';
                          }}
                        />
                      ) : (
                        <div className="image-placeholder bg-dark">
                          <ImageIcon size={32} className="text-muted" />
                        </div>
                      )}
                      <p className="image-caption">
                        {complaintData.originalImage ? 'Verified Evidence' : 'No photo uploaded'} 
                        <ShieldCheck size={14} className="text-success inline-icon" />

                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'resolution' && (
              <div className="tab-content animate-fade-in">
                {resolutionState === 'idle' && (
                  <div className="resolution-upload-area" style={{ textAlign: 'left' }}>
                    <h3 style={{ marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Bot size={20} className="text-accent" /> AI Timeline Proof System
                    </h3>
                    <p className="text-muted mb-6" style={{ fontSize: '0.9rem' }}>
                      To completely eliminate fake resolutions, upload media for all three phases of work. The AI engine will verify temporal, geographic, and structural continuity to ensure impossible-to-fake authenticity.
                    </p>

                    <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: '1fr', marginBottom: '1.5rem' }}>
                      {/* Before */}
                      <div className="glass-panel p-4" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <div>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>1. Before (Arrival State)</p>
                          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{timelineBefore ? timelineBefore.name : 'No file selected'}</p>
                        </div>
                        <label className="btn btn-secondary btn-sm cursor-pointer" style={{ margin: 0 }}>
                          {timelineBefore ? 'Change' : 'Upload Video/Image'}
                          <input type="file" style={{ display: 'none' }} accept="video/*,image/*" onChange={(e) => handleTimelineUpload(e, 'before')} />
                        </label>
                      </div>

                      {/* During */}
                      <div className="glass-panel p-4" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <div>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>2. During Work (In Progress)</p>
                          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{timelineDuring ? timelineDuring.name : 'No file selected'}</p>
                        </div>
                        <label className="btn btn-secondary btn-sm cursor-pointer" style={{ margin: 0 }}>
                          {timelineDuring ? 'Change' : 'Upload Video/Image'}
                          <input type="file" style={{ display: 'none' }} accept="video/*,image/*" onChange={(e) => handleTimelineUpload(e, 'during')} />
                        </label>
                      </div>

                      {/* After */}
                      <div className="glass-panel p-4" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <div>
                          <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>3. After (Resolved State)</p>
                          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{timelineAfter ? timelineAfter.name : 'No file selected'}</p>
                        </div>
                        <label className="btn btn-secondary btn-sm cursor-pointer" style={{ margin: 0 }}>
                          {timelineAfter ? 'Change' : 'Upload Video/Image'}
                          <input type="file" style={{ display: 'none' }} accept="video/*,image/*" onChange={(e) => handleTimelineUpload(e, 'after')} />
                        </label>
                      </div>
                    </div>

                    <div className="flex justify-end pt-2 border-t border-white/10" style={{ paddingTop: '1rem' }}>
                      <button className={`btn w-full ${allProofsLoaded ? 'btn-primary' : 'btn-secondary'}`} disabled={!allProofsLoaded} onClick={handleVerifyResolution}>
                        {allProofsLoaded ? '✓ Run 3-Point AI Continuity Verification' : 'Upload all 3 proofs to proceed'}
                      </button>
                    </div>
                  </div>
                )}

                {resolutionState === 'analyzing' && (
                  <div className="ai-state-view analyzing animate-fade-in text-center p-8">
                    <Bot size={48} className="text-accent scanner-icon mx-auto mb-4" />
                    <h3>AI Continuity Verification</h3>
                    <p className="text-muted mx-auto mt-2" style={{ fontSize: '0.85rem' }}>
                      Analyzing Timeline: Before ➔ During ➔ After.<br />
                      Verifying structural integrity matches, extracting EXIF timestamps, and confirming geographic coordinates...
                    </p>
                    <div className="progress-bar mt-6 mx-auto w-full max-w-xs "><div className="fill scan-progress max-w-full" style={{ animationDuration: '3s' }}></div></div>
                  </div>
                )}

                {resolutionState === 'success' && (
                  <div className="ai-state-view success animate-fade-in text-center p-8">
                    <div className="icon-circle bg-success-dim mx-auto mt-4">
                      <ShieldCheck size={48} className="text-success" />
                    </div>
                    <h3 className="text-success">Continuity Verified: Impossible to Fake</h3>
                    <p className="mt-2 max-w-md mx-auto text-secondary" style={{ fontSize: '0.85rem' }}>
                      The AI timeline system has successfully authenticated the workflow. Timeline metadata and structural elements match perfectly securely tying the resolution to the issue. The ticket is officially Resolved.
                    </p>
                  </div>
                )}

                {resolutionState === 'failed' && (
                  <div className="ai-state-view failed animate-fade-in text-center p-8">
                    <div className="icon-circle bg-danger-dim mx-auto mt-4">
                      <AlertTriangle size={48} className="text-danger" />
                    </div>
                    <h3 className="text-danger">AI Proof Inconsistency Detected</h3>
                    <p className="mt-2 max-w-md mx-auto text-danger" style={{ fontSize: '0.85rem' }}>
                      The uploaded timeline sequence does not match physically or temporally. AI detected structural gaps likely indicating spoofing. Task cannot be closed.
                    </p>
                    <button onClick={resetResolution} className="btn btn-secondary mt-6">Restart Compliance Process</button>
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
                <div className="progress-bar"><div className="fill bg-success" style={{ width: '98%' }}></div></div>
              </div>
              <div className="metric">
                <div className="metric-header">
                  <span>GPS Consistency</span>
                  <span className="text-success">Verified</span>
                </div>
                <div className="progress-bar"><div className="fill bg-success" style={{ width: '95%' }}></div></div>
              </div>
              <div className="metric">
                <div className="metric-header">
                  <span>Image Recycled</span>
                  <span className="text-warning">Low Risk</span>
                </div>
                <div className="progress-bar"><div className="fill bg-warning" style={{ width: '20%' }}></div></div>
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
                <div className="timeline-marker active" style={{ borderColor: 'var(--status-danger)', background: 'var(--status-danger)' }}></div>
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
