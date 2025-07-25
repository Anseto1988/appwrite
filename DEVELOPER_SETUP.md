# Developer Setup Guide for SnackTrack

This comprehensive guide will help you set up the complete development environment for SnackTrack, including Android development tools, backend configuration, and all necessary dependencies.

## 📋 Table of Contents

1. [System Requirements](#system-requirements)
2. [Android Development Setup](#android-development-setup)
3. [Project Setup](#project-setup)
4. [Backend Configuration](#backend-configuration)
5. [Development Workflow](#development-workflow)
6. [Testing Environment](#testing-environment)
7. [Debugging Tips](#debugging-tips)
8. [CI/CD Setup](#cicd-setup)

## 🖥️ System Requirements

### Minimum Requirements
- **OS**: Windows 10/11, macOS 10.14+, or Ubuntu 18.04+
- **RAM**: 8GB (16GB recommended)
- **Storage**: 10GB free space
- **CPU**: Intel i5 or equivalent

### Software Requirements
- **Java Development Kit (JDK)**: Version 17
- **Android Studio**: 2025.1.1 or newer
- **Git**: Latest version
- **Node.js**: 16.0+ (for cloud functions)

## 🤖 Android Development Setup

### Step 1: Install Android Studio

#### Linux (Ubuntu/Debian)
```bash
# Using snap (recommended)
sudo snap install android-studio --classic

# Or download from official website
wget https://developer.android.com/studio
```

#### Windows
1. Download Android Studio from https://developer.android.com/studio
2. Run the installer
3. Follow the setup wizard

#### macOS
1. Download Android Studio from https://developer.android.com/studio
2. Open the DMG file
3. Drag Android Studio to Applications folder

### Step 2: Configure Android Studio

1. **First Launch Setup**
   - Choose "Standard" installation type
   - Select UI theme (Light/Dark)
   - Verify settings and click "Finish"
   - Android Studio will download required components

2. **SDK Configuration**
   - Open SDK Manager: Tools → SDK Manager
   - Install the following:
     - Android SDK Platform 35
     - Android SDK Build-Tools 34.0.0
     - Android SDK Platform-Tools
     - Android SDK Command-line Tools
     - Android Emulator
     - Google Play services

3. **Create AVD (Android Virtual Device)**
   - Open AVD Manager: Tools → AVD Manager
   - Click "Create Virtual Device"
   - Select device: Pixel 6 (recommended)
   - System Image: Android 14.0 (API 34) with Google Play
   - Finish configuration

## 🚀 Project Setup

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone https://github.com/Anseto1988/appwrite.git
cd snacktrack

# Verify you're on the correct branch
git branch
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select "Open"
3. Navigate to the cloned `snacktrack` directory
4. Click "OK"
5. Wait for initial project sync

### Step 3: Gradle Configuration

The project uses Gradle with Kotlin DSL. Key configuration files:

1. **`gradle.properties`** - Already configured with:
   ```properties
   org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
   org.gradle.caching=true
   org.gradle.parallel=true
   android.useAndroidX=true
   ```

2. **`local.properties`** - Auto-generated, contains:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

### Step 4: Dependency Resolution

```bash
# Clean and rebuild to ensure all dependencies are resolved
./gradlew clean
./gradlew build

# If you encounter issues, try:
./gradlew build --refresh-dependencies
```

## 🔧 Backend Configuration

### Appwrite Setup

The app uses Appwrite as the backend. Current configuration:

1. **Endpoint**: `https://parse.nordburglarp.de/v2`
2. **Project ID**: `snackrack2`
3. **Database ID**: `snacktrack-db`

### Required Collections

Ensure these collections exist in your Appwrite project:

```javascript
// Core Collections
- dogs
- users
- foods
- foodIntakes
- weights
- teams
- dogMedications
- dogAllergies

// Community Features
- communityPosts
- communityComments
- communityProfiles

// Administrative
- foodSubmissions
- crawlState

// AI/ML Features
- nutritionAnalysis
- aiRecommendations
```

### Authentication Setup

1. **Enable Auth Methods**:
   - Email/Password
   - Google OAuth 2.0

2. **OAuth Configuration**:
   - Redirect URL: `appwrite-callback-snackrack2://`
   - Update in Appwrite Console under Auth settings

### Environment Variables

Create a `.env` file in the project root (for local development):

```env
APPWRITE_ENDPOINT=https://parse.nordburglarp.de/v2
APPWRITE_PROJECT_ID=snackrack2
APPWRITE_API_KEY=your-api-key-here
```

## 💻 Development Workflow

### Code Organization

```
src/main/java/com/example/snacktrack/
├── data/
│   ├── model/          # Data classes
│   ├── repository/     # Data access layer
│   └── service/        # Network services
├── ui/
│   ├── components/     # Reusable UI components
│   ├── screens/        # Feature screens
│   ├── theme/          # Material3 theming
│   └── viewmodel/      # Business logic
└── utils/              # Helper functions
```

### Adding a New Feature

1. **Create the Data Model**
   ```kotlin
   // data/model/NewFeature.kt
   data class NewFeature(
       val id: String,
       val name: String,
       val createdAt: String
   )
   ```

2. **Implement Repository**
   ```kotlin
   // data/repository/NewFeatureRepository.kt
   class NewFeatureRepository(
       private val appwriteService: AppwriteService
   ) : BaseRepository() {
       // Implementation
   }
   ```

3. **Create ViewModel**
   ```kotlin
   // ui/viewmodel/NewFeatureViewModel.kt
   class NewFeatureViewModel : ViewModel() {
       // State management
   }
   ```

4. **Build UI Screen**
   ```kotlin
   // ui/screens/NewFeatureScreen.kt
   @Composable
   fun NewFeatureScreen(
       navController: NavController
   ) {
       // UI implementation
   }
   ```

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "feat: Add your feature description"

# Push to remote
git push origin feature/your-feature-name

# Create Pull Request on GitHub
```

### Code Style Guidelines

1. **Kotlin Conventions**:
   - Use `camelCase` for functions and variables
   - Use `PascalCase` for classes and interfaces
   - Prefer `val` over `var`
   - Use meaningful names

2. **Compose Guidelines**:
   - Keep composables small and focused
   - Use `remember` for expensive computations
   - Follow Material3 design principles

## 🧪 Testing Environment

### Unit Testing

```bash
# Run all unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTestCoverage

# Run specific test
./gradlew test --tests "*.DogViewModelTest"
```

### Integration Testing

```bash
# Run instrumented tests
./gradlew connectedAndroidTest

# Run on specific device
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.snacktrack.RepositoryTest
```

### Test Configuration

Tests use the following setup:
- **Mockito**: For mocking dependencies
- **Robolectric**: For Android framework testing
- **Coroutines Test**: For testing suspend functions

Example test setup:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class ExampleTest {
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Test
    fun `test example`() = runTest {
        // Test implementation
    }
}
```

## 🐛 Debugging Tips

### Common Issues and Solutions

1. **Gradle Sync Issues**
   ```bash
   # Clear Gradle cache
   ./gradlew clean
   rm -rf ~/.gradle/caches/
   
   # Rebuild
   ./gradlew build --refresh-dependencies
   ```

2. **Appwrite Connection Failed**
   - Check network configuration in `AndroidManifest.xml`
   - Verify SSL certificates
   - Test with curl: `curl https://parse.nordburglarp.de/v2/health`

3. **Build Errors**
   ```bash
   # Invalidate caches
   rm -rf .gradle/
   rm -rf app/build/
   ./gradlew clean build
   ```

### Debugging Tools

1. **Android Studio Debugger**
   - Set breakpoints by clicking line numbers
   - Use "Debug" run configuration
   - Inspect variables in Debug window

2. **Layout Inspector**
   - Tools → Layout Inspector
   - Inspect Compose UI hierarchy
   - View recomposition counts

3. **Network Profiler**
   - View → Tool Windows → Profiler
   - Monitor network requests
   - Check response times and payloads

### Logging

```kotlin
// Use SecureLogger for sensitive data
SecureLogger.d("DogViewModel", "Loading dog: $dogId")

// Regular logging
Log.d("SnackTrack", "Debug message")
```

## 🚢 CI/CD Setup

### GitHub Actions Configuration

Create `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### Pre-commit Hooks

Install pre-commit hooks for code quality:

```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
./gradlew ktlintCheck
EOF

chmod +x .git/hooks/pre-commit
```

## 📚 Additional Resources

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose/documentation)
- [Appwrite Documentation](https://appwrite.io/docs)
- [Kotlin Language Guide](https://kotlinlang.org/docs/home.html)

### Tools
- [Android Debug Bridge (ADB)](https://developer.android.com/studio/command-line/adb)
- [Flipper](https://fbflipper.com/) - Mobile app debugging platform
- [Stetho](http://facebook.github.io/stetho/) - Debug bridge for Android

### Community
- Project Issues: https://github.com/Anseto1988/appwrite/issues
- Android Developers Community: https://developer.android.com/community
- Kotlin Slack: https://kotlinlang.slack.com

## 🔐 Security Notes

1. **Never commit sensitive data**:
   - API keys
   - Passwords
   - Personal information

2. **Use ProGuard/R8 for release builds**:
   - Already configured in `build.gradle.kts`
   - Obfuscates code and removes unused resources

3. **Network Security**:
   - SSL/TLS enforced via network security config
   - Certificate pinning recommended for production

---

**Last Updated**: January 2025

For questions or issues, please refer to the project's GitHub repository or contact the development team.