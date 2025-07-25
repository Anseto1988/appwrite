#!/bin/bash
# Environment setup script for SnackTrack
# This script helps manage environment variables for different environments

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to generate a secure random string
generate_secret() {
    if command_exists openssl; then
        openssl rand -base64 32
    else
        cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
    fi
}

# Function to create .env file
create_env_file() {
    local env_type=$1
    local env_file=".env.${env_type}"
    
    print_message $YELLOW "Creating ${env_file}..."
    
    cat > "$env_file" << EOF
# SnackTrack Environment Configuration
# Environment: ${env_type}
# Generated on: $(date)

# API Configuration
API_BASE_URL=${API_BASE_URL:-https://api.snacktrack.com}
API_VERSION=${API_VERSION:-v1}
API_TIMEOUT=${API_TIMEOUT:-30000}

# Authentication
AUTH_SECRET_KEY=$(generate_secret)
JWT_EXPIRATION=${JWT_EXPIRATION:-3600}
REFRESH_TOKEN_EXPIRATION=${REFRESH_TOKEN_EXPIRATION:-604800}

# Database Configuration
DATABASE_URL=${DATABASE_URL:-}
DATABASE_NAME=${DATABASE_NAME:-snacktrack_${env_type}}

# Firebase Configuration (if using)
FIREBASE_API_KEY=${FIREBASE_API_KEY:-}
FIREBASE_AUTH_DOMAIN=${FIREBASE_AUTH_DOMAIN:-}
FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID:-}
FIREBASE_STORAGE_BUCKET=${FIREBASE_STORAGE_BUCKET:-}
FIREBASE_MESSAGING_SENDER_ID=${FIREBASE_MESSAGING_SENDER_ID:-}
FIREBASE_APP_ID=${FIREBASE_APP_ID:-}

# Analytics
ANALYTICS_ENABLED=${ANALYTICS_ENABLED:-true}
ANALYTICS_KEY=${ANALYTICS_KEY:-}

# Feature Flags
FEATURE_SOCIAL_LOGIN=${FEATURE_SOCIAL_LOGIN:-false}
FEATURE_OFFLINE_MODE=${FEATURE_OFFLINE_MODE:-true}
FEATURE_PUSH_NOTIFICATIONS=${FEATURE_PUSH_NOTIFICATIONS:-true}

# Logging
LOG_LEVEL=${LOG_LEVEL:-info}
LOG_TO_FILE=${LOG_TO_FILE:-false}
CRASH_REPORTING_ENABLED=${CRASH_REPORTING_ENABLED:-true}

# Build Configuration
BUILD_TYPE=${env_type}
MINIFY_ENABLED=${MINIFY_ENABLED:-false}
PROGUARD_ENABLED=${PROGUARD_ENABLED:-false}

# App Signing (DO NOT COMMIT REAL VALUES)
KEYSTORE_PATH=${KEYSTORE_PATH:-}
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-}
KEY_ALIAS=${KEY_ALIAS:-}
KEY_PASSWORD=${KEY_PASSWORD:-}

# Third-party Services
SENTRY_DSN=${SENTRY_DSN:-}
MIXPANEL_TOKEN=${MIXPANEL_TOKEN:-}
ONESIGNAL_APP_ID=${ONESIGNAL_APP_ID:-}

# Environment-specific flags
DEBUG_MODE=${DEBUG_MODE:-false}
STRICT_MODE=${STRICT_MODE:-false}
LEAK_CANARY_ENABLED=${LEAK_CANARY_ENABLED:-false}
EOF

    if [ "$env_type" == "local" ] || [ "$env_type" == "development" ]; then
        cat >> "$env_file" << EOF

# Development-specific settings
DEBUG_MODE=true
STRICT_MODE=true
LEAK_CANARY_ENABLED=true
LOG_LEVEL=debug
MINIFY_ENABLED=false
PROGUARD_ENABLED=false
API_BASE_URL=http://localhost:8080
EOF
    elif [ "$env_type" == "production" ]; then
        cat >> "$env_file" << EOF

# Production-specific settings
DEBUG_MODE=false
STRICT_MODE=false
LEAK_CANARY_ENABLED=false
LOG_LEVEL=error
MINIFY_ENABLED=true
PROGUARD_ENABLED=true
EOF
    fi
    
    print_message $GREEN "✓ Created ${env_file}"
}

