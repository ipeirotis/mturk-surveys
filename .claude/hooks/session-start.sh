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
# 2. Authenticate using cloud-bootstrap credentials
###############################################################################
cd "$PROJECT_DIR"
CONFIG=".cloud-config.json"
if [ -f "$CONFIG" ]; then
  PROVIDER=$(jq -r .provider "$CONFIG" 2>/dev/null || echo "")
  GCP_PROJECT=$(jq -r .project_id "$CONFIG" 2>/dev/null || echo "")

  if [ "$PROVIDER" = "gcp" ] && [ -n "$GCP_PROJECT" ]; then
    USER_EMAIL=$(git config user.email 2>/dev/null || true)
    ENC_FILE=".cloud-credentials.${USER_EMAIL}.enc"
    KEY="${GCP_CREDENTIALS_KEY:-${CLOUD_CREDENTIALS_KEY:-}}"

    if [ -n "$USER_EMAIL" ] && [ -f "$ENC_FILE" ] && [ -n "$KEY" ]; then
      if echo "$KEY" | openssl enc -d -aes-256-cbc -pbkdf2 \
           -pass stdin -in "$ENC_FILE" -out /tmp/credentials.json 2>/dev/null; then
        gcloud auth activate-service-account --key-file=/tmp/credentials.json --quiet 2>/dev/null || true
        echo "export GOOGLE_APPLICATION_CREDENTIALS=\"/tmp/credentials.json\"" >> "$ENV_FILE"
        echo "GCP credentials activated for $USER_EMAIL"
      fi
      # Note: credentials.json kept for GOOGLE_APPLICATION_CREDENTIALS; cleaned up on session end
    fi

    gcloud config set project "$GCP_PROJECT" --quiet 2>/dev/null || true
    echo "export GOOGLE_CLOUD_PROJECT=\"${GCP_PROJECT}\"" >> "$ENV_FILE"
    echo "export GCLOUD_PROJECT=\"${GCP_PROJECT}\"" >> "$ENV_FILE"
  fi
fi

###############################################################################
# 3. Build Maven project (download dependencies)
###############################################################################
if [ -f "${PROJECT_DIR}/pom.xml" ]; then
  echo "Installing Maven dependencies..."
  cd "$PROJECT_DIR"
  JAVA_TOOL_OPTIONS="" mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:resolve --quiet -Dmaven.resolver.transport=wagon 2>/dev/null || true
fi

echo "Session start hook completed."
