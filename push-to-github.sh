#!/bin/bash

echo "====================================="
echo "GitHub Push Helper for SnackTrack"
echo "====================================="
echo ""
echo "You have 2 commits ready to push:"
echo "- 4c80414: [HIVE-SETUP] GitHub Manager Worker initialized"
echo "- 5caf901: [HIVE-FIX] Major update with 969 file changes"
echo ""
echo "Remote repository: https://github.com/Anseto1988/appwrite"
echo ""

# Check if gh CLI is authenticated
if gh auth status &>/dev/null; then
    echo "✅ GitHub CLI is authenticated!"
    echo ""
    echo "Pushing to GitHub..."
    git push origin master
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Push successful!"
        echo ""
        echo "View your changes at:"
        echo "https://github.com/Anseto1988/appwrite/commits/master"
        echo ""
        echo "Latest commit:"
        echo "https://github.com/Anseto1988/appwrite/commit/5caf901435c22159da8a8de54867348dffd8b7cb"
    else
        echo "❌ Push failed. Please check your authentication."
    fi
else
    echo "❌ GitHub CLI is not authenticated."
    echo ""
    echo "Please run one of these commands:"
    echo ""
    echo "1. For interactive login:"
    echo "   gh auth login"
    echo ""
    echo "2. If you have a Personal Access Token:"
    echo "   echo 'YOUR_TOKEN' | gh auth login --with-token"
    echo ""
    echo "After authentication, run this script again."
fi