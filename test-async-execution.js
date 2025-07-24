const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function testAsyncExecution() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Testing Async Execution...\n');
    
    try {
        // Create async execution
        const execution = await functions.createExecution(
            'dog-food-crawler',
            JSON.stringify({ test: true, maxProducts: 5 }), // Limit for testing
            true // ASYNC = true
        );
        
        console.log(`✅ Execution started: ${execution.$id}`);
        console.log(`Status: ${execution.status}`);
        console.log(`\nTo check status and logs:`);
        console.log(`1. Go to: https://parse.nordburglarp.de/console`);
        console.log(`2. Functions → dog-food-crawler → Executions`);
        console.log(`3. Click on execution: ${execution.$id}`);
        
        // Wait and check status
        console.log('\nWaiting 5 seconds before checking status...');
        await new Promise(resolve => setTimeout(resolve, 5000));
        
        // Get execution details
        const executionDetails = await functions.getExecution(
            'dog-food-crawler',
            execution.$id
        );
        
        console.log('\nExecution Update:');
        console.log(`- Status: ${executionDetails.status}`);
        console.log(`- Duration: ${executionDetails.duration}s`);
        console.log(`- Response Code: ${executionDetails.responseStatusCode}`);
        
        if (executionDetails.responseBody) {
            console.log(`- Response: ${executionDetails.responseBody}`);
        }
        
        if (executionDetails.errors) {
            console.log(`- Errors: ${executionDetails.errors}`);
        }
        
        if (executionDetails.logs) {
            console.log(`\nLogs:\n${executionDetails.logs}`);
        }
        
        // List recent executions
        console.log('\n📋 Recent Executions:');
        const executions = await functions.listExecutions('dog-food-crawler');
        
        executions.executions.slice(0, 3).forEach((exec, index) => {
            console.log(`\n${index + 1}. Execution ${exec.$id}:`);
            console.log(`   Status: ${exec.status}`);
            console.log(`   Created: ${exec.$createdAt}`);
            console.log(`   Duration: ${exec.duration}s`);
            
            if (exec.logs) {
                const logLines = exec.logs.split('\n').slice(0, 3);
                console.log(`   Logs Preview:`);
                logLines.forEach(line => console.log(`     ${line}`));
            }
        });
        
    } catch (error) {
        console.error('\n❌ Test failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run test
testAsyncExecution();