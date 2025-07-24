# 🚨 Dog Food Crawler Deployment - Manual Steps Required

## Current Status
- ✅ Function created and configured
- ✅ Database collections set up  
- ✅ Environment variables configured
- ✅ Schedule set (daily at 2 AM)
- ❌ **NO ACTIVE DEPLOYMENT** (manual activation required)

## Problem
The Appwrite Node.js SDK does not properly support file uploads for deployments. Multiple attempts to programmatically activate deployments have failed.

## Solution: Manual Deployment Upload

### Step 1: Access Appwrite Console
Go to: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler/deployments

### Step 2: Create New Deployment
1. Click the **"Create deployment"** button
2. Upload the file: `dog-food-crawler.tar.gz` (12.26 KB)
3. Set the following:
   - **Entrypoint**: `src/index.js`
   - **Build command**: `npm install`
   - **⚠️ IMPORTANT**: Check ✅ "Activate deployment after build"

### Step 3: Wait for Build
The build process will:
1. Extract the tar.gz file
2. Run `npm install` to install dependencies
3. Prepare the function for execution
4. Automatically activate if the checkbox was selected

### Step 4: Verify Activation
After build completes:
1. The deployment status should show "ready"
2. The function overview should show the deployment ID as active
3. The "Execute now" button should be enabled

## Testing the Function

### Manual Execution
Once deployed, you can test with:
```bash
node simple-execution-test.js
```

Or in the Console:
1. Go to Function overview
2. Click "Execute now"
3. Use this test data:
```json
{
  "test": true,
  "limit": 5
}
```

### Expected Output
```json
{
  "success": true,
  "message": "Crawl session completed",
  "results": {
    "sessionId": "...",
    "startTime": "...",
    "endTime": "...",
    "duration": "...",
    "productsProcessed": 5,
    "newProducts": 3,
    "duplicates": 2,
    "errors": 0
  }
}
```

## Scheduled Execution
- The function is scheduled to run daily at 2:00 AM
- It will crawl up to 100 products per run
- Results are stored in the `foodSubmissions` collection
- Crawl state is maintained in `crawlState` collection

## Troubleshooting

### If deployment fails:
1. Check build logs in the deployment details
2. Common issues:
   - Missing dependencies in package.json
   - Syntax errors in code
   - Wrong entrypoint path

### If execution shows no logs:
1. Ensure deployment is active (check function overview)
2. Check environment variables are set
3. Look at execution details in Console

## Direct Links
- Function: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler
- Deployments: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler/deployments
- Executions: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler/executions
- Variables: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler/variables

## File Location
The deployment package is available at:
- Local: `/home/anseto/programe/snacktrack/dog-food-crawler.tar.gz`
- GitHub: https://github.com/Anseto1988/appwrite/blob/master/dog-food-crawler.tar.gz