# GitHub Authentication Instructions

You need to authenticate with GitHub to push your commits. You have 2 commits ready to push:
- 4c80414: [HIVE-SETUP] GitHub Manager Worker initialized with auto-commit scripts
- 5caf901: [HIVE-FIX] Major HIVE-MIND collective update (969 file changes)

## Option 1: GitHub CLI Authentication (Recommended)

Run this command in your terminal:

```bash
gh auth login
```

Follow the prompts:
1. Choose "GitHub.com"
2. Choose "HTTPS" as preferred protocol
3. Choose "Login with a web browser" or "Paste an authentication token"
4. If using browser, follow the prompts and enter the code shown
5. If using token, create one as described in Option 2 below

After authentication, run:
```bash
git push origin master
```

## Option 2: Personal Access Token (PAT)

1. Go to https://github.com/settings/tokens/new
2. Give it a descriptive name like "SnackTrack Development"
3. Select expiration (90 days recommended)
4. Select these scopes:
   - ✅ repo (Full control of private repositories)
   - ✅ workflow (Update GitHub Action workflows)
5. Click "Generate token"
6. **COPY THE TOKEN NOW** (you won't see it again!)

Then use the token:

```bash
# Configure Git to store credentials
git config --global credential.helper store

# Push (you'll be prompted for username and password)
git push origin master

# When prompted:
# Username: Anseto1988
# Password: [paste your token here]
```

## Option 3: Use GitHub CLI with existing token

If you already have a token, you can use:

```bash
echo "YOUR_PERSONAL_ACCESS_TOKEN" | gh auth login --with-token
```

## After Authentication

Once authenticated, verify the push was successful:

```bash
# Check the status
git status

# View the repository on GitHub
gh repo view --web
```

The pushed changes will be visible at:
https://github.com/Anseto1988/appwrite

## Security Note

Never share your Personal Access Token or commit it to the repository. If you accidentally expose it, immediately revoke it at https://github.com/settings/tokens