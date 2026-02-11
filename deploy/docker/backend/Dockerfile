# Multi-stage build for NextERP Backend
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY backend/pom.xml ./backend/
COPY backend/nexterp-bom/pom.xml ./backend/nexterp-bom/
COPY backend/nexterp-platform/pom.xml ./backend/nexterp-platform/
COPY backend/nexterp-business/pom.xml ./backend/nexterp-business/
COPY backend/nexterp-shared/pom.xml ./backend/nexterp-shared/
COPY backend/nexterp-api/pom.xml ./backend/nexterp-api/
COPY backend/nexterp-assembly/pom.xml ./backend/nexterp-assembly/
COPY backend/nexterp-starters/pom.xml ./backend/nexterp-starters/

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/ ./backend/

# Build
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init tzdata

# Set timezone
ENV TZ=Asia/Shanghai

# Copy JAR file from builder
COPY --from=builder /build/backend/nexterp-assembly/nexterp-assembly-full/target/nexterp-assembly-full-*.jar app.jar

# Create non-root user
RUN addgroup -S nexterp && adduser -S nexterp -G nexterp
RUN chown -R nexterp:nexterp /app
USER nexterp

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
     "-jar", "app.jar"]
