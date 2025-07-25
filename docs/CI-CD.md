# SnackTrack CI/CD Documentation

## Overview

This document describes the Continuous Integration and Continuous Deployment (CI/CD) pipeline for the SnackTrack Android application.

## GitHub Actions Workflows

### 1. Android CI (`android-ci.yml`)

**Trigger:** Push to `master` or `develop` branches, and all pull requests

**Jobs:**
- **Lint Check**: Runs Android lint to catch potential issues
- **Unit Tests**: Executes all unit tests with JaCoCo coverage
- **Build APK**: Creates debug/release APKs based on trigger
- **Instrumented Tests**: Runs UI tests on Android emulator (push only)
- **Security Scan**: Performs security analysis with MobSF

**Artifacts:**
- Lint reports
- Test results and coverage
- APK files
- Security scan results

### 2. PR Validation (`pr-validation.yml`)

**Trigger:** Pull request events (opened, synchronized, reopened)

**Checks:**
- Semantic PR title validation
- File size limits
- Code quality (Detekt, KtLint)
- Test coverage requirements (60% overall, 80% for changed files)
- Dependency security scan
- TODO/FIXME detection

### 3. Release Build (`release.yml`)

**Trigger:** Git tags matching `v*` pattern or manual workflow dispatch

**Process:**
1. Build signed Release APK
2. Build signed Release Bundle (AAB)
3. Generate changelog from commits
4. Create GitHub release with assets
5. Optional: Upload to Google Play Store (internal track)

### 4. Dependency Updates (`dependency-update.yml`)

**Trigger:** Weekly schedule (Mondays 9 AM UTC) or manual

**Features:**
- Checks for outdated dependencies
- Updates Gradle wrapper
- Creates PR with updates
- Runs security audit (OWASP)
- Creates issues for vulnerabilities

## Required GitHub Secrets

### Signing Configuration
- `SIGNING_KEYSTORE`: Base64 encoded keystore file
- `SIGNING_KEY_ALIAS`: Key alias for app signing
- `SIGNING_KEY_PASSWORD`: Password for the key
- `SIGNING_STORE_PASSWORD`: Password for the keystore

### Optional Integrations
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`: For Play Store uploads
- `FIREBASE_CONFIG`: Firebase configuration
- `SENTRY_AUTH_TOKEN`: For crash reporting
- `SLACK_WEBHOOK_URL`: For build notifications

## Environment Management

### Setup Script (`scripts/setup-env.sh`)

Creates environment-specific configuration files:

```bash
# Create all environments
./scripts/setup-env.sh all

# Create specific environment
./scripts/setup-env.sh production

# Show GitHub secrets setup
./scripts/setup-env.sh secrets
```

### Environment Files

- `.env.local`: Local development
- `.env.development`: Development server
- `.env.staging`: Staging environment
- `.env.production`: Production environment

## Docker Support

### Building with Docker

```bash
# Build the app
docker-compose run android-build

# Run tests
docker-compose run android-test

# Run lint
docker-compose run android-lint

# Build release APK
docker-compose run android-release
```

### Benefits
- Consistent build environment
- No local Android SDK required
- Cached dependencies
- Isolated builds

## Branch Strategy

- `master`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `release/*`: Release preparation

## Release Process

1. **Prepare Release**
   ```bash
   git checkout -b release/1.0.0
   # Update version in build.gradle
   # Update CHANGELOG.md
   git commit -m "chore: prepare release 1.0.0"
   ```

2. **Create Tag**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

3. **Automated Steps**
   - CI builds and signs the release
   - Creates GitHub release with changelog
   - Uploads APK and AAB files
   - Optionally deploys to Play Store

## Monitoring and Notifications

### Build Status
- Check Actions tab in GitHub repository
- Badge in README shows current build status

### Notifications
- PR comments for test coverage
- Issue creation for security vulnerabilities
- Optional Slack notifications

## Best Practices

1. **Commit Messages**
   - Use conventional commits format
   - Examples: `feat:`, `fix:`, `docs:`, `chore:`

2. **PR Guidelines**
   - Semantic PR titles required
   - All checks must pass
   - Coverage requirements enforced

3. **Security**
   - Never commit secrets
   - Use GitHub secrets for sensitive data
   - Regular dependency updates
   - Security scans on every build

4. **Performance**
   - Gradle build cache enabled
   - Parallel execution
   - Docker layer caching
   - Artifact caching in workflows

## Troubleshooting

### Common Issues

1. **Build Failures**
   - Check Gradle sync issues
   - Verify JDK version (17 required)
   - Clear caches: `./gradlew clean`

2. **Test Failures**
   - Run locally: `./gradlew test`
   - Check test reports in `app/build/reports`
   - Verify emulator configuration

3. **Signing Issues**
   - Verify secrets are correctly set
   - Check keystore encoding
   - Validate passwords and aliases

### Local Testing

Test workflows locally with [act](https://github.com/nektos/act):

```bash
# Install act
brew install act  # macOS

# Run specific workflow
act -W .github/workflows/android-ci.yml

# Run with secrets
act -W .github/workflows/release.yml --secret-file .env.secrets
```

## Maintenance

### Weekly Tasks
- Review dependency update PRs
- Check security scan results
- Monitor build times

### Monthly Tasks
- Update GitHub Actions versions
- Review and optimize workflows
- Clean up old artifacts

### Quarterly Tasks
- Update Docker base images
- Review branch protection rules
- Audit GitHub secrets

## Support

For CI/CD issues:
1. Check workflow logs in GitHub Actions
2. Review this documentation
3. Check [GitHub Actions documentation](https://docs.github.com/actions)
4. Contact DevOps team lead