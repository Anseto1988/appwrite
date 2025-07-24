const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function deleteAndRecreateFunction() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔄 Deleting and Recreating Function\n');
    
    try {
        // 1. Delete old function
        console.log('1. Deleting old function...');
        try {
            await functions.delete('dog-food-crawler');
            console.log('✅ Old function deleted');
        } catch (error) {
            if (error.code === 404) {
                console.log('ℹ️  Function not found, continuing...');
            } else {
                console.log('⚠️  Error deleting:', error.message);
            }
        }
        
        // Wait a moment
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 2. Create new function with Node 16
        console.log('\n2. Creating new function with Node 16...');
        
        const newFunction = await functions.create(
            'dog-food-crawler',
            'Dog Food Crawler',
            'node-16.0', // Using Node 16.0
            [],          // Execute permissions (empty = manual execution only)
            [],          // Events
            '0 2 * * *', // Daily at 2 AM
            900,         // 15 minutes timeout
            true,        // Enabled
            true,        // Logging
            'src/index.js', // Entrypoint
            'npm install',  // Build command
            '.'             // Root directory
        );
        
        console.log('✅ Function created:', newFunction.$id);
        
        // 3. Create environment variables
        console.log('\n3. Creating environment variables...');
        
        const envVars = [
            { key: 'APPWRITE_ENDPOINT', value: ENDPOINT },
            { key: 'APPWRITE_FUNCTION_PROJECT_ID', value: PROJECT_ID },
            { key: 'DATABASE_ID', value: 'snacktrack-db' },
            { key: 'APPWRITE_API_KEY', value: API_KEY },
            { key: 'SUBMISSIONS_COLLECTION_ID', value: 'foodSubmissions' },
            { key: 'CRAWL_STATE_COLLECTION_ID', value: 'crawlState' },
            { key: 'MAX_PRODUCTS_PER_RUN', value: '100' },
            { key: 'CRAWLER_USER_ID', value: 'system_crawler' }
        ];
        
        for (const envVar of envVars) {
            try {
                await functions.createVariable(
                    'dog-food-crawler',
                    envVar.key,
                    envVar.value
                );
                console.log(`✅ Created: ${envVar.key}`);
            } catch (error) {
                console.log(`⚠️  Error creating ${envVar.key}:`, error.message);
            }
        }
        
        console.log('\n✅ Function recreated successfully!');
        console.log('\n📌 Next steps:');
        console.log('1. Go to: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler');
        console.log('2. Click "Create deployment"');
        console.log('3. Upload: dog-food-crawler-v16.tar.gz');
        console.log('4. Set entrypoint: src/index.js');
        console.log('5. Set build command: npm install');
        console.log('6. ✅ Check "Activate deployment after build"');
        console.log('\nThe new package is Node 16 compatible and should build without warnings.');
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
deleteAndRecreateFunction();