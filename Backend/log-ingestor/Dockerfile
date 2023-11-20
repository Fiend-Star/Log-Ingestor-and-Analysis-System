# Stage 1: Build with Maven
FROM debian:latest as builder

# Install Java and Maven
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven

# Install curl, unzip, and Protoc
RUN apt-get install -y curl unzip && \
    curl -OL https://github.com/protocolbuffers/protobuf/releases/download/v3.25.1/protoc-3.25.1-linux-x86_64.zip && \
    unzip protoc-3.25.1-linux-x86_64.zip -d /usr/local && \
    rm protoc-3.25.1-linux-x86_64.zip || true

# Install libprotobuf-dev and protobuf-compiler
RUN apt-get install -y libprotobuf-dev protobuf-compiler

# Set environment variable for Protoc
ENV PATH="/usr/local/bin:$PATH"

# Copy your project's source code into the Docker image
COPY . /usr/src/myapp

# List the contents of the /usr/src/myapp directory to verify the copy
RUN ls -lrt /usr/src/myapp

# Set the working directory
WORKDIR /usr/src/myapp

# Run Maven install to build your application
RUN mvn clean install

# Stage 2: Setup the runtime environment
FROM openjdk:17

# Copy the JAR file from the builder stage
COPY --from=builder /usr/src/myapp/target/log-ingestor-0.0.1-SNAPSHOT.jar /usr/app/log-ingestor.jar

# Set the working directory
WORKDIR /usr/app

# Set the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "log-ingestor.jar"]
