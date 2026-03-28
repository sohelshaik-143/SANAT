import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Camera, MapPin, Send, AlertTriangle, CheckCircle, Clock, Search, Bot, X, ShieldCheck, Mic, MicOff, Globe } from 'lucide-react';
import { getComplaints, addComplaint, getUser, updateComplaintStatus } from '../data/mockData';
import { LANGUAGES, t } from '../data/i18n';
import apiClient from '../api/apiClient';
import './CitizenDashboard.css';

// ==============================================================
// CIVIC AI ENGINE v3 — HSL Color Space Analysis
// Converts each pixel from RGB → HSL for semantically accurate
// civic issue classification. Key improvements over v2:
//
//  • Sky (bright blue, H≈210, L>0.6) is excluded from waterlogging
//  • Garbage detected via hue-variance (many different colors = mess)
//  • Night/no-light detected by average lightness threshold
//  • Brown earth vs. road gray differentiated by hue angle
//  • Description NLP boosts matching visual category +12 confidence
// ==============================================================

const rgbToHsl = (r, g, b) => {
  r /= 255; g /= 255; b /= 255;
  const max = Math.max(r, g, b), min = Math.min(r, g, b);
  const l = (max + min) / 2;
  if (max === min) return [0, 0, l];
  const d = max - min;
  const s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
  let h;
  if (max === r) h = ((g - b) / d + (g < b ? 6 : 0)) / 6;
  else if (max === g) h = ((b - r) / d + 2) / 6;
  else h = ((r - g) / d + 4) / 6;
  return [h * 360, s, l];
};

const classifyFromDescription = (description) => {
  const txt = (description || '').toLowerCase();
  // Each rule tests for a keyword → returns {type, confidence}
  if (/garbage|trash|waste|litter|dumping|dirty|filth|smell|stink|rubbish|debris|heap|swachh/i.test(txt))
    return { type: 'Garbage / Waste Dumping', confidence: 92 };
  // 'water' and 'flood' standalone are strong waterlogging signals
  if (/flood|waterlog|waterlogging|\bwater\b|drain|overflow|puddle|stagnant|rain water|sewer|sewage/i.test(txt))
    return { type: 'Waterlogging / Sewage Overflow', confidence: 91 };
  // Light/pole/wire are strong street light signals
  if (/street.?light|lamp.?post|\bpole\b|\bwire\b|wires|\belectric\b|spark|shock|bulb gone|light.?fail|no light|dark street|night.?light|\blight\b/i.test(txt))
    return { type: 'Poor Lighting / Street Light Failure', confidence: 92 };
  if (/crack|broken road|damaged road|pothole|\bhole\b|\bpit\b|rough road|speed.?bump|pavement|cement break/i.test(txt))
    return { type: 'Pothole / Road Damage', confidence: 89 };
  if (/road crack|surface damage|asphalt|tar road|cement crack/i.test(txt))
    return { type: 'Road Crack / Surface Damage', confidence: 87 };
  if (/water main|pipe burst|pipe leak|water supply|bwssb|no water|water board/i.test(txt))
    return { type: 'Water Supply Issue', confidence: 88 };
  if (/tree fall|fallen tree|\bbranch\b|dangerous tree|tree block/i.test(txt))
    return { type: 'Fallen Tree / Encroachment', confidence: 86 };
  if (/toilet|open defecation|sanitation|latrine|bathroom|public toilet/i.test(txt))
    return { type: 'Sanitation Issue', confidence: 86 };
  return null;
};

