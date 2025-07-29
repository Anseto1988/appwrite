# SnackTrack - Dog Nutrition Tracking App

[![Android CI](https://github.com/Anseto1988/appwrite/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Anseto1988/appwrite/actions/workflows/android-ci.yml)
[![Release Build](https://github.com/Anseto1988/appwrite/actions/workflows/release.yml/badge.svg)](https://github.com/Anseto1988/appwrite/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/Anseto1988/appwrite/branch/master/graph/badge.svg)](https://codecov.io/gh/Anseto1988/appwrite)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

SnackTrack is a comprehensive Android application for tracking and managing your dog's nutrition, health, and wellness. Built with Kotlin and Jetpack Compose, it provides an intuitive interface for pet owners to monitor their dog's dietary needs and health metrics.

## Features

### Core Features
- **Dog Profile Management**: Create and manage multiple dog profiles
- **Nutrition Tracking**: Track daily food intake and nutritional values
- **Barcode Scanning**: Scan pet food barcodes for automatic nutrition data
- **Health Monitoring**: Track weight, medications, and health entries
- **Team Collaboration**: Share dog profiles with family members or caretakers

### Advanced Features
- **AI Recommendations**: Get personalized feeding recommendations
- **Community Features**: Share experiences and tips with other dog owners
- **Statistical Analysis**: View detailed nutrition and health statistics
- **Export Functionality**: Export data for veterinary visits
- **Offline Support**: Core features work without internet connection

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Appwrite
- **Database**: Appwrite Database
- **Authentication**: Appwrite Auth with Google OAuth support
- **Image Loading**: Coil
- **Networking**: Retrofit with OkHttp
- **Dependency Injection**: Manual injection (considering Hilt for future)
- **Testing**: JUnit, MockK, Espresso

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK with minimum API 26
- Git

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Anseto1988/appwrite.git
   cd snacktrack
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Configure Environment Variables**
   - Copy `env.template` to `.env`:
     ```bash
     cp env.template .env
     ```
   - Edit `.env` file and add your Appwrite credentials:
     ```
     APPWRITE_ENDPOINT=https://your-appwrite-instance.com/v1
     APPWRITE_PROJECT_ID=your_project_id_here
     APPWRITE_DATABASE_ID=your_database_id_here
     APPWRITE_API_KEY=your_api_key_here
     ```
   
   **Important Security Notes:**
   - Never commit the `.env` file to version control
   - Keep your API keys secure and rotate them regularly
   - Use different API keys for development and production
   - The `.env` file is automatically excluded in `.gitignore`

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ```

6. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
     ```bash
     ./gradlew installDebug
     ```

## Project Structure

```
snacktrack/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/snacktrack/
│   │   │   │   ├── data/          # Data layer (models, repositories, services)
│   │   │   │   ├── ui/            # UI layer (screens, components, theme)
│   │   │   │   ├── utils/         # Utility classes
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── SnackTrackApplication.kt
│   │   │   └── res/              # Resources
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumented tests
│   └── build.gradle.kts
├── functions/                    # Appwrite functions
├── scripts/                      # Setup and utility scripts
└── docs/                        # Documentation
```

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:
- Code style and standards
- How to submit pull requests
- Issue reporting guidelines
- Development workflow

## Documentation

- [CI/CD Guide](docs/CI-CD.md)
- [Branch Protection Rules](.github/BRANCH_PROTECTION_RULES.md)
- [Contributing Guide](CONTRIBUTING.md)
- [API Documentation](docs/api/README.md) (coming soon)
- [Architecture Guide](docs/architecture/README.md) (coming soon)

## Testing

The project includes comprehensive test coverage:

- **Unit Tests**: Test business logic and ViewModels
- **Integration Tests**: Test repository and API interactions
- **UI Tests**: Test user interface and navigation

Run all tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Building for Release

1. Update version in `app/build.gradle.kts`
2. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```
3. Sign the APK with your release key
4. Test on multiple devices
5. Upload to Google Play Store

## OAuth Configuration

If your app uses OAuth authentication (e.g., Google Sign-In), you'll need to configure the callback URL:

1. **Update AndroidManifest.xml**
   - The callback scheme is currently set to `appwrite-callback-snackrack2`
   - If using a different project ID, update the scheme in `AndroidManifest.xml`:
     ```xml
     <data android:scheme="appwrite-callback-YOUR_PROJECT_ID" />
     ```

2. **Configure OAuth Provider**
   - Add these redirect URIs in your OAuth provider console:
     - `https://your-appwrite-endpoint/v1/account/sessions/oauth2/callback/google/YOUR_PROJECT_ID`
     - `appwrite-callback-YOUR_PROJECT_ID://auth`

## Troubleshooting

### Common Issues

1. **Gradle sync fails**
   - Check `gradle.properties` for correct JDK path
   - Ensure you have JDK 11 or higher

2. **Appwrite connection errors**
   - Verify endpoint URL in `.env` file
   - Check API key configuration in `.env` file
   - Ensure network permissions in AndroidManifest.xml
   - Verify all environment variables are properly set

3. **Build errors**
   - Clean and rebuild: `./gradlew clean build`
   - Invalidate caches in Android Studio
   - Ensure `.env` file exists with all required variables

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to all contributors
- Powered by [Appwrite](https://appwrite.io)
- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)

## Contact

- **Repository**: https://github.com/Anseto1988/appwrite
- **Issues**: https://github.com/Anseto1988/appwrite/issues
- **Email**: [contact email]

## Status

🚧 **Under Active Development** 🚧

This project is currently in active development. Features may change, and some functionality might be incomplete.