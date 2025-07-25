# Branch Protection Rules for SnackTrack

## Overview
This document outlines the branch protection rules for the SnackTrack Android app repository to ensure code quality and maintain a stable codebase.

## Protected Branches

### `main` (Production Branch)
- **Purpose**: Contains production-ready code that has been thoroughly tested
- **Direct pushes**: Disabled
- **Required pull request reviews**: 1
- **Dismiss stale PR approvals when new commits are pushed**: Yes
- **Required status checks**:
  - Build must pass
  - All tests must pass
  - Code coverage must meet threshold (80%)
- **Enforce for administrators**: No (for emergency fixes)
- **Required conversation resolution**: Yes

### `develop` (Development Branch)
- **Purpose**: Integration branch for features being prepared for the next release
- **Direct pushes**: Disabled
- **Required pull request reviews**: 1
- **Required status checks**:
  - Build must pass
  - All tests must pass
- **Include administrators**: No

## Branching Strategy

### Feature Branches
- **Naming convention**: `feature/[issue-number]-short-description`
- **Example**: `feature/123-add-barcode-scanner`
- **Merges into**: `develop`
- **Lifetime**: Delete after merge

### Bugfix Branches
- **Naming convention**: `bugfix/[issue-number]-short-description`
- **Example**: `bugfix/456-fix-login-crash`
- **Merges into**: `develop` (or `main` for hotfixes)
- **Lifetime**: Delete after merge

### Release Branches
- **Naming convention**: `release/v[version-number]`
- **Example**: `release/v1.2.0`
- **Created from**: `develop`
- **Merges into**: Both `main` and `develop`
- **Lifetime**: Keep for historical reference

### Hotfix Branches
- **Naming convention**: `hotfix/[issue-number]-short-description`
- **Example**: `hotfix/789-critical-security-fix`
- **Created from**: `main`
- **Merges into**: Both `main` and `develop`
- **Lifetime**: Delete after merge

## Pull Request Requirements

### All Pull Requests Must:
1. Have a descriptive title following the format: `[ISSUE-XXX] Type: Brief description`
2. Reference the related issue(s)
3. Include a description of changes
4. Have all CI checks passing
5. Be up to date with the target branch
6. Have no merge conflicts

### Code Review Checklist:
- [ ] Code follows Android coding conventions
- [ ] Unit tests are included for new functionality
- [ ] UI changes are tested on multiple screen sizes
- [ ] Documentation is updated if needed
- [ ] No hardcoded strings (use string resources)
- [ ] No sensitive data in code
- [ ] Performance considerations addressed
- [ ] Accessibility features maintained

## Merge Strategy
- **Feature → Develop**: Squash and merge (clean history)
- **Develop → Main**: Create a merge commit (preserve history)
- **Hotfix → Main**: Create a merge commit
- **Release → Main**: Create a merge commit

## Continuous Integration Requirements

### Required CI Checks:
1. **Build Check**: `./gradlew build`
2. **Unit Tests**: `./gradlew test`
3. **Instrumented Tests**: `./gradlew connectedAndroidTest`
4. **Lint Check**: `./gradlew lint`
5. **Code Coverage**: Minimum 80% coverage for new code

## Setting Up Branch Protection (GitHub)

To enable these rules in GitHub:

1. Go to Settings → Branches
2. Add rule for `main` branch:
   - Enable "Require a pull request before merging"
   - Enable "Require approvals" (1)
   - Enable "Dismiss stale pull request approvals"
   - Enable "Require status checks to pass"
   - Add required status checks
   - Enable "Require branches to be up to date"
   - Enable "Require conversation resolution"
3. Add similar rule for `develop` branch with appropriate settings
4. Save changes

## Emergency Procedures

In case of critical production issues:
1. Create a hotfix branch from `main`
2. Admin can bypass protection rules if absolutely necessary
3. Document the emergency change in the PR description
4. Follow up with proper testing in the next release

## Enforcement

- These rules apply to all contributors
- Violations should be reported to repository maintainers
- Regular audits will be conducted to ensure compliance