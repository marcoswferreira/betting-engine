FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR from the target directory
COPY target/odds-ingestion-*.jar app.jar

# Standardizing to 8081 for odds-ingestion
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
