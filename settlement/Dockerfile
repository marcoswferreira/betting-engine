FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR from the target directory
COPY target/settlement-*.jar app.jar

# Standardizing to 8082 for settlement service
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
