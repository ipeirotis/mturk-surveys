#!/bin/bash
set -euo pipefail

# Only run in remote (Claude Code on the Web) environments
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-/home/user/mturk-surveys}"
ENV_FILE="${CLAUDE_ENV_FILE:-/dev/null}"

###############################################################################
# 1. Install Google Cloud SDK (gcloud + bq) if not already installed
###############################################################################
GCLOUD_DIR="/tmp/google-cloud-sdk"
if [ ! -x "${GCLOUD_DIR}/bin/gcloud" ]; then
  echo "Installing Google Cloud SDK..."
  cd /tmp
  curl -sSL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz -o gcloud.tar.gz
  tar -xzf gcloud.tar.gz
  rm -f gcloud.tar.gz
  "${GCLOUD_DIR}/install.sh" --quiet --path-update=false --usage-reporting=false >/dev/null 2>&1
fi

# Add gcloud to PATH for this session
echo "export PATH=\"${GCLOUD_DIR}/bin:\$PATH\"" >> "$ENV_FILE"
export PATH="${GCLOUD_DIR}/bin:${PATH}"

###############################################################################
# 2. Set GCP project from .gcp-project file
###############################################################################
GCP_PROJECT=""
if [ -f "${PROJECT_DIR}/.gcp-project" ]; then
  GCP_PROJECT=$(tr -d '[:space:]' < "${PROJECT_DIR}/.gcp-project")
fi

if [ -n "$GCP_PROJECT" ]; then
  gcloud config set project "$GCP_PROJECT" --quiet 2>/dev/null || true
  echo "export GOOGLE_CLOUD_PROJECT=\"${GCP_PROJECT}\"" >> "$ENV_FILE"
  echo "export GCLOUD_PROJECT=\"${GCP_PROJECT}\"" >> "$ENV_FILE"
fi

###############################################################################
# 3. Authenticate with GCP using bootstrap SA or access token
###############################################################################
PYTHON="${GCLOUD_DIR}/platform/bundledpythonunix/bin/python3"

# Option A: Service account key via GOOGLE_APPLICATION_CREDENTIALS_BOOTSTRAP (base64-encoded)
if [ -n "${GOOGLE_APPLICATION_CREDENTIALS_BOOTSTRAP:-}" ]; then
  echo "Decoding bootstrap service account credentials..."
  SA_KEY_FILE="/tmp/gcp-sa-key.json"
  echo "$GOOGLE_APPLICATION_CREDENTIALS_BOOTSTRAP" | "$PYTHON" -c "
import sys, base64
sys.stdout.buffer.write(base64.b64decode(sys.stdin.read().strip()))
" > "$SA_KEY_FILE"

  gcloud auth activate-service-account --key-file="$SA_KEY_FILE" --quiet 2>/dev/null || true
  echo "export GOOGLE_APPLICATION_CREDENTIALS=\"${SA_KEY_FILE}\"" >> "$ENV_FILE"

  # If we have a central Secret Manager project, fetch the project-specific SA key
  if [ -n "$GCP_PROJECT" ]; then
    PROJECT_SA_KEY=$(gcloud secrets versions access latest \
      --secret="${GCP_PROJECT}-sa-key" \
      --project="$GCP_PROJECT" 2>/dev/null || echo "")
    if [ -n "$PROJECT_SA_KEY" ]; then
      PROJECT_SA_FILE="/tmp/project-sa-key.json"
      echo "$PROJECT_SA_KEY" > "$PROJECT_SA_FILE"
      gcloud auth activate-service-account --key-file="$PROJECT_SA_FILE" --quiet 2>/dev/null || true
      echo "export GOOGLE_APPLICATION_CREDENTIALS=\"${PROJECT_SA_FILE}\"" >> "$ENV_FILE"
    fi
  fi

# Option B: Pre-generated access token via CLOUDSDK_AUTH_ACCESS_TOKEN
elif [ -n "${CLOUDSDK_AUTH_ACCESS_TOKEN:-}" ]; then
  echo "Using provided access token for GCP authentication..."
  echo "export CLOUDSDK_AUTH_ACCESS_TOKEN=\"${CLOUDSDK_AUTH_ACCESS_TOKEN}\"" >> "$ENV_FILE"
fi

###############################################################################
# 4. Build Maven project (download dependencies)
###############################################################################
if [ -f "${PROJECT_DIR}/pom.xml" ]; then
  echo "Installing Maven dependencies..."
  cd "$PROJECT_DIR"
  JAVA_TOOL_OPTIONS="" mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:resolve --quiet -Dmaven.resolver.transport=wagon 2>/dev/null || true
fi

echo "Session start hook completed."
