const sdk = require('node-appwrite');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';
const DATABASE_ID = 'snacktrack-db';

async function createFunction() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    const storage = new sdk.Storage(client);
    
    console.log('🚀 Creating Dog Food Crawler Function...\n');
    
    try {
        // Step 1: Create Function
        console.log('📋 Step 1: Creating function...');
        
        // Create function with minimal required parameters
        const functionData = await functions.create(
            'dog-food-crawler',
            'Dog Food Crawler',
            'node-16.0'
        );
        
        console.log(`✅ Created function: ${functionData.$id}`);
        
        // Step 2: Update function configuration
        console.log('\n📋 Step 2: Updating function configuration...');
        
        await functions.update(
            functionData.$id,
            'Dog Food Crawler',
            'node-16.0',
            ['any'], // execute permissions
            [], // events
            '0 2 * * *', // Daily at 2 AM
            900, // 15 minutes timeout
            true, // enabled
            true // logging
        );
        
        console.log('✅ Updated function configuration');
        
        // Step 3: Create deployment package
        console.log('\n📋 Step 3: Creating deployment package...');
        
        const functionDir = path.join(__dirname, 'functions', 'dog-food-crawler');
        const tarPath = path.join(__dirname, 'crawler-code.tar.gz');
        
        // Create tar.gz file
        execSync(`cd "${functionDir}" && tar -czf "${tarPath}" .`);
        console.log('✅ Created deployment package');
        
        // Step 4: Create deployment
        console.log('\n📋 Step 4: Creating deployment...');
        
        // Read the tar.gz file
        const code = fs.readFileSync(tarPath);
        
        // Create InputFile from buffer
        const file = sdk.InputFile.fromBuffer(code, 'code.tar.gz');
        
        const deployment = await functions.createDeployment(
            functionData.$id,
            file,
            true, // activate
            'src/index.js', // entrypoint
            'npm install' // commands
        );
        
        console.log(`✅ Created deployment: ${deployment.$id}`);
        
        // Step 5: Update environment variables
        console.log('\n📋 Step 5: Setting environment variables...');
        
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
                await functions.createVariable(functionData.$id, key, value);
                console.log(`✅ Set ${key}`);
            } catch (error) {
                if (error.code === 409) {
                    // Variable exists, update it
                    await functions.updateVariable(functionData.$id, key, key, value);
                    console.log(`✅ Updated ${key}`);
                } else {
                    console.error(`❌ Error setting ${key}:`, error.message);
                }
            }
        }
        
        // Clean up
        fs.unlinkSync(tarPath);
        
        console.log('\n✅ Function created and deployed successfully!');
        console.log(`\n🎯 Function ID: ${functionData.$id}`);
        console.log('📅 Schedule: Daily at 2:00 AM (UTC)');
        console.log('⏱️  Timeout: 15 minutes');
        console.log('\nThe crawler will start automatically according to the schedule.');
        console.log('You can also trigger it manually from the Appwrite Console.');
        
    } catch (error) {
        console.error('\n❌ Function creation failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run setup
createFunction();