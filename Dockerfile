# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy and resolve dependencies first for caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source files and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose the port for the app
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

# Run the application with environment variables loaded at runtime
USER appuser
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]