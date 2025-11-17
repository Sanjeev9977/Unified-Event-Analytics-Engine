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

# Set working directory
WORKDIR /app

# Copy the built jar from the builder stage (using wildcard)
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the Spring Boot application in the foreground
ENTRYPOINT ["java", "-jar", "app.jar"]
