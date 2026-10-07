# ==========================================
# Multi-Stage Dockerfile for Spring Boot App
# ==========================================

# Stage 1: Build stage with Maven and JDK 21/25
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and resolve dependencies first (layer caching)
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy backend source code and build package
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime image with JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S telco && adduser -S telco -G telco
USER telco

# Copy compiled jar from build stage
COPY --from=build /app/target/telecom-network-monitor-1.0.0.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Configure JVM flags optimized for container memory
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar app.jar"]
