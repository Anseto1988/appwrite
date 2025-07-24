const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function checkExecutionStatus() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('📊 Checking Execution Status\n');
    
    try {
        // Get function info
        const func = await functions.get('dog-food-crawler');
        console.log(`Function: ${func.name}`);
        console.log(`Deployment: ${func.deployment || 'STILL NO ACTIVE DEPLOYMENT!'}\n`);
        
        // List recent executions
        const executions = await functions.listExecutions('dog-food-crawler', [], '', 10);
        
        console.log(`Total executions: ${executions.total}`);
        console.log('\nLast 10 executions:\n');
        
        executions.executions.forEach((exec, index) => {
            console.log(`${index + 1}. Execution ${exec.$id}`);
            console.log(`   Created: ${exec.$createdAt}`);
            console.log(`   Status: ${exec.status}`);
            console.log(`   Duration: ${exec.duration}s`);
            console.log(`   Response Code: ${exec.responseStatusCode || 'None'}`);
            
            if (exec.errors) {
                console.log(`   ❌ Errors: ${exec.errors.substring(0, 100)}...`);
            }
            
            if (exec.logs) {
                console.log(`   📋 Logs: Yes (${exec.logs.length} chars)`);
                // Show first few lines
                const firstLines = exec.logs.split('\n').slice(0, 3).join('\n');
                console.log(`      ${firstLines.replace(/\n/g, '\n      ')}`);
            } else {
                console.log(`   📋 Logs: None`);
            }
            
            if (exec.responseBody) {
                try {
                    const body = JSON.parse(exec.responseBody);
                    console.log(`   Response: ${JSON.stringify(body).substring(0, 100)}...`);
                } catch {
                    console.log(`   Response: ${exec.responseBody.substring(0, 100)}...`);
                }
            }
            
            console.log('');
        });
        
        // Check if any are processing
        const processing = executions.executions.filter(e => e.status === 'processing' || e.status === 'waiting');
        if (processing.length > 0) {
            console.log(`⏳ ${processing.length} executions are still processing/waiting`);
        }
        
        // Suggestions
        console.log('\n💡 Troubleshooting:');
        
        if (!func.deployment) {
            console.log('1. ❌ No active deployment detected');
            console.log('   - Check if deployment activation worked in Console');
            console.log('   - Try creating a NEW deployment');
            console.log('   - Make sure to check "Activate after build"');
        }
        
        const failedExecs = executions.executions.filter(e => e.status === 'failed');
        if (failedExecs.length > 0) {
            console.log(`2. ❌ ${failedExecs.length} failed executions`);
            console.log('   - Check error messages above');
            console.log('   - Look for build/deployment issues');
        }
        
        console.log('\n📌 Direct Console Links:');
        console.log(`Function: https://parse.nordburglarp.de/console/project-${PROJECT_ID}/functions/function-${func.$id}`);
        console.log(`Deployments: https://parse.nordburglarp.de/console/project-${PROJECT_ID}/functions/function-${func.$id}/deployments`);
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run check
checkExecutionStatus();