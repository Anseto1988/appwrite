# Environment Configuration Setup Guide

This guide explains how to properly configure environment variables for the SnackTrack application.

## Overview

SnackTrack uses environment variables to manage sensitive configuration data like API keys and endpoints. This approach ensures that sensitive information is never committed to version control.

## Setup Steps

### 1. Create Your Environment File

Copy the template file to create your local environment configuration:

```bash
cp env.template .env
```

Or if you prefer the example file:

```bash
cp .env.example .env
```

### 2. Configure Environment Variables

Edit the `.env` file and add your actual values:

```bash
# Required Variables
APPWRITE_ENDPOINT=https://your-appwrite-instance.com/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_DATABASE_ID=your_database_id
APPWRITE_API_KEY=your_api_key

# Optional Variables (uncomment as needed)
# APP_ENV=development
# DEBUG_MODE=true
# API_TIMEOUT=30000
```

### 3. How It Works

The build process reads these environment variables in the following order of precedence:

1. `.env` file in the project root
2. System environment variables
3. Default values (if specified)

The values are then compiled into the `BuildConfig` class and can be accessed in the app code:

```kotlin
// In your Kotlin code
val endpoint = BuildConfig.APPWRITE_ENDPOINT
val projectId = BuildConfig.APPWRITE_PROJECT_ID
val apiKey = BuildConfig.APPWRITE_API_KEY
```

## Security Best Practices

### Never Commit Sensitive Data

- The `.env` file is excluded in `.gitignore`
- Never commit actual API keys or secrets
- Use different API keys for development and production

### Rotate Keys Regularly

- Change API keys periodically
- Revoke unused keys
- Monitor API key usage in Appwrite console

### Use Minimal Permissions

- Create API keys with only the permissions your app needs
- Use separate keys for different environments
- Consider using scoped keys for specific features

## Troubleshooting

### Environment Variables Not Loading

1. Ensure `.env` file exists in the project root
2. Check file permissions (should be readable)
3. Verify variable names match exactly
4. Clean and rebuild the project:
   ```bash
   ./gradlew clean build
   ```

### Build Errors Related to BuildConfig

1. Ensure `buildFeatures { buildConfig = true }` is in your `build.gradle.kts`
2. Sync project with Gradle files
3. Invalidate caches and restart Android Studio

### Runtime Configuration Issues

If the app can't connect to Appwrite:

1. Verify the endpoint URL format (should include `/v1`)
2. Check API key has proper permissions
3. Ensure network permissions in AndroidManifest.xml
4. Test the configuration using Appwrite console

## CI/CD Configuration

For continuous integration:

1. Set environment variables in your CI/CD platform
2. Do not use `.env` files in CI/CD
3. Use secure variable storage features of your CI/CD platform
4. Example for GitHub Actions:
   ```yaml
   env:
     APPWRITE_ENDPOINT: ${{ secrets.APPWRITE_ENDPOINT }}
     APPWRITE_PROJECT_ID: ${{ secrets.APPWRITE_PROJECT_ID }}
     APPWRITE_DATABASE_ID: ${{ secrets.APPWRITE_DATABASE_ID }}
     APPWRITE_API_KEY: ${{ secrets.APPWRITE_API_KEY }}
   ```

## Development vs Production

### Development Environment

- Use a separate Appwrite project for development
- Enable debug mode for better error messages
- Use relaxed security rules for easier testing

### Production Environment

- Use production Appwrite instance
- Disable debug mode
- Implement strict security rules
- Monitor API usage and errors

## Additional Resources

- [Appwrite Documentation](https://appwrite.io/docs)
- [Android BuildConfig Documentation](https://developer.android.com/studio/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code)
- [Environment Variables Best Practices](https://12factor.net/config)