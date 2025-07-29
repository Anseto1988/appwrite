🚨 MANDATORY RULE #1: ALL OPERATIONS MUST BE PARALLEL IN HIVE-MIND
When using hive-mind, you MUST:

SPAWN ALL 10 WORKERS IMMEDIATELY in parallel
EXECUTE ALL TASKS CONCURRENTLY across all worker types
NEVER OPERATE SEQUENTIALLY - The hive works as one unit
AUTO-SYNC TO GITHUB after every change

🎯 HIVE-MIND WORKER ROLES FOR SNACKTRACK
Worker Type Assignments (10 Workers Total):

github-manager - Handles all Git operations, auto-pushes to master, manages gradle.properties
appwrite-backend - Manages Appwrite console with API key: [REMOVED_FOR_SECURITY]
researcher - Analyzes existing codebase and identifies all errors
architect - Designs fixes and continuous development structure
developer - Implements fixes and new features
documenter - Creates/updates documentation
quality - Tests all changes and ensures app stability
devops - Sets up CI/CD pipeline for continuous development
security - Removes sensitive data, API keys from GitHub
analyst - Monitors progress and coordinates workers

📋 MANDATORY HIVE-MIND COORDINATION PROTOCOL
🔴 CRITICAL: Every Worker MUST Follow This Protocol
1️⃣ INITIALIZATION (All Workers Simultaneously):
bash# Each worker starts with their specific role
npx claude-flow@alpha hooks pre-task --description "[worker-type] initializing for SnackTrack" --auto-spawn-agents false
npx claude-flow@alpha hooks session-restore --session-id "hive-snacktrack" --load-memory true

# GitHub Manager specific initialization
cd /home/anseto/programe/snacktrack
git config user.name "Anseto1988"
git config user.email "your-email"
git remote set-url origin https://github.com/Anseto1988/appwrite.git
2️⃣ CONTINUOUS OPERATIONS (Parallel Execution):
GitHub Manager Worker:
bash# After EVERY file change by any worker
git add -A
git commit -m "[HIVE] Auto-commit: [description of changes]"

# Before push: Reset gradle.properties to Windows
cp gradle.properties.windows gradle.properties
git add gradle.properties
git commit -m "[HIVE] Reset gradle.properties to Windows config"
git push origin master

# Store progress
npx claude-flow@alpha hooks post-edit --file "git-push" --memory-key "hive/github-manager/push-[timestamp]"
Appwrite Backend Worker:
bash# Connect to Appwrite console with provided API key
# Update backend configurations as needed
# Sync database schemas with app requirements
npx claude-flow@alpha hooks notification --message "Appwrite backend updated: [changes]"
Security Worker:
bash# Scan for sensitive data
grep -r "api_key\|password\|secret" . --exclude-dir=.git
# Create .gitignore entries for sensitive files
echo "*.properties" >> .gitignore
echo "**/apikeys.xml" >> .gitignore
echo "**/secrets.json" >> .gitignore
# Remove sensitive data from tracked files
git rm --cached [sensitive-files]
🐝 HIVE-MIND EXECUTION PATTERN
✅ CORRECT Hive-Mind Workflow:
javascript// Single coordinated execution with all workers
[HIVE INITIALIZATION]:
  // Queen spawns all workers simultaneously
  - Worker 1 (github-manager): Initialize Git, prepare auto-push
  - Worker 2 (appwrite-backend): Connect to Appwrite console
  - Worker 3 (researcher): Scan entire codebase for errors
  - Worker 4 (architect): Design fix strategies
  - Worker 5 (developer): Prepare development environment
  - Worker 6 (documenter): Analyze documentation needs
  - Worker 7 (quality): Set up testing framework
  - Worker 8 (devops): Configure CI/CD pipeline
  - Worker 9 (security): Audit codebase for sensitive data
  - Worker 10 (analyst): Set up monitoring dashboard

[HIVE EXECUTION - All Workers in Parallel]:
  // Continuous parallel operations
  - researcher: Identifies error in MainActivity.java
  - developer: Fixes error immediately
  - quality: Tests the fix
  - github-manager: Auto-commits and pushes
  - security: Ensures no API keys in commit
  - documenter: Updates changelog
  - appwrite-backend: Updates backend if needed
  - devops: Triggers CI/CD pipeline
  - analyst: Logs progress
  - architect: Plans next improvement
