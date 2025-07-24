# Dog Food Crawler Deployment Instructions

## Function Details
- Function ID: dog-food-crawler
- Runtime: node-16.0
- Schedule: 0 2 * * * (Daily at 2:00 AM UTC)
- Timeout: 900 seconds (15 minutes)

## Manual Deployment Steps

1. Create a ZIP file of the function code:
   ```bash
   cd functions/dog-food-crawler
   zip -r ../../dog-food-crawler.zip .
   ```

2. Go to Appwrite Console: https://parse.nordburglarp.de/console

3. Navigate to: Functions > dog-food-crawler

4. Click "Create deployment"

5. Upload the ZIP file

6. Set:
   - Entrypoint: src/index.js
   - Build commands: npm install

7. Click "Create"

## Testing

After deployment, you can:
1. Click "Execute" to run the function manually
2. Check logs in the "Executions" tab
3. Monitor the crawlState collection for progress

## Environment Variables (Already Set)
- APPWRITE_ENDPOINT
- APPWRITE_FUNCTION_PROJECT_ID
- DATABASE_ID
- SUBMISSIONS_COLLECTION_ID
- CRAWL_STATE_COLLECTION_ID
- CRAWLER_USER_ID
- MAX_PRODUCTS_PER_RUN
- APPWRITE_API_KEY