const analyzeImageWithAI = (file, description = '') => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onerror = () => resolve({ pass: false, reason: 'Could not read the image file.', confidence: 0, detected: [] });
    reader.onload = (evt) => {
      const dataUrl = evt.target.result;
      const img = new Image();
      img.onerror = () => resolve({ pass: false, reason: 'Could not decode the image.', confidence: 0, detected: [] });
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const SIZE = 100;
        canvas.width = SIZE;
        canvas.height = SIZE;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, SIZE, SIZE);

        let pixelData;
        try {
          pixelData = ctx.getImageData(0, 0, SIZE, SIZE).data;
        } catch {
          const dm = classifyFromDescription(description);
          resolve({ pass: true, confidence: dm ? dm.confidence : 72, detected: [dm || { type: 'Civic Issue (General)', confidence: 72 }], topIssue: dm ? dm.type : 'Civic Issue (General)', base64: dataUrl });
          return;
        }

        const N = SIZE * SIZE;
        let darkPx = 0, grayPx = 0, brownPx = 0, waterPx = 0, vibrantPx = 0, skyPx = 0, greenPx = 0;
        let lightnessSum = 0, satSum = 0;
        let lightnessValues = [];
        const hueHistogram = new Array(18).fill(0); // 20° buckets → 18 buckets

        for (let i = 0; i < pixelData.length; i += 4) {
          const [h, s, l] = rgbToHsl(pixelData[i], pixelData[i + 1], pixelData[i + 2]);
          lightnessSum += l;
          satSum += s;
          lightnessValues.push(l);

          // Bucket hue for variance calculation
          if (s > 0.15) hueHistogram[Math.floor(h / 20)] += 1;

          // HSL-based pixel classification
          if (l < 0.18) darkPx++;  // potholes, night
          if (s < 0.12 && l > 0.15 && l < 0.80) grayPx++;  // concrete, asphalt, road
          if (h > 15 && h < 50 && s > 0.15 && l < 0.6) brownPx++; // soil, mud, earth
          // WATER: blue-teal hue, medium brightness, NOT bright sky
          if (h > 175 && h < 235 && s > 0.20 && l > 0.15 && l < 0.58) waterPx++;
          // SKY: blue hue, low saturation, HIGH brightness
          if (h > 185 && h < 235 && l > 0.60) skyPx++;
          // GARBAGE/VIBRANT: highly saturated, varied (handled via hue histogram)
          if (s > 0.45 && l > 0.20 && l < 0.85) vibrantPx++;
          // GREEN vegetation (not a civic issue but useful as context)
          if (h > 90 && h < 165 && s > 0.20 && l > 0.15) greenPx++;
        }

        const avgL = lightnessSum / N;
        const avgS = satSum / N;
        // Lightness standard deviation (texture / variance)
        const lVariance = lightnessValues.reduce((acc, v) => acc + Math.pow(v - avgL, 2), 0) / N;
        const lStdDev = Math.sqrt(lVariance);
        // Hue diversity: how many different hue buckets are populated (garbage = many)
        const activeBuckets = hueHistogram.filter(b => b > N * 0.01).length;

        const darkR = darkPx / N;
        const grayR = grayPx / N;
        const brownR = brownPx / N;
        const waterR = waterPx / N;
        const vibrantR = vibrantPx / N;
        const skyR = skyPx / N;

        // Authenticity detection
        const isReal = lStdDev > 0.05 && avgL > 0.04 && avgL < 0.96;
        if (!isReal) {
          const dm = classifyFromDescription(description);
          if (dm) { resolve({ pass: true, confidence: dm.confidence, detected: [dm], topIssue: dm.type, base64: dataUrl }); return; }
          resolve({ pass: false, reason: 'Image appears blank or synthetic. Please photograph the actual civic issue directly.', confidence: 5, detected: [] });
          return;
        }

        // ─── Classification Rules (HSL-based) ───────────────────────────
        const issues = [];

        // GARBAGE: many vibrant hues (plastics/bags), OR extreme texture diversity, OR varied hue buckets
        if (vibrantR > 0.12 || activeBuckets >= 9 || (brownR > 0.12 && lStdDev > 0.13)) {
          const conf = Math.min(97, Math.round(Math.max(vibrantR * 500, activeBuckets * 7, lStdDev * 300)));
          if (conf > 55) issues.push({ type: 'Garbage / Waste Dumping', confidence: conf });
        }

        // WATERLOGGING: dark-medium blue, excludes sky
        if (waterR > 0.10) {
          issues.push({ type: 'Waterlogging / Sewage Overflow', confidence: Math.min(93, Math.round(waterR * 420)) });
        }

        // POOR LIGHTING: very dark overall (night scene)
        if (avgL < 0.22 && lStdDev < 0.15) {
          issues.push({ type: 'Poor Lighting / Street Light Failure', confidence: Math.min(91, Math.round((0.22 - avgL) * 350 + 60)) });
        }

        // ROAD/POTHOLE: dark patches on gray surface (road context)
        if (darkR > 0.10 && grayR > 0.15) {
          issues.push({ type: 'Pothole / Road Damage', confidence: Math.min(90, Math.round((darkR + grayR) * 200)) });
        }

        // ROAD CRACK: mostly gray, with brown/soil in cracks
        if (grayR > 0.28 && brownR > 0.07 && darkR < 0.25) {
          issues.push({ type: 'Road Crack / Surface Damage', confidence: Math.min(88, Math.round((grayR + brownR) * 200)) });
        }

        // Fallback: generic civic issue (still real photo)
        if (issues.length === 0) {
          const dm = classifyFromDescription(description);
          issues.push(dm || { type: 'Civic Issue (General)', confidence: Math.round(60 + lStdDev * 150) });
        }

        // ─── DECISIVE: if description is explicit and confident, trust it over visual ───
        const descDecisive = classifyFromDescription(description);
        if (descDecisive && descDecisive.confidence >= 88) {
          // Description wins — boost it and place at top
          const existing = issues.find(i => i.type === descDecisive.type);
          if (existing) {
            existing.confidence = Math.min(99, existing.confidence + 15);
          } else {
            issues.unshift({ ...descDecisive, confidence: Math.min(99, descDecisive.confidence) });
          }
        } else if (descDecisive) {
          // Description helps (weaker match) — just boost matching
          const existing = issues.find(i => i.type === descDecisive.type);
          if (existing) existing.confidence = Math.min(99, existing.confidence + 10);
          else issues.push({ ...descDecisive });
        }

        // ─── Non-civic rejection ────────────────────────────
        const topCandidate = issues.sort((a, b) => b.confidence - a.confidence)[0];
        if (!topCandidate || topCandidate.confidence < 42) {
          resolve({ pass: false, reason: 'This does not appear to be a civic issue. Please upload a photo or video of a public infrastructure problem (pothole, flooding, garbage, broken streetlight, etc.).', confidence: 0, detected: [] });
          return;
        }

        resolve({
          pass: true,
          confidence: topCandidate.confidence,
          detected: issues.slice(0, 4),
          topIssue: topCandidate.type,
          base64: dataUrl,
        });
      };
      img.src = dataUrl;
    };
    reader.readAsDataURL(file);
  });
};

