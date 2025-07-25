# SnackTrack Build Process Documentation

This document provides detailed information about building, testing, and deploying the SnackTrack Android application.

## 📋 Table of Contents

1. [Build Requirements](#build-requirements)
2. [Build Configuration](#build-configuration)
3. [Build Types](#build-types)
4. [Building the App](#building-the-app)
5. [Testing](#testing)
6. [Code Quality](#code-quality)
7. [Release Process](#release-process)
8. [Troubleshooting](#troubleshooting)
9. [CI/CD Pipeline](#cicd-pipeline)

## Build Requirements

### System Requirements
- **JDK**: Version 17 (bundled with Android Studio)
- **Android SDK**: API Level 35
- **Build Tools**: Version 34.0.0 or higher
- **Gradle**: Version 8.x (uses Gradle Wrapper)
- **Memory**: Minimum 4GB allocated to Gradle

### SDK Components
```
Android SDK Platform 35
Android SDK Build-Tools 34.0.0
Android SDK Platform-Tools
Android SDK Command-line Tools
Android Emulator
Google Play services
```

## Build Configuration

### Gradle Configuration Files

1. **`settings.gradle.kts`** - Project settings
   ```kotlin
   rootProject.name = "snacktrack"
   include(":app")
   ```

2. **`build.gradle.kts`** (Project level)
   ```kotlin
   plugins {
       alias(libs.plugins.android.application) apply false
       alias(libs.plugins.kotlin.android) apply false
       alias(libs.plugins.kotlin.compose) apply false
   }
   ```

3. **`app/build.gradle.kts`** - App module configuration
   - Defines SDK versions
   - Dependencies
   - Build types
   - ProGuard rules

### Version Catalog

The project uses Gradle's version catalog (`gradle/libs.versions.toml`) for dependency management:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
compose-bom = "2024.12.01"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
# ... other dependencies
```

### Gradle Properties

Key settings in `gradle.properties`:

```properties
# Memory allocation
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m

# Performance optimizations
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true

# Android settings
android.useAndroidX=true
android.nonTransitiveRClass=true

# JDK path (platform-specific)
org.gradle.java.home=/path/to/jdk-17
```

## Build Types

### Debug Build

Optimized for development and testing:
- Debugging enabled
- No code obfuscation
- No resource shrinking
- Test coverage enabled
- Faster build times

```kotlin
buildTypes {
    getByName("debug") {
        isMinifyEnabled = false
        isDebuggable = true
        enableUnitTestCoverage = true
        enableAndroidTestCoverage = true
    }
}
```

### Release Build

Optimized for production:
- ProGuard/R8 enabled
- Resource shrinking enabled
- Code optimization
- Smaller APK size
- Signing required

```kotlin
buildTypes {
    getByName("release") {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

## Building the App

### Command Line Builds

1. **Clean Build**
   ```bash
   ./gradlew clean
   ```

2. **Debug Build**
   ```bash
   ./gradlew assembleDebug
   # Output: app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Release Build**
   ```bash
   ./gradlew assembleRelease
   # Output: app/build/outputs/apk/release/app-release.apk
   ```

4. **Build All Variants**
   ```bash
   ./gradlew assemble
   ```

5. **Install Debug Build**
   ```bash
   ./gradlew installDebug
   ```

### Android Studio Builds

1. **Build Menu**
   - Build → Make Project (Ctrl+F9)
   - Build → Rebuild Project
   - Build → Clean Project

2. **Build Variants**
   - View → Tool Windows → Build Variants
   - Select debug or release

3. **Generate APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

### Build Output Structure

```
app/build/
├── outputs/
│   ├── apk/
│   │   ├── debug/
│   │   │   ├── app-debug.apk
│   │   │   └── output-metadata.json
│   │   └── release/
│   │       ├── app-release.apk
│   │       └── output-metadata.json
│   ├── logs/
│   └── mapping/          # ProGuard mappings
├── intermediates/        # Temporary build files
├── generated/           # Generated source files
└── tmp/                # Temporary files
```

## Testing

### Unit Tests

1. **Run All Unit Tests**
   ```bash
   ./gradlew test
   ```

2. **Run Specific Test Class**
   ```bash
   ./gradlew test --tests "com.example.snacktrack.DogViewModelTest"
   ```

3. **Run with Coverage**
   ```bash
   ./gradlew testDebugUnitTestCoverage
   # Report: app/build/reports/coverage/test/debug/index.html
   ```

### Instrumented Tests

1. **Run on Connected Device**
   ```bash
   ./gradlew connectedAndroidTest
   ```

2. **Run Specific Test**
   ```bash
   ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.snacktrack.ExampleInstrumentedTest
   ```

### Test Reports

Test reports are generated at:
- Unit tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- Coverage: `app/build/reports/coverage/test/debug/index.html`
- Instrumented: `app/build/reports/androidTests/connected/index.html`

## Code Quality

### Lint Checks

1. **Run Lint**
   ```bash
   ./gradlew lint
   # Report: app/build/reports/lint-results-debug.html
   ```

2. **Fix Lint Warnings**
   ```bash
   ./gradlew lintFix
   ```

### Code Formatting

1. **Check Kotlin Code Style**
   ```bash
   ./gradlew ktlintCheck
   ```

2. **Format Kotlin Code**
   ```bash
   ./gradlew ktlintFormat
   ```

### Static Analysis

1. **Detekt (if configured)**
   ```bash
   ./gradlew detekt
   ```

2. **Dependency Updates**
   ```bash
   ./gradlew dependencyUpdates
   ```

## Release Process

### 1. Prepare for Release

1. **Update Version**
   ```kotlin
   // app/build.gradle.kts
   defaultConfig {
       versionCode = 2  // Increment
       versionName = "1.1.0"  // Update
   }
   ```

2. **Update Release Notes**
   - Create `CHANGELOG.md`
   - Document new features
   - List bug fixes

### 2. Create Signing Configuration

1. **Generate Release Key**
   ```bash
   keytool -genkey -v -keystore snacktrack-release.keystore \
     -alias snacktrack -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure Signing**
   ```kotlin
   // app/build.gradle.kts
   signingConfigs {
       create("release") {
           storeFile = file("../snacktrack-release.keystore")
           storePassword = System.getenv("KEYSTORE_PASSWORD")
           keyAlias = "snacktrack"
           keyPassword = System.getenv("KEY_PASSWORD")
       }
   }
   ```

### 3. Build Release APK

```bash
# Set environment variables
export KEYSTORE_PASSWORD=your-keystore-password
export KEY_PASSWORD=your-key-password

# Build release APK
./gradlew assembleRelease
```

### 4. Build App Bundle (AAB)

```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### 5. Test Release Build

1. **Install on Device**
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

2. **Verify ProGuard**
   - Check app functionality
   - Verify crash reporting
   - Test all features

### 6. Upload to Play Store

1. **Play Console**
   - Create new release
   - Upload AAB file
   - Add release notes
   - Submit for review

## Troubleshooting

### Common Build Issues

1. **Gradle Sync Failed**
   ```bash
   # Clear caches
   ./gradlew clean
   rm -rf ~/.gradle/caches/
   ./gradlew build --refresh-dependencies
   ```

2. **Out of Memory**
   ```properties
   # Increase in gradle.properties
   org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=1536m
   ```

3. **Dependency Conflicts**
   ```bash
   # Show dependency tree
   ./gradlew app:dependencies
   
   # Force specific version
   configurations.all {
       resolutionStrategy {
           force("com.squareup.okhttp3:okhttp:4.12.0")
       }
   }
   ```

4. **Build Cache Issues**
   ```bash
   # Clear build cache
   ./gradlew cleanBuildCache
   rm -rf .gradle/
   rm -rf app/build/
   ```

### Android Studio Issues

1. **Invalidate Caches**
   - File → Invalidate Caches and Restart

2. **Sync Project**
   - File → Sync Project with Gradle Files

3. **SDK Issues**
   - File → Project Structure → SDK Location
   - Verify paths are correct

## CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: app/build/reports/tests/

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### Build Optimization Tips

1. **Enable Build Cache**
   ```properties
   org.gradle.caching=true
   org.gradle.configuration-cache=true
   ```

2. **Parallel Execution**
   ```properties
   org.gradle.parallel=true
   org.gradle.workers.max=8
   ```

3. **Incremental Builds**
   - Avoid clean builds when unnecessary
   - Use incremental compilation

4. **Module Structure**
   - Split large modules
   - Use dynamic feature modules

### Performance Monitoring

1. **Build Scan**
   ```bash
   ./gradlew build --scan
   ```

2. **Profile Build**
   ```bash
   ./gradlew build --profile
   # Report: build/reports/profile/
   ```

3. **Analyze APK**
   - Build → Analyze APK
   - Check method count
   - Review resource usage

---

**Version**: 1.0.0  
**Last Updated**: January 2025

For additional help, consult the [Android Developer Documentation](https://developer.android.com/studio/build) or the project's issue tracker.