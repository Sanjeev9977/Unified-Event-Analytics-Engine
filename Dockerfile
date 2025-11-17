# -------------------------------
# Stage 1: Build
# -------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw . 
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# -------------------------------
# Stage 2: Run
# -------------------------------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR from builder stage (wildcard avoids hardcoding version)
COPY --from=builder /app/target/*.jar app.jar

# Copy application.yml explicitly (optional, ensures config is present)
COPY src/main/resources/application.yml ./application.yml

# Expose application port
EXPOSE 8080

# Set environment variables (Railway provides these automatically)
ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV SPRING_REDIS_HOST=${SPRING_REDIS_HOST}
ENV SPRING_REDIS_PORT=${SPRING_REDIS_PORT}
ENV SPRING_REDIS_PASSWORD=${SPRING_REDIS_PASSWORD}
ENV PORT=${PORT}

# Healthcheck for Railway or Docker platform
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the Spring Boot application in the foreground
ENTRYPOINT ["java", "-jar", "app.jar"]