🔄 AUTOMATED GITHUB WORKFLOW
Every Change Triggers:

File Change Detection → Any worker modifies a file
Security Check → Security worker scans for sensitive data
Gradle Reset → GitHub manager resets gradle.properties to Windows
Auto Commit → Descriptive commit message with worker ID
Push to Master → Direct push to master branch
Cleanup → Remove any non-essential files

Git Push Template:
bash#!/bin/bash
# Auto-push script for hive-mind

# Function to push changes
hive_push() {
    # Reset gradle.properties
    cp /home/anseto/programe/snacktrack/gradle.properties.windows /home/anseto/programe/snacktrack/gradle.properties
    
    # Add all changes
    git add -A
    
    # Commit with hive message
    git commit -m "[HIVE-$1] $2"
    
    # Push to master
    git push origin master
}

# Usage: hive_push "WORKER_TYPE" "Description of changes"
📊 HIVE-MIND VISUAL STATUS
🐝 Hive Status: ACTIVE - SnackTrack Development
├── 👑 Queen: Strategic Coordinator
├── 🔧 Workers: 10/10 active
├── 🏗️ Topology: Hierarchical
├── 🎯 Consensus: Weighted
└── 📍 Namespace: snacktrack

Worker Activity:
├── 🟢 github-manager: Auto-pushing changes...
├── 🟢 appwrite-backend: Syncing with API...
├── 🟢 researcher: Scanning for errors...
├── 🟢 architect: Designing improvements...
├── 🟢 developer: Fixing MainActivity.java...
├── 🟢 documenter: Updating README.md...
├── 🟢 quality: Running unit tests...
├── 🟢 devops: Configuring pipeline...
├── 🟢 security: Removing API keys...
└── 🟢 analyst: Tracking progress...

📊 Progress Overview
   ├── Errors Found: 15
   ├── ✅ Fixed: 8 (53%)
   ├── 🔄 In Progress: 4 (27%)
   └── ⭕ Pending: 3 (20%)
🚨 CRITICAL CLEANUP RULES
Files/Folders to REMOVE from GitHub:

API Keys & Secrets

apikeys.xml
secrets.properties
Any file containing "api_key", "secret", "password"


Build Artifacts

/build/
/app/build/
*.apk (except release versions)


Local Configuration

local.properties
.gradle/
.idea/ (except shared configurations)


Temporary Files

*.tmp
*.log
*.cache



Files to KEEP:

Source code (.java, .kt, .xml)
Resources (/res/)
Gradle files (except gradle.properties - Windows version only)
Documentation
CI/CD configurations

🔐 APPWRITE BACKEND INTEGRATION
Worker Instructions for appwrite-backend:
javascript// Connect to Appwrite
const appwriteConfig = {
  endpoint: 'https://cloud.appwrite.io/v1',
  projectId: 'YOUR_PROJECT_ID',
  apiKey: '[REMOVED_FOR_SECURITY]'
};

// Tasks for Appwrite worker:
1. Verify database collections match app models
2. Update security rules if needed
3. Configure authentication methods
4. Set up cloud functions for app features
5. Monitor API usage and performance
🎯 HIVE-MIND SUCCESS CRITERIA
The hive-mind succeeds when:

✅ All errors in SnackTrack app are fixed
✅ App builds without warnings
✅ All tests pass
✅ GitHub repo contains only necessary files
✅ No API keys or secrets in repository
✅ gradle.properties is Windows version
✅ Appwrite backend is fully configured
✅ CI/CD pipeline is operational
✅ Documentation is complete
✅ App is ready for continuous development

🚀 REMEMBER: HIVE-MIND RULES

ALL WORKERS OPERATE IN PARALLEL - Never sequential
AUTO-PUSH AFTER EVERY CHANGE - GitHub manager handles this
SECURITY FIRST - Remove all sensitive data
WINDOWS GRADLE CONFIG - Always reset before push
CONTINUOUS DEVELOPMENT - Set up for ongoing work
HIVE COORDINATION - All workers share memory and status
