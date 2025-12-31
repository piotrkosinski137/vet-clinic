# Multi-stage Dockerfile for Vet Clinic Backend
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom files first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY common/pom.xml common/
COPY patient-module/pom.xml patient-module/
COPY client-module/pom.xml client-module/
COPY veterinarian-module/pom.xml veterinarian-module/
COPY visit-module/pom.xml visit-module/
COPY billing-module/pom.xml billing-module/
COPY compliance-module/pom.xml compliance-module/
COPY application/pom.xml application/

# Download dependencies (cached if pom files don't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY common/src common/src/
COPY patient-module/src patient-module/src/
COPY client-module/src client-module/src/
COPY veterinarian-module/src veterinarian-module/src/
COPY visit-module/src visit-module/src/
COPY billing-module/src billing-module/src/
COPY compliance-module/src compliance-module/src/
COPY application/src application/src/

# Build the application
RUN ./mvnw clean package -DskipTests -Pfast -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add non-root user for security
RUN addgroup -g 1001 -S vetclinic && \
    adduser -u 1001 -S vetclinic -G vetclinic

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/application/target/application-*.jar app.jar

# Set ownership
RUN chown -R vetclinic:vetclinic /app

# Switch to non-root user
USER vetclinic

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containers (optimized for Railway 1GB limit)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=60.0 \
    -XX:InitialRAMPercentage=40.0 \
    -Xss512k \
    -XX:MaxMetaspaceSize=128m \
    -XX:ReservedCodeCacheSize=96m"

# Set Spring profile to railway
ENV SPRING_PROFILES_ACTIVE=railway

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
