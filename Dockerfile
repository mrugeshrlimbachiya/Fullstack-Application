#############################################
# BUILD STAGE
#############################################
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# Copy Gradle project files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Pre-download dependencies (for faster incremental builds)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the source code
COPY src ./src

# Build Spring Boot fat jar
RUN ./gradlew clean bootJar --no-daemon

#############################################
# RUNTIME STAGE
#############################################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
