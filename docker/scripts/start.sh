#!/bin/bash
ENVIRONMENT=${1:-local} # Default value is local

case "$ENVIRONMENT" in
  local)
    TARGET_ENV=".env" docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.local.yml up -d
    ;;
  secure)
    TARGET_ENV="secured.env" docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.secure.yml --env-file secured.env up -d
    ;;
  *)
    echo "Usage: $0 {local|secure}"
    echo "Default value is local"
    exit 1
    ;;
esac