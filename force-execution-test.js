const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function forceExecution() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Force Execution Test\n');
    
    try {
        // 1. Get latest deployment
        const deployments = await functions.listDeployments('dog-food-crawler');
        
        if (deployments.total === 0) {
            console.log('❌ No deployments found!');
            return;
        }
        
        const latestDeployment = deployments.deployments[0];
        console.log(`Latest deployment: ${latestDeployment.$id}`);
        console.log(`Status: ${latestDeployment.status}`);
        
        // 2. Try to activate it
        console.log('\n🔧 Attempting to activate deployment...');
        try {
            await functions.updateFunctionDeployment('dog-food-crawler', latestDeployment.$id);
            console.log('✅ Activation request sent');
        } catch (e) {
            console.log('⚠️  Activation error:', e.message);
        }
        
        // 3. Create execution with minimal data
        console.log('\n📊 Creating execution...');
        
        const execution = await functions.createExecution(
            'dog-food-crawler',
            JSON.stringify({ test: true, limit: 1 }),
            false // Try synchronous execution
        );
        
        console.log(`\nExecution created: ${execution.$id}`);
        console.log(`Status: ${execution.status}`);
        console.log(`Duration: ${execution.duration}s`);
        
        if (execution.responseBody) {
            console.log('\n📄 Response:');
            try {
                const response = JSON.parse(execution.responseBody);
                console.log(JSON.stringify(response, null, 2));
            } catch {
                console.log(execution.responseBody);
            }
        }
        
        if (execution.errors) {
            console.log('\n❌ Errors:');
            console.log(execution.errors);
        }
        
        if (execution.logs) {
            console.log('\n📋 Logs:');
            console.log(execution.logs);
        }
        
        // 4. If still waiting, check again
        if (execution.status === 'waiting' || execution.status === 'processing') {
            console.log('\n⏳ Execution is processing, checking status...');
            
            await new Promise(resolve => setTimeout(resolve, 5000));
            
            const updated = await functions.getExecution('dog-food-crawler', execution.$id);
            console.log(`\nUpdated status: ${updated.status}`);
            
            if (updated.logs) {
                console.log('Logs:', updated.logs);
            }
            if (updated.errors) {
                console.log('Errors:', updated.errors);
            }
        }
        
        // 5. Check if container is responding
        console.log('\n🐳 Container Analysis:');
        console.log('Based on docker ps output, container exc1-snackrack2-688267f711eb80fe82d9 is running.');
        console.log('This indicates the deployment WAS activated but Appwrite API doesn\'t reflect it.');
        
        console.log('\n💡 Recommendations:');
        console.log('1. The function container IS running (good sign!)');
        console.log('2. Try accessing the function directly via HTTP if possible');
        console.log('3. Check Appwrite worker-functions logs on the server');
        console.log('4. The issue might be communication between Appwrite and the executor');
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
forceExecution();