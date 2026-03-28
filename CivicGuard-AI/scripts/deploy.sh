#!/bin/bash
# ═══════════════════════════════════════════════════════
#  CivicGuard-AI — Deployment Script
#  Builds and deploys all services using Docker Compose
# ═══════════════════════════════════════════════════════

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════╗"
echo "║      🛡️  CivicGuard-AI Deployment Script        ║"
echo "║  AI-Powered Civic Issue Monitoring System        ║"
echo "╚══════════════════════════════════════════════════╝"
echo -e "${NC}"

# ─── Check prerequisites ─────────────────────────────
echo -e "${YELLOW}[1/6] Checking prerequisites...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}ERROR: Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}ERROR: Docker Compose is not installed.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker and Docker Compose found${NC}"

# ─── Environment setup ───────────────────────────────
echo -e "${YELLOW}[2/6] Setting up environment...${NC}"

if [ ! -f .env ]; then
    echo -e "${YELLOW}Creating .env file with defaults...${NC}"
    cat > .env <<EOF
# CivicGuard-AI Environment Variables
MONGO_PASSWORD=CivicGuard@2026
JWT_SECRET=CivicGuardSuperSecretKey2026IndiaDigitalGovernance
MAIL_USERNAME=civicguard@gov.in
MAIL_PASSWORD=
SMS_API_KEY=
EOF
    echo -e "${GREEN}✓ .env file created (update with production values)${NC}"
else
    echo -e "${GREEN}✓ .env file exists${NC}"
fi

# ─── Build images ─────────────────────────────────────
echo -e "${YELLOW}[3/6] Building Docker images...${NC}"

docker compose build --no-cache
echo -e "${GREEN}✓ All images built successfully${NC}"

# ─── Stop existing containers ─────────────────────────
echo -e "${YELLOW}[4/6] Stopping existing containers...${NC}"

docker compose down --remove-orphans 2>/dev/null || true
echo -e "${GREEN}✓ Previous containers stopped${NC}"

# ─── Start services ──────────────────────────────────
echo -e "${YELLOW}[5/6] Starting all services...${NC}"

docker compose up -d
echo -e "${GREEN}✓ Services starting...${NC}"

# ─── Wait for health checks ──────────────────────────
echo -e "${YELLOW}[6/6] Waiting for services to become healthy...${NC}"

MAX_WAIT=120
WAITED=0

while [ $WAITED -lt $MAX_WAIT ]; do
    MONGO_HEALTHY=$(docker inspect --format='{{.State.Health.Status}}' civicguard-mongodb 2>/dev/null || echo "starting")
    BACKEND_HEALTHY=$(docker inspect --format='{{.State.Health.Status}}' civicguard-backend 2>/dev/null || echo "starting")
    AI_HEALTHY=$(docker inspect --format='{{.State.Health.Status}}' civicguard-ai-engine 2>/dev/null || echo "starting")

    if [ "$MONGO_HEALTHY" = "healthy" ] && [ "$BACKEND_HEALTHY" = "healthy" ] && [ "$AI_HEALTHY" = "healthy" ]; then
        break
    fi

    echo -ne "  MongoDB: ${MONGO_HEALTHY} | Backend: ${BACKEND_HEALTHY} | AI: ${AI_HEALTHY} (${WAITED}s)\r"
    sleep 5
    WAITED=$((WAITED + 5))
done

echo ""

# ─── Summary ──────────────────────────────────────────
echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════╗"
echo "║        🎉 Deployment Complete!                   ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║                                                  ║"
echo "║  Backend API:    http://localhost:8080/api        ║"
echo "║  API Health:     http://localhost:8080/actuator   ║"
echo "║  AI Engine:      http://localhost:5000/health     ║"
echo "║  Mongo Express:  http://localhost:8081 (dev)      ║"
echo "║                                                  ║"
echo "║  Credentials:                                    ║"
echo "║  Mongo Admin:    admin / civicguard               ║"
echo "║                                                  ║"
echo "╚══════════════════════════════════════════════════╝"
echo -e "${NC}"

# Show container status
echo -e "${YELLOW}Container Status:${NC}"
docker compose ps
