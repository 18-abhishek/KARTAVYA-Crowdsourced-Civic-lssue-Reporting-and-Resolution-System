# ==============================================================================
# Stage 1: Build the Executable Fat JAR with JDK 21
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle configuration files first to leverage Docker layer caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts

# Fix Windows CRLF line endings and grant execute permissions
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Copy source code
COPY src src

# Build executable fat JAR (bootJar)
RUN ./gradlew clean bootJar --no-daemon -x test --stacktrace --console=plain

# ==============================================================================
# Stage 2: Lightweight Production JRE 21 Runtime Container
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a storage directory for uploaded files
RUN mkdir -p /app/data/uploads/images /app/data/uploads/audio

# Copy executable fat JAR from builder stage
COPY --from=builder /app/build/libs/kartavya-backend-server.jar /app/app.jar

# Render injects $PORT environment variable dynamically (e.g. 10000 or 8080)
ENV PORT=8080
ENV STORAGE_DIR=/app/data/uploads

EXPOSE 8080

# Execute the Spring Boot fat JAR, dynamically binding to Render's $PORT
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