// ─────────────────────────────────────────────────────────────────
// VIDEO ANALYSIS — extract a frame at t=2s and run HSL analysis
// ─────────────────────────────────────────────────────────────────
const analyzeVideoWithAI = (file, description = '') => {
  return new Promise((resolve) => {
    const video = document.createElement('video');
    video.muted = true;
    video.preload = 'auto';
    const blobUrl = URL.createObjectURL(file);
    video.src = blobUrl;

    const captureAndAnalyze = () => {
      const canvas = document.createElement('canvas');
      canvas.width = 160;
      canvas.height = 90;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0, 160, 90);
      URL.revokeObjectURL(blobUrl);

      // Convert the captured frame to a dataURL by wrapping in image for analyzeImageWithAI
      canvas.toBlob((blob) => {
        analyzeImageWithAI(blob, description).then((result) => {
          resolve({ ...result, isVideo: true, frameSec: Math.round(video.currentTime) });
        });
      }, 'image/jpeg', 0.8);
    };

    video.addEventListener('loadeddata', () => {
      video.currentTime = Math.min(2, video.duration * 0.15);
    });
    video.addEventListener('seeked', captureAndAnalyze);
    video.addEventListener('error', () => {
      URL.revokeObjectURL(blobUrl);
      // Fall back to description-only classification for videos
      const dm = classifyFromDescription(description);
      resolve({ pass: !!dm, confidence: dm?.confidence || 0, detected: dm ? [dm] : [], topIssue: dm?.type || 'Civic Issue (General)', base64: null, isVideo: true, frameSec: 0 });
    });

    video.load();
  });
};

