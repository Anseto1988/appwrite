const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function simpleExecutionTest() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Simple Execution Test\n');
    
    try {
        // 1. Quick function check
        console.log('1. Checking function...');
        const func = await functions.get('dog-food-crawler');
        console.log(`✅ Function exists: ${func.name}`);
        console.log(`   Status: ${func.enabled ? 'Enabled' : 'Disabled'}`);
        console.log(`   Deployment: ${func.deployment || 'No active deployment!'}`);
        
        // 2. Create execution with minimal data
        console.log('\n2. Creating execution...');
        const execution = await functions.createExecution(
            'dog-food-crawler',
            '', // Empty data
            true // Async
        );
        
        console.log(`✅ Execution created: ${execution.$id}`);
        console.log(`   Initial status: ${execution.status}`);
        
        // 3. Wait for completion
        console.log('\n3. Waiting for execution to complete...');
        let checkCount = 0;
        let maxChecks = 60; // 60 seconds max
        let executionResult = null;
        
        while (checkCount < maxChecks) {
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            const current = await functions.getExecution('dog-food-crawler', execution.$id);
            
            process.stdout.write(`\r   Status: ${current.status} (${checkCount + 1}s elapsed)`);
            
            if (current.status === 'completed' || current.status === 'failed') {
                executionResult = current;
                break;
            }
            
            checkCount++;
        }
        
        console.log('\n');
        
        // 4. Show results
        if (executionResult) {
            console.log('4. Execution completed!');
            console.log(`   Final status: ${executionResult.status}`);
            console.log(`   Duration: ${executionResult.duration}s`);
            console.log(`   Response code: ${executionResult.responseStatusCode || 'None'}`);
            
            if (executionResult.responseBody) {
                console.log('\n📄 Response:');
                try {
                    const parsed = JSON.parse(executionResult.responseBody);
                    console.log(JSON.stringify(parsed, null, 2));
                } catch {
                    console.log(executionResult.responseBody);
                }
            }
            
            if (executionResult.errors) {
                console.log('\n❌ Errors:');
                console.log(executionResult.errors);
            }
            
            if (executionResult.logs) {
                console.log('\n📋 Logs:');
                console.log(executionResult.logs);
            } else {
                console.log('\n⚠️  No logs captured');
            }
            
        } else {
            console.log('⏱️  Execution did not complete within 60 seconds');
            console.log('   Check Appwrite Console for details');
        }
        
        // 5. Direct links
        console.log('\n📌 Direct links:');
        console.log(`Function: ${ENDPOINT.replace('/v1', '')}/console/project-${PROJECT_ID}/functions/function-${func.$id}`);
        console.log(`Execution: ${ENDPOINT.replace('/v1', '')}/console/project-${PROJECT_ID}/functions/function-${func.$id}/execution-${execution.$id}`);
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run test
simpleExecutionTest();