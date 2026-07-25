# Multi-stage Dockerfile for AruClinic Healthcare System (Spring Boot + Vaadin + Java 21)
# Production build timestamp: 2026-07-25

# Stage 1: Build Application using Official Maven + Java 21 Image
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy project repository
COPY . .

# Build production JAR with Vaadin production mode enabled
RUN mvn clean package -DskipTests -Dvaadin.productionMode=true

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR artifact from build stage
COPY --from=build /workspace/target/*.jar app.jar

# Expose standard application port
EXPOSE 8080

# Environment defaults for containerized runtime
ENV PORT=8080 \
    VAADIN_PRODUCTION_MODE=true

# Start Spring Boot application
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
