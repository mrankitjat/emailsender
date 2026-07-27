# Stage 1: Build JAR using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and package executable JAR
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from builder stage
COPY --from=builder /app/target/recruitment-email-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables defaults
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
