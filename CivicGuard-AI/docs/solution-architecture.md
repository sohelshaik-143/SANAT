# 🏗️ CivicGuard-AI — Solution Architecture

## System Overview

CivicGuard-AI is a three-tier architecture with an AI microservice, designed for scalability across India's diverse civic infrastructure.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│                                                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐     │
│  │ Citizen App  │  │ Officer      │  │ Admin Dashboard    │     │
│  │ (Mobile/Web) │  │ Desktop App  │  │ (Web Portal)       │     │
│  │ - Report     │  │ (JavaFX)     │  │ - Analytics        │     │
│  │ - Track      │  │ - Manage     │  │ - Heatmaps         │     │
│  │ - Photo      │  │ - Resolve    │  │ - Performance      │     │
│  │ - GPS Auto   │  │ - Reports    │  │ - Fraud Alerts     │     │
│  └──────┬───────┘  └──────┬───────┘  └────────┬───────────┘     │
│         │                 │                    │                  │
└─────────┼─────────────────┼────────────────────┼─────────────────┘
          │     HTTPS/REST  │                    │
          ▼                 ▼                    ▼
┌──────────────────────────────────────────────────────────────────┐
│                      API GATEWAY LAYER                           │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Spring Boot API (Java 21)                    │   │
│  │                                                           │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌───────────────────┐ │   │
│  │  │ Auth        │ │ Complaint    │ │ Verification      │ │   │
│  │  │ Controller  │ │ Controller   │ │ Controller        │ │   │
│  │  └──────┬──────┘ └──────┬───────┘ └───────┬───────────┘ │   │
│  │         │               │                  │              │   │
│  │  ┌──────▼──────────────▼──────────────────▼───────────┐  │   │
│  │  │              SERVICE LAYER                          │  │   │
│  │  │  ┌─────────────────┐ ┌──────────────────────────┐  │  │   │
│  │  │  │ ComplaintService │ │ AIImageValidationService │  │  │   │
│  │  │  │ - Submit         │ │ - verifyImage()          │  │  │   │
│  │  │  │ - Resolve        │ │ - verifyCompletion()     │  │  │   │
│  │  │  │ - Route          │ │ - checkDeepfake()        │  │  │   │
│  │  │  │ - Assign         │ │ - (WebClient → Python)   │  │  │   │
│  │  │  └─────────────────┘ └──────────────────────────┘  │  │   │
│  │  │  ┌──────────────────┐ ┌─────────────────────────┐  │  │   │
│  │  │  │ AutoEscalation   │ │ ReportService           │  │  │   │
│  │  │  │ Service           │ │ - Dashboard stats       │  │  │   │
│  │  │  │ - @Scheduled     │ │ - Heatmaps              │  │  │   │
│  │  │  │ - 15min check    │ │ - Department perf       │  │  │   │
│  │  │  │ - 4-tier ladder  │ │ - Officer rankings      │  │  │   │
│  │  │  └──────────────────┘ └─────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
          │                           │
          ▼                           ▼
┌─────────────────────┐    ┌─────────────────────────────────────┐
│  DATA LAYER          │    │  AI VERIFICATION ENGINE              │
│                      │    │  (Python 3.11 + Flask)               │
│  ┌────────────────┐ │    │                                      │
│  │  MongoDB 7.0   │ │    │  ┌────────────┐ ┌────────────────┐  │
│  │                │ │    │  │ Deepfake   │ │ Issue          │  │
│  │  Collections:  │ │    │  │ Detector   │ │ Classifier     │  │
│  │  - complaints  │ │    │  │ - ELA      │ │ - Color hist   │  │
│  │  - users       │ │    │  │ - Noise    │ │ - Texture      │  │
│  │  - officers    │ │    │  │ - EXIF     │ │ - Keywords     │  │
│  │                │ │    │  │ - Metadata │ │ - CNN (prod)   │  │
│  │  Indexes:      │ │    │  └────────────┘ └────────────────┘  │
│  │  - GeoSpatial  │ │    │                                      │
│  │  - Compound    │ │    │  ┌─────────────────────────────────┐ │
│  │  - Text        │ │    │  │ Completion Verifier             │ │
│  └────────────────┘ │    │  │ - Before/After SSIM comparison  │ │
│                      │    │  │ - GPS consistency check         │ │
└─────────────────────┘    │  │ - Fraud pattern detection       │ │
                           │  └─────────────────────────────────┘ │
                           └─────────────────────────────────────┘
