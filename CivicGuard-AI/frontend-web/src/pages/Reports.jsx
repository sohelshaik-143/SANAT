import React, { useState } from 'react';
import { FileText, Download, AlertCircle, CheckCircle, X } from 'lucide-react';
import './Dashboard.css';

const REPORTS = [
  {
    id: 'RTI-2026-001',
    title: 'Civic Road Repair Completion Report',
    period: 'Jan – Mar 2026',
    type: 'RTI Compliance',
    issues: 184,
    resolved: 162,
    pending: 22,
    summary: 'This report covers all road repair complaints submitted from Jan to Mar 2026. 162 out of 184 complaints were resolved within the SLA period. 22 cases remain pending due to contractor delays.',
  },
  {
    id: 'RTI-2026-002',
    title: 'Water Supply Disruption Audit',
    period: 'Feb 2026',
    type: 'Department Report',
    issues: 56,
    resolved: 51,
    pending: 5,
    summary: 'Covers water supply disruption complaints for February 2026. 51 out of 56 were resolved. 5 ongoing cases are in pipeline replacement zones and require infrastructure upgrade before closure.',
  },
  {
    id: 'RTI-2026-003',
    title: 'Garbage Collection – Q1 2026',
    period: 'Q1 2026',
    type: 'Quarterly Summary',
    issues: 312,
    resolved: 298,
    pending: 14,
    summary: 'Quarterly garbage collection compliance report for Q1 2026. The ward-wise analysis shows 98.5% SLA compliance. 14 unresolved cases are in areas under BBMP zone restructuring.',
  },
  {
    id: 'RTI-2026-004',
    title: 'Street Lights & Electrical Hazards',
    period: 'March 2026',
    type: 'Safety Audit',
    issues: 73,
    resolved: 65,
    pending: 8,
    summary: 'Safety audit for non-functional street lights and electrical hazards in March 2026. 65 resolved. 8 cases are in areas awaiting BESCOM meter upgrades.',
  },
];

