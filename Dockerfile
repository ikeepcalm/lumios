# Stage 1: Build
FROM gradle:8.12.0-jdk21 AS build
WORKDIR /app

# Copy project files to the container
COPY . .

# Build the project
RUN gradle build

# Stage 2: Deploy
FROM openjdk:21-jdk
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8080

# Set the entry point
ENTRYPOINT ["java","-jar","app.jar"]