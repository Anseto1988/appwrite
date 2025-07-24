const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function debugFunction() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔍 Debugging Dog Food Crawler Function...\n');
    
    try {
        // 1. Get function details
        console.log('📋 Step 1: Checking function configuration...');
        const functionData = await functions.get('dog-food-crawler');
        
        console.log('Function Details:');
        console.log(`- ID: ${functionData.$id}`);
        console.log(`- Name: ${functionData.name}`);
        console.log(`- Runtime: ${functionData.runtime}`);
        console.log(`- Enabled: ${functionData.enabled}`);
        console.log(`- Logging: ${functionData.logging}`);
        console.log(`- Schedule: ${functionData.schedule || 'Not set'}`);
        console.log(`- Timeout: ${functionData.timeout}s`);
        console.log(`- Deployment: ${functionData.deployment || 'No active deployment'}`);
        
        // 2. Check if deployment exists
        console.log('\n📋 Step 2: Checking deployments...');
        const deployments = await functions.listDeployments('dog-food-crawler');
        
        console.log(`Total deployments: ${deployments.total}`);
        
        if (deployments.total === 0) {
            console.log('❌ NO DEPLOYMENTS FOUND! The function code needs to be deployed.');
            console.log('\nTo fix:');
            console.log('1. Upload dog-food-crawler.tar.gz in Appwrite Console');
            console.log('2. Set entrypoint: src/index.js');
            console.log('3. Set build command: npm install');
            return;
        }
        
        // Show deployment details
        deployments.deployments.forEach((dep, index) => {
            console.log(`\nDeployment ${index + 1}:`);
            console.log(`- ID: ${dep.$id}`);
            console.log(`- Status: ${dep.status}`);
            console.log(`- Build Status: ${dep.buildStatus || 'Unknown'}`);
            console.log(`- Active: ${dep.activate}`);
            console.log(`- Created: ${dep.$createdAt}`);
            console.log(`- Size: ${dep.size} bytes`);
            console.log(`- Build Time: ${dep.buildTime}s`);
            
            if (dep.buildStatus === 'failed') {
                console.log(`- Build Error: ${dep.buildLogs || 'No logs available'}`);
            }
        });
        
        // 3. Check environment variables
        console.log('\n📋 Step 3: Checking environment variables...');
        const variables = await functions.listVariables('dog-food-crawler');
        
        console.log(`Total variables: ${variables.total}`);
        
        const requiredVars = [
            'APPWRITE_ENDPOINT',
            'APPWRITE_FUNCTION_PROJECT_ID',
            'DATABASE_ID',
            'SUBMISSIONS_COLLECTION_ID',
            'CRAWL_STATE_COLLECTION_ID',
            'CRAWLER_USER_ID',
            'MAX_PRODUCTS_PER_RUN',
            'APPWRITE_API_KEY'
        ];
        
        const foundVars = variables.variables.map(v => v.key);
        const missingVars = requiredVars.filter(v => !foundVars.includes(v));
        
        if (missingVars.length > 0) {
            console.log('❌ Missing required variables:');
            missingVars.forEach(v => console.log(`  - ${v}`));
        } else {
            console.log('✅ All required variables are set');
        }
        
        // 4. Check recent executions
        console.log('\n📋 Step 4: Checking recent executions...');
        const executions = await functions.listExecutions(
            'dog-food-crawler',
            [], // queries
            '' // search
        );
        
        console.log(`Total executions: ${executions.total}`);
        
        if (executions.total > 0) {
            console.log('\nRecent executions:');
            executions.executions.slice(0, 5).forEach((exec, index) => {
                console.log(`\nExecution ${index + 1}:`);
                console.log(`- ID: ${exec.$id}`);
                console.log(`- Status: ${exec.status}`);
                console.log(`- Created: ${exec.$createdAt}`);
                console.log(`- Duration: ${exec.duration}s`);
                console.log(`- Response Code: ${exec.responseStatusCode}`);
                
                if (exec.responseBody) {
                    console.log(`- Response: ${exec.responseBody.substring(0, 200)}...`);
                }
                
                if (exec.errors) {
                    console.log(`- Errors: ${exec.errors}`);
                }
                
                if (exec.logs) {
                    console.log(`- Logs Preview: ${exec.logs.substring(0, 200)}...`);
                }
            });
        }
        
        // 5. Test execution
        console.log('\n📋 Step 5: Creating test execution...');
        console.log('Starting execution...');
        
        const execution = await functions.createExecution(
            'dog-food-crawler',
            JSON.stringify({ test: true }),
            false // synchronous
        );
        
        console.log(`\nExecution created: ${execution.$id}`);
        console.log(`Status: ${execution.status}`);
        console.log(`Response Code: ${execution.responseStatusCode}`);
        
        if (execution.responseBody) {
            console.log(`Response: ${execution.responseBody}`);
        }
        
        if (execution.errors) {
            console.log(`Errors: ${execution.errors}`);
        }
        
        if (execution.logs) {
            console.log(`\nLogs:\n${execution.logs}`);
        } else {
            console.log('\n⚠️  No logs available. Possible issues:');
            console.log('- Function logging might be disabled');
            console.log('- Deployment might have failed');
            console.log('- Function might be crashing before logging');
        }
        
        // Summary
        console.log('\n📊 Summary:');
        if (deployments.total === 0) {
            console.log('❌ No deployment found - Upload code first!');
        } else if (missingVars.length > 0) {
            console.log('❌ Missing environment variables');
        } else if (!functionData.enabled) {
            console.log('❌ Function is disabled');
        } else if (!execution.logs && execution.status === 'failed') {
            console.log('❌ Function is failing - Check deployment build logs');
        } else {
            console.log('✅ Function appears to be configured correctly');
        }
        
    } catch (error) {
        console.error('\n❌ Debug failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run debug
debugFunction();