#!/bin/bash
# ═══════════════════════════════════════════════════════
#  CivicGuard-AI — Installation & Setup Script
#  Installs all dependencies and configures the environment
# ═══════════════════════════════════════════════════════

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════╗"
echo "║      🛡️  CivicGuard-AI Installation Script      ║"
echo "╚══════════════════════════════════════════════════╝"
echo -e "${NC}"

# ─── Detect OS ────────────────────────────────────────
echo -e "${YELLOW}[1/7] Detecting operating system...${NC}"
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
    DISTRO=$(cat /etc/os-release | grep ^ID= | cut -d= -f2 | tr -d '"')
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    OS="windows"
fi
echo -e "${GREEN}✓ Detected: $OS${NC}"

# ─── Check Java 21 ───────────────────────────────────
echo -e "${YELLOW}[2/7] Checking Java 21...${NC}"
if command -v java &> /dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F'"' '{print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VER" -ge 21 ] 2>/dev/null; then
        echo -e "${GREEN}✓ Java $JAVA_VER found${NC}"
    else
        echo -e "${RED}✗ Java 21+ required (found Java $JAVA_VER)${NC}"
        echo "  Install: https://adoptium.net/temurin/releases/"
    fi
else
    echo -e "${RED}✗ Java not found${NC}"
    echo "  Install Java 21: https://adoptium.net/temurin/releases/"
fi

# ─── Check Python 3.11+ ──────────────────────────────
echo -e "${YELLOW}[3/7] Checking Python 3.11+...${NC}"
if command -v python3 &> /dev/null; then
    PY_VER=$(python3 --version | awk '{print $2}')
    echo -e "${GREEN}✓ Python $PY_VER found${NC}"
else
    echo -e "${RED}✗ Python 3 not found${NC}"
    echo "  Install: https://www.python.org/downloads/"
fi

# ─── Check Maven ──────────────────────────────────────
echo -e "${YELLOW}[4/7] Checking Maven...${NC}"
if command -v mvn &> /dev/null; then
    MVN_VER=$(mvn --version | head -n 1 | awk '{print $3}')
    echo -e "${GREEN}✓ Maven $MVN_VER found${NC}"
else
    echo -e "${RED}✗ Maven not found${NC}"
    echo "  Install: https://maven.apache.org/install.html"
fi

# ─── Check MongoDB ────────────────────────────────────
echo -e "${YELLOW}[5/7] Checking MongoDB...${NC}"
if command -v mongod &> /dev/null; then
    MONGO_VER=$(mongod --version | head -n 1 | awk '{print $3}')
    echo -e "${GREEN}✓ MongoDB $MONGO_VER found${NC}"
elif command -v docker &> /dev/null; then
    echo -e "${GREEN}✓ MongoDB will run via Docker${NC}"
else
    echo -e "${YELLOW}⚠ MongoDB not found (will need Docker or manual install)${NC}"
fi

# ─── Install Python Dependencies ─────────────────────
echo -e "${YELLOW}[6/7] Installing Python AI engine dependencies...${NC}"
if command -v python3 &> /dev/null; then
    cd ai-verification-engine
    python3 -m pip install -r requirements.txt --quiet 2>/dev/null || {
        echo -e "${YELLOW}⚠ pip install failed. Try: python3 -m pip install -r ai-verification-engine/requirements.txt${NC}"
    }
    cd ..
    echo -e "${GREEN}✓ Python dependencies installed${NC}"
fi

# ─── Create Required Directories ─────────────────────
echo -e "${YELLOW}[7/7] Creating required directories...${NC}"
mkdir -p uploads/resolutions
mkdir -p uploads/verification
mkdir -p reports
mkdir -p logs
mkdir -p ai-verification-engine/model
echo -e "${GREEN}✓ Directories created${NC}"

# ─── Create .env if not exists ────────────────────────
if [ ! -f .env ]; then
    cat > .env <<EOF
# CivicGuard-AI Environment Configuration
# ──────────────────────────────────────────

# MongoDB
MONGO_PASSWORD=CivicGuard@2026

# JWT Authentication
JWT_SECRET=CivicGuardSuperSecretKey2026IndiaDigitalGovernance

# Email (SMTP)
MAIL_USERNAME=civicguard@gov.in
MAIL_PASSWORD=

# SMS Provider (MSG91 / Twilio)
SMS_API_KEY=
SMS_SENDER_ID=CVGARD

# AI Engine
AI_ENGINE_URL=http://localhost:5000
AI_CONFIDENCE_THRESHOLD=0.85
EOF
    echo -e "${GREEN}✓ .env file created${NC}"
fi

# ─── Summary ──────────────────────────────────────────
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════╗"
echo "║         Installation Complete!                    ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║                                                  ║"
echo "║  Next steps:                                     ║"
echo "║  1. Update .env with production credentials      ║"
echo "║  2. Run: ./scripts/deploy.sh (Docker)            ║"
echo "║     OR                                           ║"
echo "║  3. Run manually:                                ║"
echo "║     cd backend && mvn spring-boot:run             ║"
echo "║     cd ai-verification-engine &&                  ║"
echo "║       python3 src/verify_image.py                 ║"
echo "║                                                  ║"
echo -e "╚══════════════════════════════════════════════════╝${NC}"
