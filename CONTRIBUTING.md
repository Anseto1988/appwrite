# Contributing to SnackTrack

We're excited that you're interested in contributing to SnackTrack! This document provides guidelines for contributing to the project.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing](#testing)
- [Documentation](#documentation)

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

### Our Standards
- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on constructive criticism
- Accept responsibility for mistakes

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR-USERNAME/snacktrack.git`
3. Add upstream remote: `git remote add upstream https://github.com/Anseto1988/appwrite.git`
4. Create a branch: `git checkout -b feature/your-feature-name`

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or higher
- Android SDK with minimum API 26
- Git

### Setup Instructions
1. Open the project in Android Studio
2. Let Android Studio sync the project
3. Configure local.properties with your SDK path
4. Copy `.env.example` to `.env` and configure Appwrite credentials
5. Build the project: `./gradlew build`

### Appwrite Backend Setup
1. Ensure you have access to the Appwrite instance at https://parse.nordburglarp.de/v2
2. Set up your API key in the `.env` file
3. Run the setup scripts to initialize collections

## How to Contribute

### Reporting Bugs
1. Check existing issues to avoid duplicates
2. Use the bug report template
3. Include:
   - Clear description of the issue
   - Steps to reproduce
   - Expected vs actual behavior
   - Device information
   - Screenshots if applicable

### Suggesting Features
1. Use the feature request template
2. Explain the problem your feature solves
3. Describe your proposed solution
4. Consider alternatives

### Contributing Code
1. Pick an issue or create one
2. Comment on the issue to claim it
3. Fork and create a feature branch
4. Write your code following our standards
5. Add tests for new functionality
6. Submit a pull request

## Coding Standards

### Kotlin Style Guide
We follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additions:

```kotlin
// Class naming
class DogRepository // Pascal case

// Function naming
fun calculateNutrition() // Camel case

// Constants
const val MAX_DOG_COUNT = 10 // Screaming snake case

// Package structure
com.example.snacktrack
├── data
│   ├── model
│   ├── repository
│   └── service
├── ui
│   ├── screens
│   ├── components
│   └── theme
└── utils
```

### Code Organization
- Keep functions small and focused
- Use meaningful variable and function names
- Add comments for complex logic
- Remove commented-out code before committing

### Android Best Practices
- Use ViewModel for UI state management
- Follow MVVM architecture pattern
- Use Kotlin coroutines for async operations
- Implement proper error handling
- Support configuration changes

## Commit Guidelines

We use conventional commits for clear history:

### Format
```
[ISSUE-XXX] type: description

Longer explanation if needed

Closes #XXX
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Test additions or changes
- `chore`: Build process or auxiliary tool changes

### Examples
```
[ISSUE-123] feat: Add barcode scanning functionality

Implemented camera permission handling and barcode detection
using ML Kit. Includes error handling and user feedback.

Closes #123
```

## Pull Request Process

### Before Submitting
1. Update your branch with latest `develop`:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```
2. Run all tests: `./gradlew test`
3. Run lint: `./gradlew lint`
4. Ensure build passes: `./gradlew build`

### PR Requirements
- Title format: `[ISSUE-XXX] Type: Brief description`
- Link to related issue(s)
- Description of changes
- Screenshots for UI changes
- Test instructions
- Checklist completion

### Review Process
1. Automated checks must pass
2. At least one maintainer review required
3. Address all feedback
4. Maintainer merges when approved

## Testing

### Unit Tests
- Write tests for all new functionality
- Maintain 80% code coverage
- Use MockK for mocking
- Follow AAA pattern (Arrange, Act, Assert)

Example:
```kotlin
@Test
fun `calculateCalories returns correct value`() {
    // Arrange
    val food = Food(calories = 100, amount = 50)
    
    // Act
    val result = nutritionCalculator.calculateCalories(food)
    
    // Assert
    assertEquals(50, result)
}
```

### Integration Tests
- Test API interactions
- Test database operations
- Use test fixtures for consistent data

### UI Tests
- Test critical user flows
- Use Espresso for UI testing
- Test on multiple screen sizes

## Documentation

### Code Documentation
- Add KDoc comments for public APIs
- Document complex algorithms
- Include examples in documentation

```kotlin
/**
 * Calculates the daily nutrition requirements for a dog
 * based on weight, age, and activity level.
 *
 * @param dog The dog to calculate nutrition for
 * @return NutritionRequirements object with daily values
 * @throws IllegalArgumentException if dog weight is invalid
 */
fun calculateNutritionRequirements(dog: Dog): NutritionRequirements
```

### README Updates
- Update README for significant features
- Keep setup instructions current
- Add new dependencies to documentation

## Questions?

If you have questions:
1. Check existing documentation
2. Search closed issues
3. Ask in an issue or discussion
4. Contact maintainers

## Recognition

Contributors are recognized in:
- Release notes
- Contributors file
- Project documentation

Thank you for contributing to SnackTrack! 🐕🦴