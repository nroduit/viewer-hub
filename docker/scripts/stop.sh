#!/bin/bash
ENVIRONMENT=${1:-local} # Default value is local

case "$ENVIRONMENT" in
  local)
    docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.local.yml down
    ;;
  unsecure)
    docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.unsecure.yml down
    ;;
  secure)
    docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.secure.yml down
    ;;
  *)
    echo "Usage: $0 {local|unsecure|secure}"
    echo "Default value is local"
    exit 1
    ;;
esac