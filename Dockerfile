# Build stage
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to appuser
RUN chown appuser:appuser app.jar

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]