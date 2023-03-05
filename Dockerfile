# Use the official Gradle image as the base image
FROM gradle:latest AS build

# Copy the Gradle project to the Docker image
COPY . /home/gradle/project

# Set the working directory
WORKDIR /home/gradle/project

# Build the project with Gradle
RUN gradle service:file:bootWar

# Use the official OpenJDK image as the base image for the runtime image
FROM openjdk:latest

# Copy the built JAR file to the Docker image
COPY --from=build /home/gradle/project/service/file/build/libs/*.war /app.war

# Set the command to run when the container starts
CMD ["java", "-jar", "/app.war"]
