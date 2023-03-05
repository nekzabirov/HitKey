FROM gradle:latest AS build

# Copy the Gradle project to the Docker image
COPY . /home/gradle/project

# Set the working directory
WORKDIR /home/gradle/project

# Build the project with Gradle
RUN gradle bootWarAll --no-daemon

# Use the official Tomcat image as the base image for the runtime image
FROM tomcat:latest

# Copy the built WAR file to the Tomcat webapps directory
COPY --from=build /home/gradle/project/service/server/build/libs/*.war /usr/local/tomcat/webapps/my-app.war

# Expose the default Tomcat port
EXPOSE 8080

# Set the command to run when the container starts
CMD ["catalina.sh", "run"]