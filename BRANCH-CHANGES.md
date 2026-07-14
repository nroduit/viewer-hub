# Branch changelist — `chore/modernize-spring-boot-4-vaadin-25`

Summary of the **uncommitted** working-tree changes (149 files, ~2124 insertions / ~738 deletions).
Two threads: a platform modernization (Spring Boot 4 / Vaadin 25 / Jackson 3 / Java 25 + infra) and
a resource-serving consistency fix.

---

## 1. Platform upgrade (`pom.xml`)

- **Java 25**; Spring Boot **4.1.0**, Spring Cloud **2025.1.2**, Vaadin **25.2.1**, awspring **4.0.2**,
  AWS SDK 2.35.6, Liquibase 4.33, Lombok 1.18.42, weasis-dicom-tools 5.34.3.1.
- BOM-driven: imports `spring-boot-dependencies` (not the starter parent), so the `repackage` goal is
  bound explicitly; most versions now managed transitively.

## 2. Spring Boot 4 API adjustments

- OAuth2 starters renamed → `spring-boot-starter-security-oauth2-client` / `-resource-server`;
  Liquibase → `spring-boot-liquibase`; test starters → `spring-boot-starter-webmvc-test`,
  `-data-jpa-test`.
- Touched config: `SecurityConfiguration`, `RedisConfiguration`, `WebConfiguration`,
  `ConnectionProvider`, `ExceptionControllerAdvice`, `ViewerHubApplication`.

## 3. Jackson 3 migration

- Package move `com.fasterxml.jackson.*` → `tools.jackson.*`; `ObjectMapper` → `JsonMapper`
  (`jackson-annotations` kept for the annotation API).
- Applied across `JacksonUtil`, all `model/` + `model/manifest/` + `model/patient/` DTOs,
  `WeasisPropertyEntitySerializer`, entities and repositories.
- **New `Jackson3EnumParityTest`** — regression guard that enums (de)serialize by `name()`, not by
  Lombok `toString()`, for both query-param deduction and the MVC JSON converter.

## 4. Vaadin 25 UI migration

- Streams API: uploads move to `UploadHandler` / `ByteArrayInputStream` (`I18nFileUpload`,
  `PackageVersionFileUpload`); theme-variant calls replaced with `getElement().getThemeList()`.
- Frontend relocated `frontend/` → **`src/main/frontend/`** (index, themes, view CSS); new
  `vite.config.ts`, `vaadin-featureflags.properties`, and theme CSS (`grid-theme`, `launch-grid`,
  `notification-theme`). `vaadin-dev` added (no longer transitive in Vaadin 25).
- Broad but mechanical touches across `front/views/**`, `layouts/`, `components/`, `AppShell`.

## 5. Infrastructure & CI (new)

- `Dockerfile`, `.dockerignore`, `tools/docker-entrypoint.sh`; refreshed `docker/` compose stack
  (dcm4chee, keycloak, ohif, orthanc, technical-stack, `.env`).
- GitHub Actions: `maven.yml`, `sonar.yml`, `docker-publish.yml`; `dependabot.yml`; `SECURITY.md`.
- Docs: `CLAUDE.md` (new), `README.md`.

## 6. Resource-serving consistency fix

Fixes corrupted/stale downloads of i18n and package resources served from the S3-backed `/weasis/**`
handler, and makes publishing atomic.

**Problems:** a re-upload in place left a stale cached content-length → truncated downloads; uploads
to deterministic keys were fire-and-forget → torn / half-written folder reads.

**Fix — immutable, build-stamped, atomic publish:**
- Each upload lands in an immutable `…/<version>/<buildId>/…` dir; a single `current` pointer object
  is PUT **only after every file is durable**; `buildId` is stored on the catalog entity and folded
  into the launch URL, pinning each Weasis session to one coherent snapshot.
- Hardening: `UploadResource` uses `fromBytes(readAllBytes())` (exact length, no leaked executor);
  S3 checksums `WHEN_REQUIRED` → `WHEN_SUPPORTED` (⚠️ verify against target MinIO); `WebConfiguration`
  re-enables the resolver cache + `Cache-Control: immutable` (safe now that URLs are immutable);
  `PackageServiceImpl` chains upload → zip → mapping → publish so refresh runs after all files land.
- New `db.changelog-1.3.xml` adds nullable `build_id` to `package_version` and `i18n` (guarded by
  `columnExists`). Backward compatible: legacy versions stay at the top level and serve via the
  legacy URL.
- New **`BuildRetentionService`** — daily cleaner of obsolete `<version>/<uuid>/…` build dirs, gated
  on `now − pointerLastModified > gracePeriod` (default `PT24H`), never touching the active build or
  legacy content. Config under `viewer-hub.resources-packages.build-retention`.

Files: `WebConfiguration`, `S3ClientConfiguration`, `UploadResource`, `PackageVersionEntity`,
`I18nEntity`, `PackageServiceImpl`, `I18nServiceImpl`, `LaunchPreferenceServiceImpl`, `S3Service(Impl)`,
`I18nService`, `PackageUtil`, `application.yml`, `db.changelog-1.3.xml`, `BuildRetentionService` (+test).

---

## Verification & follow-ups

- `mvn -Punit-test test` passes (incl. new `BuildRetentionServiceTest`, `Jackson3EnumParityTest`);
  `mvn spotless:check` clean; integration tests compile.
- **Not yet done:** end-to-end round-trip against MinIO (upload → launch → download), primarily to
  confirm the `WHEN_SUPPORTED` checksum change round-trips on the deployed MinIO version.
- Suggested commit split: (1) platform/pom upgrade, (2) Jackson 3, (3) Vaadin 25 + frontend move,
  (4) infra/CI, (5) resource-consistency (hardening → build-stamping → retention).
