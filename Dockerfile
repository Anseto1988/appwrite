# Dockerfile for Android build environment
FROM openjdk:17-jdk-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Create android sdk directory
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools

# Download Android command line tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# Accept licenses
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses

# Install Android SDK components
RUN ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository" \
    "extras;google;google_play_services"

# Create workspace
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle* /app/
COPY gradle /app/gradle

# Download gradle wrapper
RUN ./gradlew --version || true

# Copy the rest of the project
COPY . /app

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the project
CMD ["./gradlew", "build"]