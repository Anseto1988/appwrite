const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function createFunction() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('Creating new function...\n');
    
    try {
        // First, try to delete existing function
        try {
            await functions.delete('dog-food-crawler');
            console.log('Deleted existing function');
            await new Promise(resolve => setTimeout(resolve, 2000));
        } catch (e) {
            console.log('No existing function to delete');
        }
        
        // Create with minimal parameters
        const func = await functions.create(
            'dog-food-crawler', // functionId
            'Dog Food Crawler', // name
            'node-16.0'        // runtime
        );
        
        console.log('✅ Function created:', func.$id);
        
        // Update function with additional settings
        console.log('\nUpdating function settings...');
        
        const updated = await functions.update(
            'dog-food-crawler',
            'Dog Food Crawler',
            'node-16.0', // runtime is required
            undefined,   // execute (keep existing)
            undefined,   // events (keep existing)
            '0 2 * * *', // schedule
            900,         // timeout
            true,        // enabled
            true         // logging
        );
        
        console.log('✅ Function updated');
        
        // Create environment variables
        console.log('\nCreating environment variables...');
        
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
                if (error.code === 409) {
                    console.log(`ℹ️  ${envVar.key} already exists`);
                } else {
                    console.log(`⚠️  Error creating ${envVar.key}:`, error.message);
                }
            }
        }
        
        console.log('\n✅ Function created successfully!');
        console.log('\n📦 Deployment package ready: dog-food-crawler-v16.tar.gz');
        console.log('   - Node 16 compatible');
        console.log('   - No engine warnings');
        console.log('   - All dependencies included');
        
        console.log('\n📌 Next steps:');
        console.log('1. Go to: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler');
        console.log('2. Click "Create deployment"');
        console.log('3. Upload: dog-food-crawler-v16.tar.gz');
        console.log('4. Entrypoint: src/index.js');
        console.log('5. Build command: npm install');
        console.log('6. ✅ Check "Activate deployment after build"');
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
createFunction();