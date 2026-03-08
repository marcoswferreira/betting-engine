FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR from the target directory
# Assumes 'mvn clean package -DskipTests' was run in the module directory or root
COPY target/core-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
