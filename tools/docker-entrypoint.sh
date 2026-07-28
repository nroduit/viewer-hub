#!/bin/bash
#
#  Copyright (c) 2022-2026 Weasis Team and other contributors.
#
#  This program and the accompanying materials are made available under the terms of the Eclipse
#  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
#  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
#
#  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
#
#

set -e

# https://github.com/keycloak/keycloak-containers/blob/master/server/tools/docker-entrypoint.sh#L4-L28
# usage: file_env VAR [DEFAULT]
#    ie: file_env 'XYZ_DB_PASSWORD' 'example'
# (will allow for "$XYZ_DB_PASSWORD_FILE" to fill in the value of
#  "$XYZ_DB_PASSWORD" from a file, especially for Docker's secrets feature)
file_env() {
    local var="$1"
    local fileVar="${var}_FILE"
    local def="${2:-}"
    if [[ ${!var:-} && ${!fileVar:-} ]]; then
        echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
        exit 1
    fi
    local val="$def"
    if [[ ${!var:-} ]]; then
        val="${!var}"
    elif [[ ${!fileVar:-} ]]; then
        val="$(< "${!fileVar}")"
    fi

    if [[ -n $val ]]; then
        export "$var"="$val"
    fi

    unset "$fileVar"
}

# viewer-hub reads its configuration from the Spring Cloud Config Server; most settings below are
# referenced in application.yml as ${ENV_VAR} and are picked up directly from the environment via
# Spring's relaxed binding. This entrypoint only resolves Docker "*_FILE" secrets, applies a few
# sane defaults, and then launches the app.

#############################
#  VIEWER-HUB ENVIRONMENT   #
#############################
: "${ENVIRONMENT:=prod}"

# HTTP ports (default aligned with the image's EXPOSE 8081)
: "${SERVER_PORT:=8081}"
export SERVER_PORT

###########################
#  DATABASE (PostgreSQL)  #
###########################
file_env 'DB_USER'
file_env 'DB_PASSWORD'
file_env 'DB_NAME'
: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_USER:=viewer-hub}"
: "${DB_PASSWORD:=viewer-hub}"
: "${DB_NAME:=viewer-hub}"
export DB_HOST DB_PORT DB_USER DB_PASSWORD DB_NAME

#########################
#  OBJECT STORAGE (S3)  #
#########################
file_env 'S3_ACCESS_KEY'
file_env 'S3_SECRET_KEY'
export S3_ACCESS_KEY S3_SECRET_KEY

######################################
#  OPENID CONNECT PROVIDER (secret)  #
######################################
file_env 'OIDC_CLIENT_SECRET'
[[ -n ${OIDC_CLIENT_SECRET:-} ]] && export OIDC_CLIENT_SECRET

# Extra JVM options may be supplied through JAVA_OPTS
exec java ${JAVA_OPTS:-} -jar application.jar
