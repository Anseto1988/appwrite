# CI/CD Pipeline Guide for SnackTrack

## Overview

This guide documents the continuous integration and deployment (CI/CD) pipeline setup for the SnackTrack Android application using GitHub Actions.

## Pipeline Structure

### 1. Android CI (`android-ci.yml`)
**Trigger**: Push to `master` or `develop`, Pull Requests
**Purpose**: Main CI pipeline for continuous integration

#### Jobs:
- **Setup Environment**: Configures JDK 17, Android SDK, and Gradle caching
- **Lint Check**: Runs Android lint to catch potential issues
- **Unit Tests**: Executes all unit tests
- **Build APK**: Creates both debug and release APKs
- **Security Scan**: Runs Trivy and TruffleHog for vulnerability detection
- **Dependency Check**: Scans for vulnerable dependencies
- **Code Quality**: SonarCloud analysis (requires setup)

### 2. Release Pipeline (`release.yml`)
**Trigger**: Git tags matching `v*` pattern or manual workflow dispatch
**Purpose**: Automated release process

#### Features:
- Version validation and semantic versioning
- Signed APK and AAB generation (requires keystore setup)
- Automated changelog generation
- GitHub release creation
- Appwrite function deployment
- Release notifications

### 3. Pull Request Checks (`pr-check.yml`)
**Trigger**: Pull request events
**Purpose**: Automated PR validation and review

#### Checks:
- PR title validation (semantic commits)
- Branch naming convention
- Automated code review (detekt, ktlint)
- APK size impact analysis
- Dependency license review
- PR preview APK generation

### 4. Nightly Builds (`nightly-build.yml`)
**Trigger**: Daily at 2 AM UTC or manual
**Purpose**: Daily integration testing

#### Features:
- Full test suite execution
- Performance benchmarks
- Security scanning
- Artifact retention (7 days)
- Automated cleanup of old builds

### 5. Dependency Updates (`dependency-update.yml`)
**Trigger**: Weekly on Mondays or manual
**Purpose**: Automated dependency management

#### Process:
- Checks for outdated dependencies
- Creates PR with updates
- Runs tests on updated dependencies
- Generates detailed update report

## Required Secrets

Configure these secrets in your GitHub repository settings:

```yaml
# Appwrite Configuration
APPWRITE_ENDPOINT: Your Appwrite endpoint URL
APPWRITE_PROJECT_ID: Your Appwrite project ID
APPWRITE_API_KEY: Your Appwrite API key

# Android Signing (for releases)
ANDROID_KEYSTORE_BASE64: Base64 encoded keystore file
ANDROID_KEYSTORE_PASSWORD: Keystore password
ANDROID_KEY_ALIAS: Key alias name
ANDROID_KEY_PASSWORD: Key password

# Code Quality (optional)
SONAR_TOKEN: SonarCloud authentication token
```

## Setting Up Secrets

### 1. Appwrite Secrets
```bash
# Get these from your Appwrite console
APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
APPWRITE_PROJECT_ID="your-project-id"
APPWRITE_API_KEY="your-api-key"
```

### 2. Android Signing Secrets
```bash
# Generate keystore if you don't have one
keytool -genkey -v -keystore release-keystore.jks \
  -alias snacktrack -keyalg RSA -keysize 2048 -validity 10000

# Convert keystore to base64
base64 -w 0 release-keystore.jks > keystore-base64.txt

# Add the content of keystore-base64.txt as ANDROID_KEYSTORE_BASE64 secret
```

### 3. Add Secrets to GitHub
1. Go to Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add each secret with its corresponding value

## Gradle Caching Strategy

The pipelines use aggressive caching to speed up builds:

```yaml
~/.gradle/caches       # Gradle dependency cache
~/.gradle/wrapper      # Gradle wrapper files
~/.android/build-cache # Android build cache
```

Cache keys are based on:
- OS (Linux)
- Gradle files hash
- Build variant (for specific workflows)

## Artifact Management

### Retention Policy
- **CI Artifacts**: 7 days
- **Release Artifacts**: Permanent (attached to GitHub releases)
- **Nightly Builds**: 7 builds retained
- **PR Preview APKs**: Until PR is closed

### Artifact Types
- Debug APKs
- Release APKs (unsigned and signed)
- App Bundles (AAB)
- Test reports
- Lint results
- Security scan reports

## Security Features

### 1. Secret Scanning
- TruffleHog scans for exposed secrets
- Runs on every push and PR

### 2. Dependency Scanning
- OWASP Dependency Check
- License compliance check
- Vulnerability detection

### 3. Code Security
- Trivy filesystem scanning
- SARIF report upload to GitHub Security tab

## Performance Optimizations

### 1. Parallel Execution
- Lint, tests, and security scans run in parallel
- Reduces overall pipeline time

### 2. Conditional Steps
- Skip expensive operations when not needed
- Smart caching reduces redundant work

### 3. Gradle Optimizations
```bash
GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx4g -Dorg.gradle.daemon=false"
```

## Monitoring and Notifications

### Build Status
- Commit status updates
- PR comments with build results
- Release notifications

### Metrics Available
- Build duration
- Test results
- Code coverage (when configured)
- APK size changes
- Security vulnerabilities

## Troubleshooting

### Common Issues

#### 1. Build Failures
```bash
# Check Gradle compatibility
./gradlew --version

# Clear caches
./gradlew clean
rm -rf ~/.gradle/caches
```

#### 2. Test Failures
```bash
# Run tests locally
./gradlew test --info

# Check test reports
open app/build/reports/tests/testDebugUnitTest/index.html
```

#### 3. Signing Issues
```bash
# Verify keystore
keytool -list -v -keystore release-keystore.jks

# Test signing locally
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=release-keystore.jks \
  -Pandroid.injected.signing.store.password=YOUR_PASSWORD
```

## Best Practices

### 1. Branch Protection
Enable these rules for `master`:
- Require PR reviews
- Require status checks to pass
- Require branches to be up to date
- Include administrators

### 2. Semantic Versioning
Follow semantic versioning for releases:
- MAJOR.MINOR.PATCH (e.g., 1.2.3)
- Use tags: `v1.2.3`

### 3. Commit Messages
Use conventional commits:
```
feat: Add new feature
fix: Fix bug
docs: Update documentation
chore: Update dependencies
test: Add tests
```

### 4. PR Process
1. Create feature branch
2. Make changes
3. Push and create PR
4. Wait for CI checks
5. Address review comments
6. Merge when approved

## Extending the Pipeline

### Adding New Jobs
Create new job in existing workflow:
```yaml
new-job:
  name: New Job
  runs-on: ubuntu-latest
  needs: [build]  # Dependencies
  steps:
    - uses: actions/checkout@v4
    # Add your steps
```

### Adding New Workflows
Create new file in `.github/workflows/`:
```yaml
name: Custom Workflow
on:
  workflow_dispatch:
jobs:
  custom:
    runs-on: ubuntu-latest
    steps:
      # Your steps
```

### Using Reusable Workflows
```yaml
jobs:
  build:
    uses: ./.github/workflows/reusable-android-build.yml
    with:
      build-variant: release
      run-tests: true
    secrets: inherit
```

## Maintenance

### Regular Tasks
1. **Weekly**: Review dependency updates
2. **Monthly**: Clean up old artifacts
3. **Quarterly**: Review and update workflows

### Monitoring
- Check Actions tab for failed workflows
- Review security alerts
- Monitor build times and optimize if needed

## Support

For issues with the CI/CD pipeline:
1. Check workflow logs in Actions tab
2. Review this documentation
3. Check GitHub Actions status page
4. Open an issue with details