const generateCSV = (report) => {
  const header = 'Report ID,Title,Period,Type,Total Issues,Resolved,Pending\n';
  const row = `${report.id},"${report.title}","${report.period}","${report.type}",${report.issues},${report.resolved},${report.pending}\n`;
  const summaryRow = `\nSummary:\n"${report.summary}"\n`;
  const blob = new Blob([header + row + summaryRow], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${report.id}_${report.period.replace(/[^a-zA-Z0-9]/g, '_')}.csv`;
  a.click();
  URL.revokeObjectURL(url);
};

const Reports = () => {
  const [generating, setGenerating] = useState(null);
  const [downloaded, setDownloaded] = useState({});
  const [previewReport, setPreviewReport] = useState(null);
  const [showCustomModal, setShowCustomModal] = useState(false);
  const [customTitle, setCustomTitle] = useState('');
  const [customPeriod, setCustomPeriod] = useState('');

  const handleDownload = (report) => {
    setGenerating(report.id);
    setTimeout(() => {
      generateCSV(report);
      setGenerating(null);
      setDownloaded(prev => ({ ...prev, [report.id]: true }));
      setTimeout(() => setDownloaded(prev => ({ ...prev, [report.id]: false })), 3000);
    }, 1200);
  };

  const handleCustomGenerate = () => {
    if (!customTitle) return;
    const custom = {
      id: `RTI-2026-CUSTOM`,
      title: customTitle || 'Custom Report',
      period: customPeriod || 'Mar 2026',
      type: 'Custom',
      issues: 0,
      resolved: 0,
      pending: 0,
      summary: `Custom report generated for: ${customTitle}. Period: ${customPeriod || 'N/A'}.`,
    };
    setShowCustomModal(false);
    setGenerating('CUSTOM');
    setTimeout(() => {
      generateCSV(custom);
      setGenerating(null);
    }, 1000);
  };

  return (
    <div className="dashboard-container animate-fade-in">
      <div className="dashboard-header-flex">
        <div>
          <h1 className="page-title">Reports (RTI)</h1>
          <p className="page-subtitle">Transparency reports and compliance documentation for citizens</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => setShowCustomModal(true)}>
          <FileText size={16} /> Generate Custom Report
        </button>
      </div>

      <div className="glass-panel p-4 mb-6" style={{ display: 'flex', gap: '1rem', alignItems: 'center', background: 'rgba(245,158,11,0.08)', border: '1px solid rgba(245,158,11,0.2)', borderRadius: '10px' }}>
        <AlertCircle size={20} style={{ color: '#f59e0b', flexShrink: 0 }} />
        <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          RTI (Right To Information) reports are auto-generated from verified civic data. Click "Download PDF" to save a CSV report to your device.
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {REPORTS.map(r => (
          <div key={r.id} className="glass-panel" style={{ padding: '1.25rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <div style={{ width: 44, height: 44, borderRadius: '10px', background: 'rgba(139,92,246,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <FileText size={20} style={{ color: '#8b5cf6' }} />
              </div>
              <div>
                <p style={{ fontWeight: 700, marginBottom: 2 }}>{r.title}</p>
                <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{r.id} • {r.period} • {r.type}</p>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontWeight: 700, fontSize: '1.1rem' }}>{r.issues}</p>
                <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Total</p>
              </div>
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--status-success)' }}>{r.resolved}</p>
                <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Resolved</p>
              </div>
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontWeight: 700, fontSize: '1.1rem', color: '#f59e0b' }}>{r.pending}</p>
                <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Pending</p>
              </div>
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => setPreviewReport(r)}
              >
                Preview
              </button>
              <button
                className={`btn btn-sm ${downloaded[r.id] ? 'btn-secondary' : 'btn-primary'}`}
                onClick={() => handleDownload(r)}
                disabled={!!generating}
                style={{ minWidth: 120 }}
              >
                {downloaded[r.id]
                  ? <><CheckCircle size={14} /> Downloaded!</>
                  : generating === r.id
                  ? 'Generating...'
                  : <><Download size={14} /> Download CSV</>}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Preview Modal */}
      {previewReport && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel" style={{ maxWidth: 540, width: '100%', padding: '2rem', position: 'relative' }}>
            <button onClick={() => setPreviewReport(null)} style={{ position: 'absolute', top: 16, right: 16, background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
              <X size={20} />
            </button>
            <h2 style={{ fontWeight: 700, marginBottom: 4 }}>{previewReport.title}</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>{previewReport.id} • {previewReport.period}</p>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '1.5rem' }}>{previewReport.summary}</p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'space-around', background: 'rgba(255,255,255,0.04)', padding: '1rem', borderRadius: '8px' }}>
              {[['Total', previewReport.issues, '#fff'], ['Resolved', previewReport.resolved, 'var(--status-success)'], ['Pending', previewReport.pending, '#f59e0b']].map(([l, v, c]) => (
                <div key={l} style={{ textAlign: 'center' }}>
                  <p style={{ fontSize: '1.5rem', fontWeight: 800, color: c }}>{v}</p>
                  <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{l}</p>
                </div>
              ))}
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setPreviewReport(null)}>Close</button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => { handleDownload(previewReport); setPreviewReport(null); }}>
                <Download size={14} /> Download CSV
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Custom Report Modal */}
      {showCustomModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel" style={{ maxWidth: 420, width: '100%', padding: '2rem', position: 'relative' }}>
            <button onClick={() => setShowCustomModal(false)} style={{ position: 'absolute', top: 16, right: 16, background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
              <X size={20} />
            </button>
            <h2 style={{ fontWeight: 700, marginBottom: '1.5rem' }}>Generate Custom Report</h2>
            <div className="input-group">
              <label className="input-label">Report Title</label>
              <input className="input-field" placeholder="e.g. Sewage Overflow Audit" value={customTitle} onChange={e => setCustomTitle(e.target.value)} />
            </div>
            <div className="input-group">
              <label className="input-label">Period</label>
              <input className="input-field" placeholder="e.g. April 2026" value={customPeriod} onChange={e => setCustomPeriod(e.target.value)} />
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowCustomModal(false)}>Cancel</button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={handleCustomGenerate} disabled={!customTitle}>
                <Download size={14} /> Generate & Download
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Reports;