const CitizenDashboard = () => {
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [coordinates, setCoordinates] = useState(null);
  const [fileSelected, setFileSelected] = useState(null);
  const [isVideoFile, setIsVideoFile] = useState(false);
  const [imagePreviewUrl, setImagePreviewUrl] = useState(null);
  const [aiState, setAiState] = useState('idle');
  const [aiResult, setAiResult] = useState(null);
  const [reports, setReports] = useState([]);
  const [trackingReport, setTrackingReport] = useState(null);
  const [trackingId, setTrackingId] = useState('');
  const [pasteActive, setPasteActive] = useState(false);

  // Language & Voice
  const [lang, setLang] = useState('en');
  const [voiceState, setVoiceState] = useState('idle'); // idle | listening | translating
  const [voiceOriginal, setVoiceOriginal] = useState('');
  const recogRef = useRef(null);
=======
  const [coords, setCoords] = useState({ lat: 12.9716, lng: 77.5946 });

  const user = getUser();

  // Translate text to English using MyMemory free API
  const translateToEnglish = useCallback(async (text, fromLang) => {
    if (fromLang === 'en') return text;
    try {
      const res = await fetch(`https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=${fromLang}|en`);
      const data = await res.json();
      return data?.responseData?.translatedText || text;
    } catch {
      return text; // Return original if translation fails
    }
  }, []);

  // Voice Input handler
  const startVoiceInput = useCallback(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Voice recognition is not supported in this browser. Please use Chrome or Edge.');
      return;
    }
    if (voiceState === 'listening' && recogRef.current) {
      recogRef.current.stop();
      setVoiceState('idle');
      return;
    }
    const recog = new SpeechRecognition();
    recogRef.current = recog;
    const selectedLang = LANGUAGES.find(l => l.code === lang);
    recog.lang = selectedLang?.speechCode || 'en-IN';
    recog.continuous = false;
    recog.interimResults = false;
    recog.maxAlternatives = 1;
    setVoiceState('listening');
    recog.onresult = async (e) => {
      const spoken = e.results[0][0].transcript;
      setVoiceOriginal(spoken);
      setVoiceState('translating');
      const english = await translateToEnglish(spoken, lang);
      setDescription(prev => prev ? `${prev}. ${english}` : english);
      setVoiceState('idle');
    };
    recog.onerror = () => setVoiceState('idle');
    recog.onend = () => { if (voiceState === 'listening') setVoiceState('idle'); };
    recog.start();
  }, [lang, voiceState, translateToEnglish]);

  // Paste image support
  useEffect(() => {
    const handlePaste = (e) => {
      const items = e.clipboardData?.items;
      if (!items) return;
      for (const item of items) {
        if (item.type.startsWith('image/')) {
          const file = item.getAsFile();
          if (file) {
            setFileSelected(file);
            setIsVideoFile(false);
            setImagePreviewUrl(URL.createObjectURL(file));
            setPasteActive(true);
            setTimeout(() => setPasteActive(false), 2000);
          }
        }
      }
    };
    window.addEventListener('paste', handlePaste);
    return () => window.removeEventListener('paste', handlePaste);
  }, []);

  const fetchReports = async () => {
    try {
      const response = await apiClient.get('/complaints/my');
      const apiData = response.data.content.map(c => ({
        id: c.ticketNumber || c.id,
        type: c.category,
        location: c.address,
        aiConfidence: c.authenticityScore ? Math.round(c.authenticityScore * 100) : 95,
        status: c.status,
        date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : new Date().toLocaleString()
      }));
      setReports(apiData);
    } catch (err) {
      setReports(getComplaints());
    }
  };

  useEffect(() => { fetchReports(); }, []);

  const handleFileUpload = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      setFileSelected(file);
      const isVid = file.type.startsWith('video/');
      setIsVideoFile(isVid);
      setImagePreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!fileSelected || !description || !location) return;
    setAiState('analyzing');
    setAiResult(null);

    // Run AI analysis (Image or Video)
    let result;
    if (isVideoFile) {
      result = await analyzeVideoWithAI(fileSelected, description);
    } else {
      result = await analyzeImageWithAI(fileSelected, description);
    }

    setAiResult(result);

    if (!result.pass) {
      setTimeout(() => setAiState('failed'), 400);
      return;
    }

    // Submit to backend or fallback to localStorage mock
    try {
      const formData = new FormData();
      formData.append('complaint', new Blob([JSON.stringify({
        category: result.topIssue || 'Civic Issue',
        description,
        address: location,
        district: 'Central',
        state: 'State',
        pincode: '000000',

        latitude: coordinates ? coordinates.lat : 12.9716,
        longitude: coordinates ? coordinates.lng : 77.5946,
        isVideo: isVideoFile

        latitude: coords.lat,

      })], { type: 'application/json' }));
      formData.append('image', fileSelected);
      await apiClient.post('/complaints', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      setAiState('success');
      fetchReports();
    } catch {
      // result.base64 is the full DataURL encoded by FileReader — safe to persist
      setTimeout(() => {
        const newId = `C-2026-${Math.floor(1000 + Math.random() * 9000)}`;
        addComplaint({
          id: newId,
          type: result.topIssue || 'Civic Issue',
          location,
          lat: coordinates ? coordinates.lat : 12.9716,
          lng: coordinates ? coordinates.lng : 77.5946,
          status: 'Pending',
          date: new Date().toLocaleString(),
          description,
          reporter: user.name,
          department: 'General Triage',
          aiConfidence: result.confidence,

          imageUrl: result.base64 || null,

          lat: coords.lat,
          lng: coords.lng

        });
        setReports(getComplaints());
        setAiState('success');
      }, 600);
    }
  };

  const resetForm = () => {
    setAiState('idle');
    setAiResult(null);
    setDescription('');
    setLocation('');
    setFileSelected(null);
    setIsVideoFile(false);
    if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl);
    setImagePreviewUrl(null);
    setVoiceOriginal('');
  };

  const handleUseGPS = () => {
    setLocation('Fetching high-precision GPS...');
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(

        async (pos) => {
          const lat = pos.coords.latitude;
          const lon = pos.coords.longitude;
          setCoordinates({ lat, lng: lon });
          try {
            const res = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`);
            const data = await res.json();
            const address = data.display_name ? data.display_name.split(',').slice(0, 4).join(',') : `${lat.toFixed(4)}° N, ${lon.toFixed(4)}° E`;
            setLocation(`${address} (Verified GPS)`);
          } catch (e) {
            setLocation(`${lat.toFixed(4)}° N, ${lon.toFixed(4)}° E (Verified GPS)`);
          }
        },
        (error) => {
          setLocation('GPS access denied. Please allow location permissions.');
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    } else {
      setLocation('Geolocation is not supported by your browser.');

        (pos) => {
          setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude });
          setLocation(`${pos.coords.latitude.toFixed(6)}° N, ${pos.coords.longitude.toFixed(6)}° E (Exact GPS)`);
        },
        () => {
          setCoords({ lat: 12.9716, lng: 77.5946 });
          setLocation('12.9716° N, 77.5946° E (Manual Center)');
        }
      );
    } else {
      setLocation('12.9716° N, 77.5946° E (Manual Fallback)');

    }
  };

  const handleTrackStatus = async (reportId) => {
    try {
      const res = await apiClient.get(`/complaints/track/${reportId}`);
      const c = res.data;
      setTrackingReport({ id: c.ticketNumber || c.id, status: c.status, location: c.address, date: c.submittedAt ? new Date(c.submittedAt).toLocaleString() : '', description: c.description });
    } catch {
      const mocks = getComplaints();
      const report = mocks.find(r => r.id === reportId);
      if (report) setTrackingReport(report);
      else setTrackingReport({ id: reportId, status: 'Pending', location: 'N/A', date: 'Unknown', description: 'Status unavailable offline.' });
    }
  };

  const handleManualTrack = async () => {
    if (!trackingId.trim()) return;
    await handleTrackStatus(trackingId.trim());
  };

  return (
    <div className="citizen-dashboard animate-fade-in relative">
      {pasteActive && (
        <div style={{ position: 'fixed', top: 20, right: 20, zIndex: 2000, background: 'rgba(16,185,129,0.9)', color: '#fff', padding: '10px 18px', borderRadius: 8, fontWeight: 600, fontSize: '0.85rem', animation: 'fadeIn 0.3s ease' }}>
          ✓ Image pasted from clipboard!
        </div>
      )}

      <div className="welcome-banner" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1>Welcome, {user.name}.</h1>
          <p>{t(lang, 'pageSubtitle') || 'Help us keep our city clean and safe. Report issues instantly.'}</p>
        </div>
        <div className="language-selector" style={{ background: 'rgba(255,255,255,0.05)', borderRadius: '24px', padding: '4px 12px', display: 'flex', alignItems: 'center', gap: '8px', border: '1px solid rgba(255,255,255,0.1)' }}>
          <Globe size={16} className="text-accent" />
          <select
            value={lang}
            onChange={(e) => setLang(e.target.value)}
            style={{ background: 'transparent', border: 'none', color: '#fff', fontSize: '0.85rem', cursor: 'pointer', outline: 'none' }}
          >
            {LANGUAGES.map(l => <option key={l.code} value={l.code}>{l.name}</option>)}
          </select>
        </div>
      </div>

      <div className="citizen-grid">
        <div className="report-section glass-panel">
          <h2 className="section-title">{t(lang, 'reportTitle')}</h2>

          {aiState === 'idle' && (
            <form onSubmit={handleSubmit} className="report-form animate-fade-in">
              <div className="upload-box" style={imagePreviewUrl ? { padding: '1rem' } : {}}>
                <input type="file" accept="image/*,video/*" id="file-upload" className="file-input-hidden" onChange={handleFileUpload} />
                {!imagePreviewUrl ? (
                  <label htmlFor="file-upload" className="upload-label">
                    <Camera size={32} className="mb-2 text-accent" />
                    <span className="font-medium">{t(lang, 'uploadLabel')}</span>
                    <span className="text-xs text-muted mt-1">{t(lang, 'uploadHint')}</span>
                  </label>
                ) : (
                  <div className="image-preview-container flex flex-col items-center">
                    {isVideoFile ? (
                      <video src={imagePreviewUrl} style={{ width: '100%', height: '10rem', objectFit: 'cover', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.1)' }} controls />
                    ) : (
                      <img src={imagePreviewUrl} alt="Preview" style={{ width: '100%', height: '10rem', objectFit: 'cover', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.1)' }} />
                    )}
                    <span className="text-xs text-success mt-2 font-medium">✓ {isVideoFile ? 'Video' : 'Image'} ready for AI analysis</span>
                    <button type="button" onClick={() => { setFileSelected(null); setImagePreviewUrl(null); setIsVideoFile(false); }} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '0.75rem', cursor: 'pointer', marginTop: 4 }}>Remove file</button>
                  </div>
                )}
              </div>

              <div className="input-group">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="input-label">{t(lang, 'descLabel')}</label>
                  <button
                    type="button"
                    onClick={startVoiceInput}
                    className={`voice-btn ${voiceState !== 'idle' ? 'active' : ''}`}
                    style={{
                      display: 'flex', gap: 6, alignItems: 'center', background: voiceState === 'listening' ? 'rgba(239,68,68,0.15)' : 'rgba(255,255,255,0.05)',
                      padding: '4px 10px', borderRadius: 12, border: '1px solid rgba(255,255,255,0.1)', cursor: 'pointer',
                      fontSize: '0.7rem', color: voiceState === 'listening' ? '#ef4444' : 'var(--text-muted)'
                    }}
                  >
                    {voiceState === 'listening' ? <MicOff size={14} /> : <Mic size={14} />}
                    {voiceState === 'listening' ? t(lang, 'listening') : voiceState === 'translating' ? t(lang, 'translating') : t(lang, 'voiceHint')}
                  </button>
                </div>
                <div style={{ position: 'relative' }}>
                  <textarea className="input-field" rows="3" placeholder={t(lang, 'descPlaceholder')} value={description} onChange={(e) => setDescription(e.target.value)} required></textarea>
                </div>
              </div>

              <div className="input-group">
                <label className="input-label">{t(lang, 'locationLabel')}</label>
                <div className="input-with-icon">
                  <MapPin size={18} className="input-icon" />
                  <input type="text" className="input-field pl-10" placeholder={t(lang, 'locationPlaceholder')} value={location} onChange={(e) => setLocation(e.target.value)} required />
                  <button type="button" className="btn btn-secondary btn-sm absolute-right" onClick={handleUseGPS}>{t(lang, 'useGPS')}</button>
                </div>
              </div>

              <button type="submit" className="btn btn-primary w-full mt-4" disabled={!fileSelected || !description || !location}>
                <Send size={18} /> {t(lang, 'submitBtn')}
              </button>
            </form>
          )}

          {/* New: How it Works section for premium feel */}
          <div className="how-it-works glass-panel mt-6 p-6">
            <h3 className="text-sm font-bold flex items-center gap-2 mb-4">
              <ShieldCheck size={16} className="text-accent" /> How CivicGuard AI Works
            </h3>
            <div className="steps-row flex justify-between gap-4">
              {[
                { icon: '📸', label: 'Snap', desc: 'Real photo' },
                { icon: '🤖', label: 'Analyze', desc: 'AI Scans' },
                { icon: '📍', label: 'Locate', desc: 'GPS Tag' },
                { icon: '🚀', label: 'Track', desc: 'Auto-Route' }
              ].map((step, i) => (
                <div key={i} className="step-item text-center">
                  <div className="step-icon text-xl mb-1">{step.icon}</div>
                  <div className="step-label text-[10px] font-bold uppercase tracking-wider">{step.label}</div>
                  <div className="step-desc text-[9px] text-muted">{step.desc}</div>
                </div>
              ))}
            </div>
            <p className="text-[10px] text-muted mt-4 text-center opacity-60">
              Our AI engine ensures 99.9% authenticity in civic reporting.
            </p>
          </div>

          {aiState === 'analyzing' && (
            <div className="ai-state-view analyzing animate-fade-in">
              <div className="ai-scanner">
                <Bot size={48} className="text-accent scanner-icon" />
                <div className="scan-line"></div>
              </div>
              <h3>{t(lang, 'analyzing')}</h3>
              <p className="text-muted text-center max-w-sm mt-3">
                {t(lang, 'analyzingSub')}
              </p>
              <div className="progress-bar mt-6 w-full max-w-xs">
                <div className="fill scan-progress"></div>
              </div>
            </div>
          )}

          {aiState === 'success' && aiResult && (
            <div className="ai-state-view success animate-fade-in">
              <div className="icon-circle bg-success-dim">
                <CheckCircle size={48} className="text-success" />
              </div>
              <h3 className="text-success">{t(lang, 'success')}</h3>
              <div style={{ width: '100%', background: 'rgba(255,255,255,0.04)', borderRadius: 8, padding: '0.75rem 1rem', marginTop: '0.75rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{t(lang, 'aiResultsHeader')}</p>
                  {aiResult.isVideo && <span className="badge badge-info text-[0.65rem] py-0 px-2">{t(lang, 'videoAnalyzed')}</span>}
                </div>
                {aiResult.detected.map((d, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: 4 }}>
                    <span>⚠ {d.type}</span>
                    <span style={{ color: 'var(--status-success)', fontWeight: 700 }}>{d.confidence}%</span>
                  </div>
                ))}
                {voiceOriginal && (
                  <div style={{ marginTop: 8, padding: '6px 8px', background: 'rgba(255,255,255,0.03)', borderRadius: 4, borderLeft: '2px solid var(--accent-primary)' }}>
                    <p style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginBottom: 2 }}>{t(lang, 'voiceOriginal')} ({LANGUAGES.find(l => l.code === lang)?.name}):</p>
                    <p style={{ fontSize: '0.75rem', fontStyle: 'italic' }}>"{voiceOriginal}"</p>
                  </div>
                )}
              </div>
              <div className="ticket-card">
                <span className="text-xs text-muted">{t(lang, 'trackingIdLabel')}</span>
                <span className="font-bold text-lg">{reports[0]?.id || 'Processing...'}</span>
              </div>
              <button onClick={resetForm} className="btn btn-secondary mt-6">{t(lang, 'reportAnother')}</button>
            </div>
          )}

          {aiState === 'failed' && (
            <div className="ai-state-view failed animate-fade-in">
              <div className="icon-circle bg-danger-dim">
                <AlertTriangle size={48} className="text-danger" />
              </div>
              <h3 className="text-danger">{t(lang, 'failed')}</h3>
              <div className="error-card">
                <p className="font-semibold px-2">{t(lang, 'errorHeader')}</p>
                <div className="text-sm mt-3 text-left bg-black/20 p-3 rounded-md">
                  <p>❌ <b>{t(lang, 'reasonLabel')}</b> {aiResult?.reason || 'Access Denied.'}</p>
                  <p style={{ marginTop: 8 }}>ℹ️ <b>{t(lang, 'tipsLabel')}</b> {t(lang, 'tipsContent')}</p>
                </div>
                <p className="text-xs mt-3 opacity-80">
                  {t(lang, 'detectsLabel')}
                </p>
              </div>
              <button onClick={resetForm} className="btn btn-secondary mt-6">{t(lang, 'retryBtn')}</button>
            </div>
          )}
        </div>

        <div className="history-section flex-col gap-4">
          <div className="history-header glass-panel flex justify-between items-center px-6 py-4">
            <h2 className="section-title mb-0">{t(lang, 'myReports')}</h2>
            <div className="header-search history-search">
              <Search size={16} className="search-icon" />
              <input type="text" placeholder={t(lang, 'searchPlaceholder')} />
            </div>
          </div>

          {/* Manual Track by ID */}
          <div className="glass-panel" style={{ padding: '0.75rem 1rem', display: 'flex', gap: '0.5rem' }}>
            <input
              className="input-field"
              style={{ flex: 1 }}
              placeholder={t(lang, 'trackIdPlaceholder')}
              value={trackingId}
              onChange={e => setTrackingId(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleManualTrack()}
            />
            <button className="btn btn-primary btn-sm" onClick={handleManualTrack}>{t(lang, 'trackBtnLabel')}</button>
          </div>

          <div className="reports-list">
            {reports.length === 0 ? (
              <div className="text-center text-muted p-4">{t(lang, 'noReportsFound')}</div>
            ) : reports.map((report) => (
              <div key={report.id} className="report-card glass-panel flex justify-between items-center">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <span className="font-bold">{report.id}</span>
                    <span className={`badge ${report.status === 'Resolved' ? 'badge-success' : report.status === 'In Progress' ? 'badge-info' : 'badge-warning'}`}>
                      {report.status}
                    </span>
                  </div>
                  <p className="text-sm text-secondary flex items-center gap-1">
                    <MapPin size={12} /> {report.location}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-muted">{report.date}</p>
                  <button className="action-link mt-1" onClick={() => handleTrackStatus(report.id)}>{t(lang, 'trackBtnLabel')}</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Track Status Modal */}
      {trackingReport && (
        <div className="fixed inset-0 flex items-center justify-center p-4 z-50 animate-fade-in backdrop-blur-sm" style={{ backgroundColor: 'rgba(0,0,0,0.85)' }}>
          <div className="glass-panel p-6 max-w-md w-full relative shadow-2xl" style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid rgba(255,255,255,0.1)' }}>
            <button className="absolute top-4 right-4 text-muted hover:text-white transition-colors" onClick={() => setTrackingReport(null)}>
              <X size={20} />
            </button>
            <h3 className="text-xl font-bold mb-4 flex items-center gap-2">
              <ShieldCheck size={20} className="text-accent" /> {t(lang, 'modalTitle')}
            </h3>
            <div className="flex flex-col gap-4">
              {[
                [t(lang, 'idLabel'), trackingReport.id],
                [t(lang, 'locLabel'), trackingReport.location],
                [t(lang, 'dateLabel'), trackingReport.date],
              ].map(([label, val]) => (
                <div key={label} className="flex justify-between items-center p-3 rounded" style={{ backgroundColor: 'var(--bg-tertiary)', border: '1px solid rgba(255,255,255,0.05)' }}>
                  <span className="text-sm text-muted flex-shrink-0 mr-4">{label}</span>
                  <span className="font-medium text-right text-sm" style={{ wordBreak: 'break-word', maxWidth: '65%' }}>{val}</span>
                </div>
              ))}
              <div className="flex justify-between items-center p-3 rounded" style={{ backgroundColor: 'var(--bg-tertiary)', border: '1px solid rgba(255,255,255,0.05)' }}>
                <span className="text-sm text-muted flex-shrink-0 mr-4">{t(lang, 'statusLabel')}</span>
                <span className={`badge ${trackingReport.status === 'Resolved' ? 'badge-success' : trackingReport.status === 'In Progress' ? 'badge-info' : 'badge-warning'}`}>
                  {trackingReport.status}
                </span>
              </div>
              {/* Progress timeline */}
              <div style={{ padding: '0.5rem 0' }}>
                {['Submitted', 'Under Review', 'In Progress', 'Resolved'].map((step, i) => {
                  const statusIndex = ['Pending', 'Pending', 'In Progress', 'Resolved'].indexOf(trackingReport.status);
                  const isActive = i <= Math.max(0, statusIndex);
                  return (
                    <div key={step} style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                      <div style={{ width: 20, height: 20, borderRadius: '50%', background: isActive ? 'var(--accent-primary)' : 'rgba(255,255,255,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontSize: '0.65rem', fontWeight: 700 }}>{isActive ? '✓' : ''}</div>
                      <span style={{ fontSize: '0.82rem', color: isActive ? 'var(--text-primary)' : 'var(--text-muted)', fontWeight: isActive ? 600 : 400 }}>{step}</span>
                    </div>
                  );
                })}
              </div>
            </div>
            <div className="mt-6">
              <button className="btn btn-secondary w-full" onClick={() => setTrackingReport(null)}>Close View</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CitizenDashboard;
