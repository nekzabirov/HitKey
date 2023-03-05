FROM gradle:latest AS build

# Copy the Gradle project to the Docker image
COPY . /home/gradle/project

# Set the working directory
WORKDIR /home/gradle/project

# Build the project with Gradle
RUN gradle bootWarAll --no-daemon