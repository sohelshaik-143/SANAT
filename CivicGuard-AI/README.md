# 🛡️ CivicGuard-AI

### AI-Powered Civic Issue Monitoring & Auto-Escalation System for India

> An intelligent governance platform that enables citizens to report civic issues instantly, verifies authenticity using AI, assigns complaints to the right authority, auto-escalates delays, detects fraud, and ensures full transparency and accountability in government operations.

---

## 🚨 Problem

India faces millions of unreported or unresolved civic issues daily — potholes, garbage dumping, drainage overflow, broken infrastructure, water leaks, and hazardous waste — causing public safety risks, health hazards, and billions in economic loss. Manual reporting is slow, complaints get lost, officials avoid accountability, and fake "work done" reports go undetected.

## ✅ Solution

CivicGuard-AI is a **smart, automated digital governance platform** that closes all gaps between citizens and authorities through:

| Feature | Description |
|---|---|
| 📱 Citizen Mobile Reporting | Photo capture + auto GPS tagging + instant submission |
| 🤖 AI Image Verification | Deepfake detection, category classification, authenticity scoring |
| 🎫 Auto-Ticketing | Intelligent department routing (Municipal, PWD, Water Board, etc.) |
| ⏰ Auto-Escalation | 4-tier escalation: Field Officer → Supervisor → Commissioner → State |
| 🔍 Fraud Detection | Before/After comparison, GPS consistency, timestamp validation |
| ✅ AI-Verified Completion | AI confirms actual resolution before case closure |
| 📊 Government Dashboard | Real-time heatmaps, severity ranking, officer performance metrics |
| 📋 Audit-Ready Docs | Timestamped, geo-tagged, AI-verified archives for RTI/courts |

---

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  Citizen App    │────▶│  Spring Boot API  │────▶│  MongoDB Atlas      │
│  (Mobile/Web)   │     │  (Java 21)        │     │  (Document Store)   │
└─────────────────┘     └────────┬─────────┘     └─────────────────────┘
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
          ┌─────────────┐ ┌──────────┐ ┌──────────────┐
          │ AI Engine   │ │ Escalation│ │ Notification │
          │ (Python)    │ │ Scheduler │ │ Service      │
          │ - Deepfake  │ │ - Cron    │ │ - SMS/Email  │
          │ - Classify  │ │ - Rules   │ │ - Push       │
          │ - Verify    │ │ - Alerts  │ │ - WhatsApp   │
          └─────────────┘ └──────────┘ └──────────────┘
                    
          ┌─────────────────────────────────────┐
          │  Officer Desktop App (JavaFX)       │
          │  - Complaint Management             │
          │  - Before/After Upload              │
          │  - Field Reports                    │
          └─────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend API | Spring Boot 3.x (Java 21) |
| Database | MongoDB (NoSQL) |
| AI/ML Engine | Python 3.11 + scikit-learn + OpenCV + TensorFlow |
| Desktop App | JavaFX 21 |
| Auth | Spring Security + JWT |
| Notifications | Twilio SMS + JavaMail |
| Containerization | Docker + Docker Compose |
| Geolocation | Google Maps API |

---

## 📁 Project Structure

```
CivicGuard-AI/
├── backend/                        # Spring Boot (Java 21)
│   ├── pom.xml
│   └── src/main/java/com/civicguard/
│       ├── config/                  # Security, MongoDB, AI configs
│       ├── controller/              # REST API endpoints
│       ├── service/                 # Business logic
│       ├── repository/              # MongoDB repositories
│       ├── model/                   # Domain entities
│       ├── dto/                     # Request/Response DTOs
│       └── util/                    # Image, PDF, notification utilities
├── ai-verification-engine/         # Python ML models
│   ├── model/                       # Trained model files
│   └── src/                         # Image verification scripts
├── desktop-app/                    # JavaFX officer application
├── docs/                           # Documentation & diagrams
└── scripts/                        # Deployment & backup scripts
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Python 3.11+
- MongoDB 7.0+
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone & Setup
```bash
git clone https://github.com/your-org/CivicGuard-AI.git
cd CivicGuard-AI
chmod +x scripts/*.sh
./scripts/install.sh
```

### 2. Run with Docker
```bash
docker-compose up --build
```

### 3. Run Individually
```bash
# Backend
cd backend && mvn spring-boot:run

# AI Engine
cd ai-verification-engine && pip install -r requirements.txt
python src/verify_image.py

# Desktop App
cd desktop-app && mvn javafx:run
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Citizen/Officer registration |
| POST | `/api/auth/login` | JWT authentication |
| POST | `/api/complaints` | Submit new complaint with photo |
| GET | `/api/complaints/{id}` | Get complaint details |
| GET | `/api/complaints/area/{pincode}` | Area-wise complaints |
| PUT | `/api/complaints/{id}/resolve` | Submit resolution proof |
| POST | `/api/verify/image` | AI image verification |
| POST | `/api/verify/completion` | AI completion verification |
| GET | `/api/dashboard/stats` | Dashboard analytics |
| GET | `/api/dashboard/heatmap` | Issue heatmap data |

---

## 🇮🇳 India-Specific Features

- **Department Routing**: Municipal Corporation, PWD, NHAI, Water Board, Sanitation, Electricity Board
- **Escalation Chain**: Field Officer → Block Officer → District Collector → State Commissioner
- **Reporting Integration**: Cyber Crime Helpline 1930, cybercrime.gov.in, RBI Ombudsman, CERT-IN
- **Language Support**: Hindi, Telugu, Tamil, Kannada, Malayalam, Bengali, Marathi, Gujarati, Punjabi, Odia, Assamese, Urdu
- **RTI-Ready Documentation**: All complaint records are audit-ready for Right to Information requests

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/ai-enhancement`)
3. Commit changes (`git commit -m 'Add advanced deepfake detection'`)
4. Push to branch (`git push origin feature/ai-enhancement`)
5. Open a Pull Request

---

**Built with ❤️ for Digital India** 🇮🇳
