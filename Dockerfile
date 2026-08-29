FROM eclipse-temurin:17-jre-alpine

# Working directory inside container
WORKDIR /app

# Copy Spring Boot JAR
COPY target/tripItinerary-0.0.1-SNAPSHOT.jar app.jar

# Render listens on port 8080
EXPOSE 8080

# JVM optimization for containers
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]