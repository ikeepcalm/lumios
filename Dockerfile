# Stage 1: Build with GraalVM
FROM ghcr.io/graalvm/native-image-community:21 AS build
WORKDIR /app

# Install required packages
RUN microdnf install -y findutils

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# Copy source code
COPY src/ src/

# Make gradlew executable
RUN chmod +x gradlew

# Build native image
RUN ./gradlew nativeCompile && \
    echo "=== Native compile output ===" && \
    ls -la /app/build/native/nativeCompile/

# Stage 2: Runtime dependencies - Get zlib library
FROM debian:12-slim AS runtime-deps
RUN apt-get update && apt-get install -y \
    zlib1g \
    && rm -rf /var/lib/apt/lists/*

# Stage 3: Runtime - Use distroless for minimal attack surface
FROM gcr.io/distroless/base-debian12:nonroot
WORKDIR /app

# Copy required shared libraries
COPY --from=runtime-deps /usr/lib/x86_64-linux-gnu/libz.so.1* /usr/lib/x86_64-linux-gnu/

# Copy the native executable
COPY --from=build /app/build/native/nativeCompile/lumios /app/lumios

# Expose the port (from application.properties)
EXPOSE 8847

# Set the entry point - use the copied executable
ENTRYPOINT ["/app/lumios"]