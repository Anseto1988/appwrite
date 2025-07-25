#!/bin/bash

# SnackTrack Test Automation Script
# Runs comprehensive test suite for the Android app

set -e  # Exit on any error

echo "🧪 SnackTrack Test Automation Starting..."
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "Not in the project root directory. Please run from the project root."
    exit 1
fi

# Store test results
TEST_RESULTS_DIR="build/test-results"
mkdir -p "$TEST_RESULTS_DIR"

# Function to run tests with error handling
run_test_suite() {
    local test_type=$1
    local gradle_task=$2
    local description=$3
    
    print_status "Running $description..."
    
    if ./gradlew $gradle_task --continue; then
        print_success "$description completed successfully"
        return 0
    else
        print_error "$description failed"
        return 1
    fi
}

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

# Run static analysis first
print_status "Running static analysis..."
LINT_FAILED=0
if ! ./gradlew lint; then
    print_warning "Lint checks found issues (non-blocking)"
    LINT_FAILED=1
fi

# Run unit tests
print_status "Starting Unit Tests..."
UNIT_TESTS_FAILED=0
if ! run_test_suite "unit" "test" "Unit Tests"; then
    UNIT_TESTS_FAILED=1
fi

# Run instrumented tests (if available)
print_status "Starting Instrumented Tests..."
INSTRUMENTED_TESTS_FAILED=0
if ! run_test_suite "instrumented" "connectedAndroidTest" "Instrumented Tests"; then
    print_warning "Instrumented tests failed or no connected devices found"
    INSTRUMENTED_TESTS_FAILED=1
fi

# Generate test reports
print_status "Generating test reports..."
./gradlew testReport || print_warning "Test report generation failed"

# Copy results to easily accessible location
if [ -d "app/build/reports/tests" ]; then
    cp -r app/build/reports/tests/* "$TEST_RESULTS_DIR/" 2>/dev/null || true
fi

# Check for specific test files and run them individually
print_status "Running specific repository and viewmodel tests..."

SPECIFIC_TESTS=(
    "com.example.snacktrack.DogRepositoryTest"
    "com.example.snacktrack.FoodRepositoryTest"
    "com.example.snacktrack.DogViewModelTest"
    "com.example.snacktrack.BarcodeRepositoryTest"
    "com.example.snacktrack.CommunityRepositoryTest"
)

SPECIFIC_TEST_FAILURES=0
for test_class in "${SPECIFIC_TESTS[@]}"; do
    print_status "Running $test_class..."
    if ./gradlew test --tests "$test_class" 2>/dev/null; then
        print_success "$test_class passed"
    else
        print_warning "$test_class failed or not found"
        SPECIFIC_TEST_FAILURES=$((SPECIFIC_TEST_FAILURES + 1))
    fi
done

# Summary
echo ""
echo "🏁 Test Execution Summary"
echo "========================="

if [ $UNIT_TESTS_FAILED -eq 0 ]; then
    print_success "Unit Tests: PASSED"
else
    print_error "Unit Tests: FAILED"
fi

if [ $INSTRUMENTED_TESTS_FAILED -eq 0 ]; then
    print_success "Instrumented Tests: PASSED"
else
    print_warning "Instrumented Tests: FAILED or SKIPPED"
fi

if [ $LINT_FAILED -eq 0 ]; then
    print_success "Lint Checks: PASSED"
else
    print_warning "Lint Checks: ISSUES FOUND"
fi

echo "Specific Test Failures: $SPECIFIC_TEST_FAILURES"

# Final status
TOTAL_FAILURES=$((UNIT_TESTS_FAILED + SPECIFIC_TEST_FAILURES))

if [ $TOTAL_FAILURES -eq 0 ]; then
    print_success "🎉 All critical tests passed!"
    echo ""
    echo "📊 Test Reports Location:"
    echo "   - HTML Reports: $TEST_RESULTS_DIR/"
    echo "   - XML Reports: app/build/test-results/"
    exit 0
else
    print_error "❌ $TOTAL_FAILURES critical test suite(s) failed"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Check test reports in: $TEST_RESULTS_DIR/"
    echo "   2. Fix failing tests"
    echo "   3. Re-run tests"
    exit 1
fi