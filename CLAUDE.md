# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

ViewerHub manages medical imaging viewers (Weasis, OHIF, 3D Slicer, MicroDicom) across an IT infrastructure. It resolves which viewer to launch, builds the manifest (XML listing studies/series/instances) that Weasis downloads from a PACS, manages Weasis resource versions/i18n/properties, and handles authentication (OAuth2 tokens for viewers hitting dcm4chee). It is a Spring Boot service with a Vaadin admin UI, registered in Eureka and configured via Spring Cloud Config Server. See full docs at https://weasis.org/en/viewer-hub/index.html.

## Build & Run

Maven wrapper is not committed; use a local `mvn` (Java 25 required).

```bash
mvn clean install                    # full build (runs Vaadin frontend build in production profile — default)
mvn spring-boot:run                  # run the app (needs env vars / VM options below and infra up)
mvn test -Punit-test                 # unit tests (skips Vaadin build-frontend)
mvn verify -Pintegration-test        # integration tests (*IntegrationTests.java, assumes frontend already built)
mvn spotless:apply                   # auto-format (Spotless — run before committing; CI enforces it)
mvn spotless:check                   # verify formatting
```

Run a single test class: `mvn test -Punit-test -Dtest=DisplayServiceImplTest`. Single method: `-Dtest=DisplayServiceImplTest#methodName`.

The app needs the local infra stack (Postgres, Redis, MinIO/S3, Keycloak, Config Server, Eureka, a PACS) running first:

```bash
cd docker && ./scripts/start.sh local
# or: docker compose -p imaging_hub -f docker-compose.yml -f docker-compose.local.yml up -d
```

Required VM options / env vars for a local run (full list in README.md "Run configuration"): `ENVIRONMENT=local`, `S3_*`, `DB_*` (Postgres on port 45101), `CONFIGSERVER_URI=http://localhost:8888`, `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE`, `BACKEND_URI`, `server.port=8081`. App runs at http://localhost:8081 (login `viewer-hub-user` / `password`).

Note: `application.yml` imports config from `configserver:` and fails fast — the Config Server must be reachable, so the app cannot start standalone without the docker stack.

## Test Layout

Three separate source roots wired via `build-helper-maven-plugin`:
- `src/main/java` — application code
- `src/test/java` — unit tests, named `*Test.java` (activated by `-Punit-test`)
- `src/integration-test/java` — integration tests, named `*IntegrationTests.java` (activated by `-Pintegration-test`)

Test resources under `src/test/resources/weasis/` hold sample Weasis `package/` and `i18n/` version trees used by package/version-management tests.

## Architecture

The Java code splits into two top-level packages under `org.viewer.hub`:
- `back/` — the backend: controllers, services, JPA entities, repositories, config.
- `front/` — the Vaadin UI: `views/` (secured admin screens), `layouts/`, `components/`, `authentication/`.

### Backend flow: viewer launch

`DisplayController` (`/display`, `/display/auth`, and IHE IID `/IHEInvokeImageDisplay` endpoints) is the main entry point. `/auth` variants add OAuth2 authentication before launching. It delegates to `DisplayService`, which dispatches to a per-viewer display service (`WeasisDisplayService`, `OhifDisplayService`, `SlicerDisplayService`, `MicroDicomDisplayService`). `ViewerSelectionService` picks the viewer based on selection rules (combinations of modality + archive, see `ViewerSelectionEntity`).

### Connector query flow (PACS abstraction)

`ConnectorQueryService` is the abstraction for querying an archive; implementations back it by different sources: `DicomConnectorQueryService` (DICOM C-FIND), `DicomWebClientService`/`WeasisConnectorQueryService` (DICOMweb), and `DbConnectorQueryService` (database). Which connectors are active is driven by Spring profiles (see below) and `ConnectorConfigurationProperties`. The manifest (built here and served by `ManifestController`) is cached in Redis.

### Spring profiles (connector wiring)

The active profile set is in `application.yml` (`spring.profiles.active`). The connector combination is chosen by swapping ONE of these:
- `connectors-dicom-no-gtw` — DICOM, no gateway (default)
- `connectors-dicom-gtw` — DICOM via the [viewer-hub-gateway](https://github.com/nroduit/viewer-hub-gateway)
- `connectors-dicomweb-gtw` — DICOMweb via gateway

The gateway handles authentication (basic, OAuth2 client-credentials & authorization-code) so viewers can pull data from the PACS. Other profiles (`oidc`, `package`, `cryptography`, `ohif`, `weasis`, `slicer`, `microdicom`, `environment-override`) toggle feature areas. README.md lists ready-made launch URLs per profile/archive.

### Persistence & multi-tenancy

JPA + Liquibase (migrations in `src/main/resources/db/changelog/`, master `db.changelog-master.yaml`). Multi-tenant datasource routing lives in `back/config/tenant/` (`TenantRoutingDatasource`, `TenantIdentifierResolver`, `ConnectionProvider`) keyed by region/datacenter. Entities use the legacy shared `hibernate_sequence` (`db_structure_naming_strategy: legacy`) — do not assume per-table sequences.

### Storage & external services

- Weasis packages, i18n bundles, and resources are stored in MinIO/S3 (`S3Service`, paths under `viewer-hub.resources-packages.*`), with compatibility mapping between client Weasis versions and uploaded resources.
- Weasis package versions are also pulled from Nexus (`WebClientNexusRepositoryConfiguration`, `WeasisRepositoryService`).
- Redis caches manifests (`RedisConfiguration`, `CacheService`).
- OAuth2/OIDC via Keycloak (`OAuth2Configuration`, `SecurityConfiguration`, `JWTSecurityService`); launch URLs can be encrypted (`CryptographyService`).

### Frontend (Vaadin)

Each admin view under `front/views/` follows a `*View` + `*Logic` + `*DataProvider` (+ `component/`) triad. Areas: `viewer/selection` (viewer selection rules), `weasis/association` (user/machine group associations), `weasis/bundle/override` (per-group Weasis property overrides — live config changes), `weasis/bundle/repository` (Nexus package assets), `weasis/i18n` (translation versions), `weasis/preference/application`. Views are security-scoped (`SecurityRole`). The Vaadin frontend is built by `vaadin-maven-plugin` (`build-frontend`) — this is skipped in the test profiles and run in the default `production` profile.

## Conventions

- Lombok is used throughout; logger field is `LOG` (see `lombok.config`). `@Slf4j`-generated loggers use that name.
- Spotless formatting is enforced — always run `mvn spotless:apply` before committing.
- Enums in `back/enums/` are the source of truth for domain vocabulary (`ViewerType`, `ConnectorType`, `ModalityType`, `SecurityRole`, `WeasisProperty*`, etc.); prefer extending these over introducing string constants.
