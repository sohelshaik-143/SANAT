# ═══════════════════════════════════════════
#  CivicGuard-AI Backend — Docker Image
#  Multi-stage build for Spring Boot + Java 21
# ═══════════════════════════════════════════

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="CivicGuard Team <civicguard@gov.in>"
LABEL description="CivicGuard-AI Backend - Civic Issue Monitoring API"

WORKDIR /app

# Create upload directories
RUN mkdir -p /app/uploads/resolutions /app/uploads/verification /app/reports

# Copy built JAR
COPY --from=builder /build/target/*.jar app.jar

# Non-root user for security
RUN addgroup -S civicguard && adduser -S civicguard -G civicguard
RUN chown -R civicguard:civicguard /app
USER civicguard

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
