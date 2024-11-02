# Start with a base image containing Java runtime
FROM openjdk:11-jre-slim

# Add Maintainer Info
LABEL maintainer="support@prtsoftwaresolutions.com"

# The application's jar file
ARG JAR_FILE=target/skilltecheraloginregistration.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

# Expose the port that the application runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app.jar"]
