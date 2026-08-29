# ==============================================================================
# Multi-stage Dockerfile for Flutter Mobile & Web (OCP AT Mobile)
# Stages:
#   1. flutter-base    : Flutter SDK & Dart environment
#   2. web-builder     : Compiles Flutter Web SPA
#   3. serve           : Lightweight Nginx image hosting Flutter Web (default runtime)
#   4. android-builder : Android SDK + Gradle + Flutter -> Builds Release APK
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Flutter Base SDK
# ------------------------------------------------------------------------------
FROM debian:bookworm-slim AS flutter-base

ENV DEBIAN_FRONTEND=noninteractive
ENV FLUTTER_HOME=/opt/flutter
ENV PATH="${FLUTTER_HOME}/bin:${FLUTTER_HOME}/bin/cache/dart-sdk/bin:${PATH}"

# Install base dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    git \
    unzip \
    xz-utils \
    zip \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install Flutter SDK (Stable channel) — version alignée sur le poste de dev
ARG FLUTTER_VERSION=3.47.2
RUN git clone --depth 1 --branch ${FLUTTER_VERSION} https://github.com/flutter/flutter.git ${FLUTTER_HOME} \
    && flutter config --no-analytics \
    && flutter precache --web

WORKDIR /app

# Copy dependency specs first for optimal layer caching
COPY pubspec.yaml pubspec.lock* ./
RUN flutter pub get

# Copy all source files
COPY . .

# ------------------------------------------------------------------------------
# Stage 2: Flutter Web Builder
# ------------------------------------------------------------------------------
FROM flutter-base AS web-builder

ARG ENV=prod
ARG API_BASE_URL=""

RUN if [ -n "$API_BASE_URL" ]; then \
      flutter build web --release --dart-define=ENV=${ENV} --dart-define=API_BASE_URL=${API_BASE_URL}; \
    else \
      flutter build web --release --dart-define=ENV=${ENV}; \
    fi

# ------------------------------------------------------------------------------
# Stage 3: Serve Flutter Web with Nginx (Default target)
# ------------------------------------------------------------------------------
FROM nginx:1.25-alpine AS serve

COPY --from=web-builder /app/build/web /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/health || exit 1

CMD ["nginx", "-g", "daemon off;"]

# ------------------------------------------------------------------------------
# Stage 4: Android APK Builder (for CI/CD & local containerized APK export)
# ------------------------------------------------------------------------------
FROM flutter-base AS android-builder

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

# Install OpenJDK 17
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

# Install Android Command Line Tools & SDK components
ARG ANDROID_TOOLS_VERSION=11076708
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && curl -fsSL -o /tmp/cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_TOOLS_VERSION}_latest.zip" \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# Accept licenses and install platform packages
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

RUN flutter precache --android

ARG ENV=prod
ARG API_BASE_URL=""

# Build release APK
RUN if [ -n "$API_BASE_URL" ]; then \
      flutter build apk --release --dart-define=ENV=${ENV} --dart-define=API_BASE_URL=${API_BASE_URL}; \
    else \
      flutter build apk --release --dart-define=ENV=${ENV}; \
    fi

# Default command copies the generated APK to /output mount
VOLUME /output
CMD ["sh", "-c", "cp /app/build/app/outputs/flutter-apk/*.apk /output/ && echo 'APK exported successfully to /output'"]
