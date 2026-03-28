# 📡 CivicGuard-AI — API Documentation

**Base URL:** `http://localhost:8080/api`  
**Authentication:** JWT Bearer Token  
**Content-Type:** `application/json` (unless multipart)

---

## 🔐 Authentication

### Register Citizen
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "Ravi Kumar",
  "email": "ravi@gmail.com",
  "phone": "+919876543210",
  "password": "SecurePass123",
  "pincode": "500001",
  "city": "Hyderabad",
  "state": "Telangana",
  "language": "te"
}

Response (201):
{
  "message": "Registration successful",
  "userId": "665a1b2c3d4e5f...",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "CITIZEN"
}
```

### Register Officer
```
POST /api/auth/register/officer
Content-Type: application/json

{
  "name": "Suresh Reddy",
  "email": "suresh.officer@gov.in",
  "phone": "+919123456789",
  "password": "OfficerPass456",
  "designation": "Junior Engineer",
  "escalationTier": 1,
  "department": "PWD",
  "employeeId": "PWD-TS-2024-0142",
  "state": "Telangana",
  "district": "Hyderabad",
  "city": "Hyderabad",
  "assignedPincodes": ["500001", "500002", "500003"]
}
```

### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "ravi@gmail.com",
  "password": "SecurePass123"
}

Response (200):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "665a1b2c3d4e5f...",
  "name": "Ravi Kumar",
  "role": "CITIZEN",
  "language": "te"
}
```

---

## 📋 Complaints

### Submit Complaint
```
POST /api/complaints
Content-Type: multipart/form-data
Authorization: Bearer <token>

Parts:
  complaint (JSON): {
    "description": "Large pothole on main road near Charminar causing accidents",
    "category": "POTHOLE",
    "latitude": 17.3616,
    "longitude": 78.4747,
    "address": "Main Road, Charminar, Hyderabad",
    "landmark": "Near Charminar Bus Stop",
    "pincode": "500002",
    "district": "Hyderabad",
    "state": "Telangana"
  }
  image (file): pothole_photo.jpg

Response (201):
{
  "id": "665b...",
  "ticketNumber": "CG-2026-TS-001042",
  "category": "POTHOLE",
  "severity": "HIGH",
  "status": "ASSIGNED",
  "assignedDepartment": "PWD",
  "assignedOfficerName": "Suresh Reddy",
  "imageVerified": true,
  "authenticityScore": 0.92,
  "deadline": "2026-03-29T10:30:00"
}
```

### Get Complaint by ID
```
GET /api/complaints/{id}
Authorization: Bearer <token>
```

### Track by Ticket Number
```
GET /api/complaints/track/CG-2026-TS-001042
Authorization: Bearer <token>
```

### Get My Complaints (Citizen)
```
GET /api/complaints/my?page=0&size=10
Authorization: Bearer <token>
```

### Get Area Complaints
```
GET /api/complaints/area/500002
Authorization: Bearer <token>
```

### Get Assigned Complaints (Officer)
```
GET /api/complaints/assigned?status=ASSIGNED&page=0&size=10
Authorization: Bearer <token>
Role Required: OFFICER, SUPERVISOR, ADMIN
```

### Submit Resolution
```
PUT /api/complaints/{id}/resolve
Content-Type: multipart/form-data
Authorization: Bearer <token>
Role Required: OFFICER, ADMIN

Parts:
  image (file): resolution_photo.jpg
  notes (text): "Pothole filled with bitumen. Road leveled."

Response (200):
{
  "id": "665b...",
  "ticketNumber": "CG-2026-TS-001042",
  "status": "RESOLVED",      // or "FRAUD_DETECTED"
  "fraudDetected": false,
  "resolvedAt": "2026-03-29T14:22:00"
}
```

---

## 🤖 AI Verification

### Verify Image
```
POST /api/verify/image
Content-Type: multipart/form-data
Authorization: Bearer <token>
Role Required: OFFICER, SUPERVISOR, ADMIN

Parts:
  image (file): suspect_photo.jpg
  latitude: 17.3616
  longitude: 78.4747

Response (200):
{
  "authentic": true,
  "authenticityScore": 0.91,
  "deepfakeDetected": false,
  "deepfakeConfidence": 0.08,
  "classifiedCategory": "POTHOLE",
  "categoryConfidence": 0.87,
  "suggestedDepartment": "PWD",
  "suggestedSeverity": "HIGH",
  "gpsValid": true,
  "timestampValid": true,
  "fraudIndicators": false,
  "fraudRiskScore": 0.05
}
```

### Verify Completion
```
POST /api/verify/completion
Content-Type: multipart/form-data

Parts:
  beforeImage (file): before.jpg
  afterImage (file): after.jpg
  latitude: 17.3616
  longitude: 78.4747
```

### Deepfake Check
```
POST /api/verify/deepfake
Content-Type: multipart/form-data

Parts:
  image (file): suspicious.jpg
```

---

## 📊 Dashboard & Analytics

### Dashboard Overview
```
GET /api/dashboard/stats
Role Required: SUPERVISOR, ADMIN

Response (200):
{
  "totalComplaints": 14523,
  "totalCitizens": 8742,
  "totalOfficers": 156,
  "statusBreakdown": {
    "submitted": 42,
    "verified": 18,
    "assigned": 234,
    "inProgress": 187,
    "escalated": 23,
    "resolved": 13890,
    "rejected": 89,
    "fraudDetected": 12
  },
  "resolutionRate": 95.64,
  "totalFraudCases": 12,
  "flaggedOfficers": 3,
  "complaintsToday": 47,
  "complaintsThisWeek": 312
}
```

### Heatmap Data
```
GET /api/dashboard/heatmap

Response: Array of { pincode, count, latitude, longitude, intensity }
```

### Category Distribution
```
GET /api/dashboard/categories

Response: Array of { category, count }
```

### Department Performance
```
GET /api/dashboard/departments

Response: Array of { department, totalComplaints, resolved, pending, escalated, resolutionRate }
```

### Officer Performance
```
GET /api/dashboard/officers

Response: Array of { officerId, name, designation, performanceScore, ... }
```

### Complaint Trend
```
GET /api/dashboard/trend?days=30

Response: Array of { date, count }
```

---

## Status Codes

| Status | Meaning |
|---|---|
| SUBMITTED | Complaint received, pending AI verification |
| VERIFIED | AI verification passed |
| ASSIGNED | Routed to officer |
| IN_PROGRESS | Officer acknowledged |
| RESOLUTION_SUBMITTED | Officer submitted proof, pending AI check |
| RESOLVED | AI verified resolution is complete |
| ESCALATED | Deadline exceeded, escalated to higher authority |
| REJECTED | AI verification failed (fake/duplicate) |
| FRAUD_DETECTED | Fraudulent resolution detected |

## Severity Levels

| Level | SLA (hours) | Description |
|---|---|---|
| EMERGENCY | 4 | Immediate threat to life/safety |
| CRITICAL | 12 | Severe infrastructure failure |
| HIGH | 24 | Significant issue requiring prompt action |
| MEDIUM | 48 | Notable issue affecting daily life |
| LOW | 72 | Minor issue for scheduled maintenance |

## Error Responses

```json
{
  "error": "Description of what went wrong",
  "status": 400
}
```

| Code | Description |
|---|---|
| 400 | Bad Request — invalid input |
| 401 | Unauthorized — missing/invalid token |
| 403 | Forbidden — insufficient role |
| 404 | Not Found |
| 413 | Image too large (>10MB) |
| 500 | Internal Server Error |
