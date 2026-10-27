# AGENTS.md

Guidance for AI coding agents and human contributors working in this repository.
The nearest `AGENTS.md` up the directory tree applies.
Read [CONTRIBUTING.md](CONTRIBUTING.md) for how issues and pull requests are handled.

## Project overview

ViewerHub manages medical imaging viewers (Weasis, OHIF, 3D Slicer, MicroDicom) across an IT infrastructure: it builds the launch of a viewer for a study (Weasis manifests, launch URLs, tokens), selects the viewer by rules, and serves the Weasis packages and their configuration.
It is a Spring Boot application with a Vaadin admin UI, Liquibase for the database schema, Redis for the cache, S3 (Minio) for packages, OAuth2 (Keycloak) for security and Eureka for discovery; the [gateway](https://github.com/nroduit/viewer-hub-gateway) is a separate repository.
It requires **JDK 25** (`java-version` in `pom.xml`) and Maven; the Vaadin frontend build needs Node 24 and pnpm.
User and administrator documentation is on the [ViewerHub site](https://weasis.org/en/viewer-hub/index.html); the README describes the local Docker services and the run configuration.

## Commands

```bash
mvn -Punit-test,coverage -B verify                      # what CI runs; the unit-test profile skips the frontend build
mvn -Punit-test test -Dtest=ClassName#methodName        # one test
mvn clean install                                       # full build with the production frontend (default profile)
```

Integration tests need the Docker infrastructure described in the README (database, Redis, Minio, Keycloak, archives) and do not run in CI.

## Definition of done

A change is complete when:

- `mvn -Punit-test,coverage -B verify` passes.
- New behavior has tests, and a fixed bug has a test that failed before the fix.
- A schema change comes with a new Liquibase changelog.
- A change to authentication, OAuth2 token handling, URL cryptography, manifest generation or access control states its security impact in the description.
- The description of the change says how it was verified: unit tests, or manual testing against which archive, viewer and profile.

## Architecture

- `org.viewer.hub.back`: the server side.
  `controller` (launch and REST endpoints), `service` (one display service per viewer: `WeasisDisplayService`, `OhifDisplayService`, `SlicerDisplayService`, `MicroDicomDisplayService`; connector queries for DICOM, DICOMweb and database connectors; packages, preferences, viewer selection, cryptography, S3), `entity` and `repository` (JPA), `model`, `config`, `security`, `enums`, `constant`, `util`.
- `org.viewer.hub.front`: the Vaadin admin UI (`views`, `layouts`, `components`, `authentication`, `help`).
- `src/main/resources`: `application.yml` and `application-redis.yml`, the Liquibase changelogs in `db/changelog/changes/` (included by `db.changelog-master.yaml`), the templates, and the architecture diagrams in `documentation/`.
- Connector profiles (`connectors-dicom-no-gtw`, `connectors-dicom-gtw`, `connectors-dicomweb-gtw`) select how studies are queried and whether the gateway is used.

## Code conventions

- **License header**: every source file keeps the EPL-2.0 OR Apache-2.0 header of the existing files.
- **Formatting**: no formatter is enforced by the build; match the formatting, naming and Lombok usage of the surrounding code.
- **Security**: tokens, keys and passwords come from the configuration server, the environment or secret files; never hard-code them, log them or put them in a launch URL in clear.
  Keep the encryption of launch parameters and the role checks intact.
- **Logging**: use SLF4J through Lombok `@Slf4j` with `{}` placeholders, never `System.out` or `printStackTrace()`.
  Never log patient identifiers, tokens or decrypted parameters above `DEBUG`.
- **Resilience**: calls to S3, the database, archives and other services fail gracefully (circuit breaker, timeouts) and never block the launch of a viewer longer than needed.
- **Database**: never edit a Liquibase changeset that has been released; add a new `db.changelog-<version>.xml` instead.
- **Vaadin UI**: long work never runs on the UI thread; the UI is updated through `UI.access`.
- **Javadoc**: keep it minimal: one short sentence on the intent of a public API, no restated method names, no `TODO` placeholders.
- **Code quality**: small focused methods, expressive names, early returns.
  Remove redundant code: unused imports and locals, dead branches, duplicated logic, comments that repeat the code.

## Testing

- Use **JUnit Jupiter** (`org.junit.jupiter.*`), **Mockito** and Spring Boot test support.
  Use JUnit's `Assertions`; a few tests use AssertJ, do not spread it to new ones.
- Tests mirror the main packages under `src/test/java`; fixtures go in `src/test/resources`.
- Unit tests need no Docker service, network or real credentials.

## Writing conventions

These rules apply to Markdown docs, Javadoc, comments, UI messages and commit messages.

- **American English**: center, color, gray, behavior, normalize, authorization, license (noun).
  DICOM keywords and external API names keep their own spelling.
- **Markdown**:
  - One sentence per line, so that diffs show the changed sentence only.
  - A blank line before and after headings, lists, tables, code blocks and quotes; never more than one blank line.
  - A hard line break is a trailing `\`, never two trailing spaces.
  - CommonMark with GFM tables and fenced code blocks that name their language (`java`, `bash`, `yaml`, `text`, …); no raw HTML.
  - Relative links between files in the repository.
- `.editorconfig` sets UTF-8, LF and a final newline; `.markdownlint.yaml` holds the Markdown rules a linter can check.

## Git and commits

- Work on a feature branch and merge through a pull request.
- One logical change per commit; never mix formatting or refactoring with a functional change.
- Subject line: a type prefix (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `build:`, `ci:`) and an imperative summary without a trailing period, at most about 72 characters (for example `fix: implement circuit breaker for package versions`).
- A body, after a blank line, explains why when the subject is not enough.
- Never push, force-push, rewrite published history or create a release unless explicitly asked.

## Boundaries

### Never

- Commit or paste patient data: manifests, logs, screenshots and DICOM attachments are synthetic or fully anonymized.
- Commit secrets, real hostnames, tokens or keys; the local development values of the README are the only ones allowed.
- Edit generated content: `target/`, `src/main/frontend/generated/`, `vite.generated.ts`.
- Disable or skip tests to make a build pass.

### Ask first

- Any change to authentication, OAuth2 token handling, URL cryptography, manifest generation or access control.
- A database schema change.
- Adding, upgrading or removing a dependency or a Maven plugin; a new dependency must have a license compatible with EPL-2.0 OR Apache-2.0.
- Changing the launch URLs, the REST endpoints, the connector profiles or the configuration properties, which deployed viewers and the gateway use.
- Changing the CI or docker workflows.
