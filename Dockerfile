# To build, run the following command from the top level project directory:
#
# docker build -t nroduit/viewer-hub:latest -f Dockerfile .

# Based on build image containing maven and jdk
FROM maven:3-eclipse-temurin-26-noble AS builder
WORKDIR /app

# Build the Spring Boot application with layers. viewer-hub excludes the OpenCV native
# libraries, so — unlike karnak — no native library staging is required here.
COPY pom.xml lombok.config ./
COPY src ./src
COPY frontend ./frontend
RUN mvn -B clean package -P production -Dmaven.test.skip=true
WORKDIR /app/bin
RUN cp ../target/viewer-hub*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# Build the final deployment image
FROM eclipse-temurin:25-jdk-noble
WORKDIR /app

COPY --from=builder /app/bin/extracted/dependencies/ ./
RUN true
COPY --from=builder /app/bin/extracted/spring-boot-loader/ ./
RUN true
COPY --from=builder /app/bin/extracted/snapshot-dependencies/ ./
RUN true
COPY --from=builder /app/bin/extracted/application/ ./
RUN true
COPY tools/docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

EXPOSE 8081
ENTRYPOINT ["/app/docker-entrypoint.sh"]
