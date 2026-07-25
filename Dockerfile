# Multi-stage Dockerfile for AruClinic Healthcare System (Spring Boot + Vaadin + Java 21)

# Stage 1: Build Application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy configuration files
COPY pom.xml .
COPY package.json .
COPY tsconfig.json .
COPY vite.config.ts .
COPY src ./src
COPY frontend ./frontend

# Install Maven dependencies and build production JAR
RUN apk add --no-gradable --no-cache maven \
    && mvn clean package -DskipTests -Dvaadin.productionMode=true

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