# Function to create gradle properties for environment
create_gradle_properties() {
    local env_type=$1
    local properties_file="gradle.${env_type}.properties"
    
    print_message $YELLOW "Creating ${properties_file}..."
    
    cat > "$properties_file" << EOF
# Gradle properties for ${env_type} environment
# Generated on: $(date)

# Build Configuration
org.gradle.jvmargs=-Xmx2048m -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
kotlin.code.style=official
android.useAndroidX=true
android.enableJetifier=false

# Build Features
android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false

# R8 Configuration
android.enableR8.fullMode=true
android.enableResourceOptimizations=true

# Environment-specific
IS_${env_type^^}_BUILD=true
EOF
    
    print_message $GREEN "✓ Created ${properties_file}"
}

# Function to setup GitHub secrets instructions
setup_github_secrets() {
    print_message $YELLOW "\n📝 GitHub Secrets Setup Instructions:"
    print_message $NC "Add the following secrets to your GitHub repository:"
    print_message $NC "(Settings → Secrets and variables → Actions → New repository secret)"
    
    cat << EOF

Required Secrets:
-----------------
SIGNING_KEYSTORE       - Base64 encoded keystore file
SIGNING_KEY_ALIAS      - Key alias for signing
SIGNING_KEY_PASSWORD   - Key password
SIGNING_STORE_PASSWORD - Keystore password

Optional Secrets:
-----------------
GOOGLE_PLAY_SERVICE_ACCOUNT_JSON - For Play Store deployment
FIREBASE_CONFIG                  - Firebase configuration JSON
SENTRY_AUTH_TOKEN               - For Sentry releases
SLACK_WEBHOOK_URL               - For build notifications

To encode your keystore:
  base64 -i your-keystore.jks | pbcopy  # macOS
  base64 your-keystore.jks | xclip      # Linux

EOF
}

# Main script
main() {
    print_message $GREEN "🚀 SnackTrack Environment Setup"
    print_message $NC "================================\n"
    
    # Check if we're in the project root
    if [ ! -f "settings.gradle" ] && [ ! -f "settings.gradle.kts" ]; then
        print_message $RED "❌ Error: This script must be run from the project root directory"
        exit 1
    fi
    
    # Create scripts directory if it doesn't exist
    mkdir -p scripts
    
    # Parse command line arguments
    case "$1" in
        "local"|"development"|"staging"|"production")
            create_env_file "$1"
            create_gradle_properties "$1"
            ;;
        "all")
            for env in local development staging production; do
                create_env_file "$env"
                create_gradle_properties "$env"
            done
            ;;
        "secrets")
            setup_github_secrets
            ;;
        *)
            print_message $YELLOW "Usage: $0 {local|development|staging|production|all|secrets}"
            print_message $NC "\nOptions:"
            print_message $NC "  local       - Create local development environment"
            print_message $NC "  development - Create development environment"
            print_message $NC "  staging     - Create staging environment"
            print_message $NC "  production  - Create production environment"
            print_message $NC "  all         - Create all environments"
            print_message $NC "  secrets     - Show GitHub secrets setup instructions"
            exit 1
            ;;
    esac
    
    # Add .env files to .gitignore if not already present
    if ! grep -q "^\.env" .gitignore 2>/dev/null; then
        print_message $YELLOW "\nAdding .env files to .gitignore..."
        cat >> .gitignore << EOF

# Environment files
.env
.env.*
!.env.example
gradle.*.properties
!gradle.properties
EOF
        print_message $GREEN "✓ Updated .gitignore"
    fi
    
    # Create .env.example if it doesn't exist
    if [ ! -f ".env.example" ]; then
        print_message $YELLOW "\nCreating .env.example..."
        cp ".env.local" ".env.example" 2>/dev/null || create_env_file "example"
        # Clear sensitive values
        sed -i.bak 's/=.*/=/' .env.example && rm .env.example.bak
        print_message $GREEN "✓ Created .env.example"
    fi
    
    print_message $GREEN "\n✅ Environment setup complete!"
    print_message $YELLOW "\n⚠️  Important reminders:"
    print_message $NC "  - Never commit .env files to version control"
    print_message $NC "  - Keep your signing keys secure"
    print_message $NC "  - Rotate secrets regularly"
    print_message $NC "  - Use different keys for each environment"
}

# Run main function
main "$@"