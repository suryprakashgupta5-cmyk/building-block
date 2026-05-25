# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom first (Docker caches this layer)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build the JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ── Stage 2: Run ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app uses
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]