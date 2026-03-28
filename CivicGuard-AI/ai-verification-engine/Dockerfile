# ═══════════════════════════════════════════
#  CivicGuard-AI Verification Engine
#  Python 3.11 + Flask + OpenCV + scikit-learn
# ═══════════════════════════════════════════

FROM python:3.11-slim

LABEL maintainer="CivicGuard Team"
LABEL description="AI Image Verification & Deepfake Detection Engine"

WORKDIR /app

# System dependencies for OpenCV
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code and models
COPY src/ ./src/
COPY model/ ./model/

# Create temp directory
RUN mkdir -p /tmp/civicguard_verify

# Non-root user
RUN useradd -m -r civicguard && chown -R civicguard:civicguard /app /tmp/civicguard_verify
USER civicguard

EXPOSE 5000

HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:5000/health || exit 1

CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "2", "--timeout", "60", "src.verify_image:app"]
