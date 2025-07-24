const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';
const DATABASE_ID = 'snacktrack-db';

async function deployFunction() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Setting up Dog Food Crawler Function...\n');
    
    try {
        const functionId = 'dog-food-crawler';
        
        // Update environment variables
        console.log('📋 Setting environment variables...');
        
        const envVars = {
            'APPWRITE_ENDPOINT': ENDPOINT,
            'APPWRITE_FUNCTION_PROJECT_ID': PROJECT_ID,
            'DATABASE_ID': DATABASE_ID,
            'SUBMISSIONS_COLLECTION_ID': 'foodSubmissions',
            'CRAWL_STATE_COLLECTION_ID': 'crawlState',
            'CRAWLER_USER_ID': 'system_crawler',
            'MAX_PRODUCTS_PER_RUN': '500',
            'APPWRITE_API_KEY': API_KEY
        };
        
        for (const [key, value] of Object.entries(envVars)) {
            try {
                await functions.createVariable(functionId, key, value);
                console.log(`✅ Set ${key}`);
            } catch (error) {
                if (error.code === 409) {
                    // Variable exists, update it
                    // Get list of variables to find the ID
                    const variables = await functions.listVariables(functionId);
                    const variable = variables.variables.find(v => v.key === key);
                    
                    if (variable) {
                        await functions.updateVariable(functionId, variable.$id, key, value);
                        console.log(`✅ Updated ${key}`);
                    }
                } else {
                    console.error(`❌ Error setting ${key}:`, error.message);
                }
            }
        }
        
        console.log('\n✅ Environment variables set successfully!');
        console.log('\n📋 Next steps:');
        console.log('1. Go to Appwrite Console: https://parse.nordburglarp.de/console');
        console.log('2. Navigate to Functions > dog-food-crawler');
        console.log('3. Click "Create deployment"');
        console.log('4. Upload the code from: functions/dog-food-crawler/');
        console.log('5. Set entrypoint: src/index.js');
        console.log('6. Set build command: npm install');
        console.log('\nThe function will run automatically at 2:00 AM UTC daily.');
        
        // Create a deployment instruction file
        const instructions = `# Dog Food Crawler Deployment Instructions

## Function Details
- Function ID: dog-food-crawler
- Runtime: node-16.0
- Schedule: 0 2 * * * (Daily at 2:00 AM UTC)
- Timeout: 900 seconds (15 minutes)

## Manual Deployment Steps

1. Create a ZIP file of the function code:
   \`\`\`bash
   cd functions/dog-food-crawler
   zip -r ../../dog-food-crawler.zip .
   \`\`\`

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
`;
        
        require('fs').writeFileSync('DEPLOYMENT_INSTRUCTIONS.md', instructions);
        console.log('\n📄 Created DEPLOYMENT_INSTRUCTIONS.md with detailed steps');
        
    } catch (error) {
        console.error('\n❌ Setup failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run setup
deployFunction();