#!/bin/bash

# Check if JDK 17 is installed
if ! [ -x "$(command -v java)" ] || ! [ "$(java -version 2>&1 | awk -F'[\"\"]' '/version/ {print $2}' | cut -d'.' -f1)" -lt "17" ]; then
  echo "JDK 17 is not installed, downloading and installing..."
  curl -s "https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz" -o "zulu17.tar.gz"
  tar -xf "zulu17.tar.gz"
  rm "zulu17.tar.gz"
  export PATH="$PWD/zulu17.28.13-ca-jdk17.0.0-macosx_x64/bin:$PATH"
fi

# Build Gradle project
./gradlew bootWarAll

# Start Docker Compose
docker-compose up --build -d