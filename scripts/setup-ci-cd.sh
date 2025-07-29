#!/bin/bash

# CI/CD Setup Script for SnackTrack
# This script helps set up the required secrets for GitHub Actions

set -e

echo "==================================="
echo "SnackTrack CI/CD Setup Script"
echo "==================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}GitHub CLI (gh) is not installed.${NC}"
    echo "Please install it from: https://cli.github.com/"
    exit 1
fi

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}Not in a git repository!${NC}"
    exit 1
fi

# Get repository info
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
echo -e "Repository: ${GREEN}$REPO${NC}"
echo ""

# Function to set a secret
set_secret() {
    local secret_name=$1
    local secret_value=$2
    
    echo -n "Setting $secret_name... "
    if gh secret set "$secret_name" -b "$secret_value" 2>/dev/null; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
        return 1
    fi
}

# Function to set a secret from file
set_secret_from_file() {
    local secret_name=$1
    local file_path=$2
    
    echo -n "Setting $secret_name from file... "
    if gh secret set "$secret_name" < "$file_path" 2>/dev/null; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
        return 1
    fi
}

echo "=== Appwrite Configuration ==="
echo ""

# Check for Appwrite environment variables
if [[ -f .env ]]; then
    echo -e "${YELLOW}Found .env file. Loading Appwrite configuration...${NC}"
    source .env
fi

# Appwrite Endpoint
if [[ -z "$APPWRITE_ENDPOINT" ]]; then
    read -p "Enter Appwrite Endpoint (e.g., https://cloud.appwrite.io/v1): " APPWRITE_ENDPOINT
fi
set_secret "APPWRITE_ENDPOINT" "$APPWRITE_ENDPOINT"

# Appwrite Project ID
if [[ -z "$APPWRITE_PROJECT_ID" ]]; then
    read -p "Enter Appwrite Project ID: " APPWRITE_PROJECT_ID
fi
set_secret "APPWRITE_PROJECT_ID" "$APPWRITE_PROJECT_ID"

# Appwrite API Key
if [[ -z "$APPWRITE_API_KEY" ]]; then
    read -sp "Enter Appwrite API Key: " APPWRITE_API_KEY
    echo ""
fi
set_secret "APPWRITE_API_KEY" "$APPWRITE_API_KEY"

echo ""
echo "=== Android Signing Configuration (Optional) ==="
echo ""

read -p "Do you want to set up Android signing for releases? (y/N): " setup_signing
if [[ "$setup_signing" =~ ^[Yy]$ ]]; then
    
    # Check for existing keystore
    if [[ -f "release-keystore.jks" ]]; then
        echo -e "${YELLOW}Found existing keystore: release-keystore.jks${NC}"
        KEYSTORE_FILE="release-keystore.jks"
    else
        read -p "Enter path to keystore file (or press Enter to create new): " KEYSTORE_FILE
        
        if [[ -z "$KEYSTORE_FILE" ]]; then
            # Create new keystore
            echo ""
            echo "Creating new keystore..."
            KEYSTORE_FILE="release-keystore.jks"
            
            read -p "Enter key alias (default: snacktrack): " KEY_ALIAS
            KEY_ALIAS=${KEY_ALIAS:-snacktrack}
            
            keytool -genkey -v -keystore "$KEYSTORE_FILE" \
                -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity 10000
        fi
    fi
    
    # Convert keystore to base64
    echo -n "Converting keystore to base64... "
    KEYSTORE_BASE64=$(base64 -w 0 "$KEYSTORE_FILE")
    echo -e "${GREEN}✓${NC}"
    
    # Set keystore secret
    set_secret "ANDROID_KEYSTORE_BASE64" "$KEYSTORE_BASE64"
    
    # Get keystore password
    read -sp "Enter keystore password: " KEYSTORE_PASSWORD
    echo ""
    set_secret "ANDROID_KEYSTORE_PASSWORD" "$KEYSTORE_PASSWORD"
    
    # Get key alias
    if [[ -z "$KEY_ALIAS" ]]; then
        read -p "Enter key alias: " KEY_ALIAS
    fi
    set_secret "ANDROID_KEY_ALIAS" "$KEY_ALIAS"
    
    # Get key password
    read -sp "Enter key password: " KEY_PASSWORD
    echo ""
    set_secret "ANDROID_KEY_PASSWORD" "$KEY_PASSWORD"
fi

echo ""
echo "=== Code Quality Configuration (Optional) ==="
echo ""

read -p "Do you want to set up SonarCloud? (y/N): " setup_sonar
if [[ "$setup_sonar" =~ ^[Yy]$ ]]; then
    read -p "Enter SonarCloud token: " SONAR_TOKEN
    set_secret "SONAR_TOKEN" "$SONAR_TOKEN"
    
    echo ""
    echo -e "${YELLOW}Don't forget to update the SonarCloud configuration in android-ci.yml:${NC}"
    echo "  - sonar.projectKey"
    echo "  - sonar.organization"
fi

echo ""
echo "=== Branch Protection Rules ==="
echo ""

read -p "Do you want to set up branch protection rules for 'master'? (y/N): " setup_protection
if [[ "$setup_protection" =~ ^[Yy]$ ]]; then
    echo "Setting up branch protection for 'master'..."
    
    # Create branch protection rule
    gh api \
        --method PUT \
        -H "Accept: application/vnd.github+json" \
        "/repos/$REPO/branches/master/protection" \
        -f required_status_checks='{"strict":true,"contexts":["continuous-integration/github-actions"]}' \
        -f enforce_admins=false \
        -f required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true}' \
        -f restrictions=null \
        -f allow_force_pushes=false \
        -f allow_deletions=false
    
    echo -e "${GREEN}Branch protection enabled for 'master'${NC}"
fi

echo ""
echo "=== Setup Complete! ==="
echo ""
echo -e "${GREEN}✓${NC} GitHub Secrets configured"
echo -e "${GREEN}✓${NC} CI/CD pipelines ready to use"
echo ""
echo "Next steps:"
echo "1. Commit and push the workflow files"
echo "2. Create a PR to test the PR checks"
echo "3. Tag a release to test the release pipeline"
echo ""
echo "For more information, see: docs/CI-CD-GUIDE.md"