```

---

## Data Flow

### Complaint Submission Flow
```
Citizen captures photo → GPS auto-tagged → Uploaded to API
  → AI Engine: deepfake check + classification + GPS validation
  → Ticket generated (CG-2026-TS-XXXXXX)
  → Department auto-routed (PWD / Municipal / Water Board)
  → Officer auto-assigned (by pincode + workload)
  → SLA deadline set (based on severity)
  → SMS + Email notification sent
  → Complaint saved to MongoDB
```

### Auto-Escalation Flow
```
Every 15 minutes:
  → Scan for overdue complaints
  → For each overdue complaint:
    → Level 1 → Level 2 (Block Officer)      [after SLA breach]
    → Level 2 → Level 3 (District Collector)  [after 2nd deadline]
    → Level 3 → Level 4 (State Commissioner)  [after 3rd deadline]
  → Notify new authority (SMS + Email)
  → Warn previous officer (performance impact)
  → Notify citizen (escalation update)
```

### Resolution Verification Flow
```
Officer uploads resolution photo → API receives before + after images
  → AI Engine: compare before/after
    → SSIM structural similarity
    → GPS consistency check
    → Deepfake detection on after image
    → Hash comparison (detect recycled images)
  → If fraud detected:
    → Flag officer, increment fraud counter
    → If 3+ consecutive frauds: RED FLAG + auto-complaint
  → If verified:
    → Mark as RESOLVED
    → Update officer performance score
    → Notify citizen
```

---

## Technology Stack Details

| Component | Technology | Justification |
|---|---|---|
| Backend API | Spring Boot 3.x (Java 21) | Enterprise-grade, scalable, widely used in Indian govt IT |
| Database | MongoDB 7.0 | Flexible schema for varied complaint data, geo-spatial queries |
| AI Engine | Python 3.11 + Flask | Rich ML ecosystem, easy model integration |
| Image Analysis | OpenCV + Pillow | Industry-standard image processing |
| ML Models | scikit-learn (baseline), TensorFlow (production) | Proven accuracy for classification tasks |
| Desktop App | JavaFX 21 | Native performance, works on govt Windows machines |
| Auth | JWT + BCrypt | Stateless, scalable authentication |
| Notifications | Spring Mail + Twilio/MSG91 | Email + SMS for Indian mobile networks |
| Containers | Docker + Docker Compose | Consistent deployment across environments |
| Scheduling | Spring @Scheduled | Reliable cron-like job execution |

---

## Security Architecture

1. **Authentication**: JWT tokens with BCrypt password hashing (factor 12)
2. **Authorization**: Role-based access control (CITIZEN, OFFICER, SUPERVISOR, ADMIN)
3. **Data Protection**: Aadhaar stored as SHA-256 hash only
4. **API Security**: CORS whitelist, rate limiting, input validation
5. **Image Security**: File type validation, size limits, sanitized filenames
6. **Container Security**: Non-root user, minimal base images, health checks

---

## Scalability Considerations

- **Horizontal scaling**: Stateless API design allows multiple instances behind a load balancer
- **Database**: MongoDB Atlas for managed scaling with automatic sharding
- **AI Engine**: Deployed as independent microservice, can scale separately
- **CDN**: Complaint images served through CDN for fast citizen access
- **Caching**: Redis layer for frequently accessed dashboard data
- **Queue**: RabbitMQ/Kafka for async notification processing (production)

---

## Deployment Options

| Environment | Configuration |
|---|---|
| Development | Docker Compose (local) |
| Staging | AWS EC2 / Azure VM |
| Production | Kubernetes (EKS/AKS) or NIC Cloud (Indian govt) |
| Database | MongoDB Atlas (managed) or on-premise |
| CDN | CloudFront / Azure CDN for image serving |
