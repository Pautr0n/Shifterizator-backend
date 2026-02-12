## Multi-stage Dockerfile for Shifterizator backend
##
## Stage 1: Build the application JAR using Maven
## Stage 2: Run the JAR on a lightweight Java 21 runtime image

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven descriptor first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (will be cached unless pom.xml changes)
RUN mvn -q -B dependency:go-offline

# Now copy the source code
COPY src ./src

# Build the application (skip tests for faster image builds)
RUN mvn -q -B package -DskipTests


# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from the build stage
# Use a wildcard so it still works if the version changes
COPY --from=build /app/target/*.jar app.jar

# Default environment for the container
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS=""

# Expose the internal port (external port is configured via docker-compose)
EXPOSE 8080

# Use JAVA_OPTS for tuning (heap, GC, etc.) if needed
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

