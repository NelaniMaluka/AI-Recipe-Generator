# ===== Build Stage: Maven + Temurin JDK 17 =====
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application (skip tests)
ARG JAR_FILE=target/recipe-search-backend-0.0.1-SNAPSHOT.jar
RUN mvn package -DskipTests

# ===== Runtime Stage: Temurin JRE 17 (lighter than full JDK) =====
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user for security
RUN useradd -ms /bin/bash appuser
USER appuser

# Set environment variables for Spring profile and Java options
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Copy the JAR from the build stage
COPY --from=build /app/$JAR_FILE /app/app.jar

# Expose the port
EXPOSE 8080

# Healthcheck for Docker orchestration
HEALTHCHECK --interval=30s --timeout=65s --start-period=300s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entry point using environment variable for JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
