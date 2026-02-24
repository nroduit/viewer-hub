#!/bin/bash
#
#  Copyright (c) 2022-2025 Weasis Team and other contributors.
#
#  This program and the accompanying materials are made available under the terms of the Eclipse
#  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
#  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
#
#  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
#
#

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