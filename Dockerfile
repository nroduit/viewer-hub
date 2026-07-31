# To build, run the following command from the top level project directory:
#
# docker build -t nroduit/viewer-hub:latest -f Dockerfile .

# Based on build image containing maven and jdk
FROM maven:3-eclipse-temurin-26-noble AS builder
WORKDIR /app

# The Vaadin frontend sources live in src/main/frontend, so they arrive with "COPY src".
COPY pom.xml lombok.config vite.config.ts ./
COPY src ./src

# The cache mounts keep the Maven repository, the Vaadin-managed Node runtime and the pnpm
# store warm between builds; a cold frontend build otherwise re-downloads all of them.
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/root/.vaadin \
    --mount=type=cache,target=/root/.cache/pnpm \
    mvn -B package -P production -Dmaven.test.skip=true

# Split the fat jar into layers that change at different rates so the runtime stage only
# re-ships what actually changed.
WORKDIR /app/bin
RUN cp ../target/viewer-hub*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------------------------------------------------------------------------
# Runtime stage
# ---------------------------------------------------------------------------
# Full JDK rather than a JRE, so jcmd/jstack/jmap stay available for production diagnostics.
FROM eclipse-temurin:25-jdk-noble
WORKDIR /app

LABEL org.opencontainers.image.title="viewer-hub" \
      org.opencontainers.image.description="Weasis viewer hub" \
      org.opencontainers.image.source="https://github.com/nroduit/viewer-hub" \
      org.opencontainers.image.licenses="EPL-2.0 OR Apache-2.0"

# Ordered from least to most frequently changed to maximise layer reuse across releases.
COPY --from=builder /app/bin/extracted/dependencies/ ./
COPY --from=builder /app/bin/extracted/spring-boot-loader/ ./
COPY --from=builder /app/bin/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/bin/extracted/application/ ./
COPY --chmod=755 tools/docker-entrypoint.sh ./

# Run unprivileged. All state lives in PostgreSQL and S3, so nothing is written under /app.
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin viewer-hub
USER 10001

EXPOSE 8081
ENTRYPOINT ["/app/docker-entrypoint.sh"]