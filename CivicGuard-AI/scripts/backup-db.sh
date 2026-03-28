#!/bin/bash
# ═══════════════════════════════════════════════════════
#  CivicGuard-AI — MongoDB Backup Script
#  Creates timestamped backups of the civic complaints database
#  Suitable for RTI compliance and audit readiness
# ═══════════════════════════════════════════════════════

set -e

# Configuration
BACKUP_DIR="${BACKUP_DIR:-./backups}"
DB_NAME="${DB_NAME:-civicguard_db}"
MONGO_HOST="${MONGO_HOST:-localhost}"
MONGO_PORT="${MONGO_PORT:-27017}"
MONGO_USER="${MONGO_USER:-civicguard_admin}"
MONGO_PASS="${MONGO_PASS:-CivicGuard@2026}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}[CivicGuard Backup] Starting backup at $(date)${NC}"

# Create backup directory
BACKUP_PATH="${BACKUP_DIR}/civicguard_${TIMESTAMP}"
mkdir -p "${BACKUP_PATH}"

# ─── Option 1: Docker-based backup ───────────────────
if docker ps | grep -q civicguard-mongodb; then
    echo -e "${YELLOW}[1/4] Running MongoDB backup via Docker...${NC}"
    
    docker exec civicguard-mongodb mongodump \
        --db "${DB_NAME}" \
        --username "${MONGO_USER}" \
        --password "${MONGO_PASS}" \
        --authenticationDatabase admin \
        --out "/tmp/backup_${TIMESTAMP}"
    
    # Copy backup from container
    docker cp "civicguard-mongodb:/tmp/backup_${TIMESTAMP}/${DB_NAME}" "${BACKUP_PATH}/"
    
    # Cleanup inside container
    docker exec civicguard-mongodb rm -rf "/tmp/backup_${TIMESTAMP}"
    
    echo -e "${GREEN}✓ MongoDB dump completed${NC}"

# ─── Option 2: Local mongodump ───────────────────────
elif command -v mongodump &> /dev/null; then
    echo -e "${YELLOW}[1/4] Running mongodump locally...${NC}"
    
    mongodump \
        --host "${MONGO_HOST}" \
        --port "${MONGO_PORT}" \
        --db "${DB_NAME}" \
        --username "${MONGO_USER}" \
        --password "${MONGO_PASS}" \
        --authenticationDatabase admin \
        --out "${BACKUP_PATH}"
    
    echo -e "${GREEN}✓ MongoDB dump completed${NC}"
else
    echo -e "${RED}ERROR: Neither Docker MongoDB container nor mongodump found${NC}"
    exit 1
fi

# ─── Compress backup ─────────────────────────────────
echo -e "${YELLOW}[2/4] Compressing backup...${NC}"
cd "${BACKUP_DIR}"
tar -czf "civicguard_${TIMESTAMP}.tar.gz" "civicguard_${TIMESTAMP}"
rm -rf "civicguard_${TIMESTAMP}"
BACKUP_FILE="${BACKUP_DIR}/civicguard_${TIMESTAMP}.tar.gz"
BACKUP_SIZE=$(du -sh "${BACKUP_FILE}" | awk '{print $1}')
echo -e "${GREEN}✓ Compressed: ${BACKUP_FILE} (${BACKUP_SIZE})${NC}"
cd - > /dev/null

# ─── Generate backup manifest ────────────────────────
echo -e "${YELLOW}[3/4] Generating backup manifest...${NC}"
cat > "${BACKUP_DIR}/civicguard_${TIMESTAMP}_manifest.json" <<EOF
{
  "backup_id": "civicguard_${TIMESTAMP}",
  "database": "${DB_NAME}",
  "timestamp": "$(date -Iseconds)",
  "host": "${MONGO_HOST}:${MONGO_PORT}",
  "file": "civicguard_${TIMESTAMP}.tar.gz",
  "size": "${BACKUP_SIZE}",
  "collections": ["complaints", "users", "officers"],
  "purpose": "CivicGuard-AI Database Backup",
  "compliance": "RTI-ready, Audit-compliant",
  "retention_policy_days": ${RETENTION_DAYS},
  "generated_by": "backup-db.sh"
}
EOF
echo -e "${GREEN}✓ Manifest created${NC}"

# ─── Cleanup old backups ─────────────────────────────
echo -e "${YELLOW}[4/4] Cleaning up backups older than ${RETENTION_DAYS} days...${NC}"
DELETED=$(find "${BACKUP_DIR}" -name "civicguard_*.tar.gz" -type f -mtime +${RETENTION_DAYS} -delete -print | wc -l)
find "${BACKUP_DIR}" -name "civicguard_*_manifest.json" -type f -mtime +${RETENTION_DAYS} -delete 2>/dev/null
echo -e "${GREEN}✓ Removed ${DELETED} old backup(s)${NC}"

# ─── Summary ──────────────────────────────────────────
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════╗"
echo "║         Backup Complete!                          ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  File:   civicguard_${TIMESTAMP}.tar.gz"
echo "║  Size:   ${BACKUP_SIZE}"
echo "║  Path:   ${BACKUP_DIR}/"
echo "║  Time:   $(date)"
echo "╚══════════════════════════════════════════════════╝"
echo -e "${NC}"

# ─── Restore instructions ────────────────────────────
echo -e "${YELLOW}To restore this backup:${NC}"
echo "  tar -xzf civicguard_${TIMESTAMP}.tar.gz"
echo "  mongorestore --db ${DB_NAME} civicguard_${TIMESTAMP}/${DB_NAME}/"
echo ""
