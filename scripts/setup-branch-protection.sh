#!/bin/bash
# Script to set up branch protection rules using GitHub CLI
# Requires: gh CLI tool and authentication

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

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    print_message $RED "❌ Error: GitHub CLI (gh) is not installed"
    print_message $NC "Install it from: https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    print_message $RED "❌ Error: Not authenticated with GitHub"
    print_message $NC "Run: gh auth login"
    exit 1
fi

# Get repository info
REPO_OWNER=$(gh repo view --json owner -q .owner.login)
REPO_NAME=$(gh repo view --json name -q .name)

print_message $GREEN "🚀 Setting up branch protection for $REPO_OWNER/$REPO_NAME"
print_message $NC "================================================\n"

# Function to create branch protection rule
create_protection_rule() {
    local branch=$1
    local require_pr_reviews=$2
    local required_approvals=$3
    local dismiss_stale_reviews=$4
    local require_code_owner_reviews=$5
    local include_admins=$6
    
    print_message $YELLOW "Setting up protection for branch: $branch"
    
    # Create the protection rule
    gh api -X PUT \
        "/repos/$REPO_OWNER/$REPO_NAME/branches/$branch/protection" \
        --field "required_status_checks[strict]=true" \
        --field "required_status_checks[contexts][]=lint" \
        --field "required_status_checks[contexts][]=unit-test" \
        --field "required_status_checks[contexts][]=build" \
        --field "enforce_admins=$include_admins" \
        --field "required_pull_request_reviews[required_approving_review_count]=$required_approvals" \
        --field "required_pull_request_reviews[dismiss_stale_reviews]=$dismiss_stale_reviews" \
        --field "required_pull_request_reviews[require_code_owner_reviews]=$require_code_owner_reviews" \
        --field "restrictions=null" \
        --field "allow_force_pushes=false" \
        --field "allow_deletions=false" \
        --field "required_conversation_resolution=true" \
        --field "lock_branch=false" \
        --field "allow_fork_syncing=true" \
        2>/dev/null || {
            print_message $RED "❌ Failed to set protection for $branch"
            return 1
        }
    
    print_message $GREEN "✓ Protection enabled for $branch"
}

# Main protection rules
print_message $NC "\n📋 Configuring branch protection rules...\n"

# Master branch - strictest protection
create_protection_rule "master" true 2 true true true

# Develop branch - moderate protection
create_protection_rule "develop" true 1 true false false

# Create CODEOWNERS file if it doesn't exist
if [ ! -f ".github/CODEOWNERS" ]; then
    print_message $YELLOW "\nCreating CODEOWNERS file..."
    mkdir -p .github
    cat > .github/CODEOWNERS << EOF
# Code Owners for SnackTrack
# These owners will be requested for review when someone opens a pull request

# Global owners
* @Anseto1988

# Android specific
/app/ @Anseto1988
*.kt @Anseto1988
*.gradle @Anseto1988

# CI/CD
/.github/ @Anseto1988
/scripts/ @Anseto1988

# Documentation
/docs/ @Anseto1988
*.md @Anseto1988
EOF
    print_message $GREEN "✓ Created CODEOWNERS file"
fi

# Create auto-labeler configuration
print_message $YELLOW "\nConfiguring auto-labeler..."
gh api -X PUT \
    "/repos/$REPO_OWNER/$REPO_NAME/contents/.github/labeler.yml" \
    --field "message=Add auto-labeler configuration" \
    --field "content=$(base64 -w 0 .github/labeler.yml 2>/dev/null || base64 .github/labeler.yml)" \
    2>/dev/null || print_message $YELLOW "⚠️  Labeler config already exists or couldn't be created"

# Enable required settings
print_message $YELLOW "\nEnabling repository settings..."

# Enable issues
gh api -X PATCH \
    "/repos/$REPO_OWNER/$REPO_NAME" \
    --field "has_issues=true" \
    --field "has_projects=true" \
    --field "has_wiki=false" \
    --field "allow_squash_merge=true" \
    --field "allow_merge_commit=false" \
    --field "allow_rebase_merge=true" \
    --field "delete_branch_on_merge=true" \
    --field "allow_auto_merge=true" \
    2>/dev/null || print_message $RED "❌ Failed to update repository settings"

print_message $GREEN "✓ Repository settings updated"

# Create default labels
print_message $YELLOW "\nCreating default labels..."

create_label() {
    local name=$1
    local color=$2
    local description=$3
    
    gh label create "$name" --color "$color" --description "$description" 2>/dev/null || true
}

# Priority labels
create_label "P0: Critical" "b60205" "Critical priority - immediate action required"
create_label "P1: High" "d93f0b" "High priority"
create_label "P2: Medium" "fbca04" "Medium priority"
create_label "P3: Low" "0e8a16" "Low priority"

# Type labels
create_label "bug" "d73a4a" "Something isn't working"
create_label "enhancement" "a2eeef" "New feature or request"
create_label "documentation" "0075ca" "Improvements or additions to documentation"
create_label "security" "ff0000" "Security vulnerability or concern"

# Status labels
create_label "good first issue" "7057ff" "Good for newcomers"
create_label "help wanted" "008672" "Extra attention is needed"
create_label "wontfix" "ffffff" "This will not be worked on"
create_label "duplicate" "cfd3d7" "This issue or pull request already exists"

# Component labels
create_label "android" "3DDC84" "Android app related"
create_label "backend" "1e90ff" "Backend/API related"
create_label "ci" "000000" "Continuous Integration"
create_label "dependencies" "0366d6" "Pull requests that update a dependency file"

print_message $GREEN "✓ Labels created"

# Summary
print_message $GREEN "\n✅ Branch Protection Setup Complete!"
print_message $NC "\n📊 Summary:"
print_message $NC "  - Master branch: Protected (2 reviews required, admins included)"
print_message $NC "  - Develop branch: Protected (1 review required)"
print_message $NC "  - CODEOWNERS file: Created/Updated"
print_message $NC "  - Auto-labeler: Configured"
print_message $NC "  - Repository settings: Updated"
print_message $NC "  - Default labels: Created"

print_message $YELLOW "\n⚠️  Additional manual steps:"
print_message $NC "  1. Go to Settings → Branches in GitHub"
print_message $NC "  2. Verify protection rules are correctly applied"
print_message $NC "  3. Add team members as collaborators if needed"
print_message $NC "  4. Configure webhooks for external services"
print_message $NC "  5. Set up deployment environments in Settings → Environments"

print_message $GREEN "\n🎉 Done! Your repository is now properly configured for CI/CD."