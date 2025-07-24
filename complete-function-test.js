const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function runCompleteTest() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔍 Complete Function Test and Execution\n');
    console.log('=' . repeat(50));
    
    try {
        // 1. Get function status
        console.log('\n1️⃣ FUNCTION STATUS CHECK');
        console.log('-' . repeat(30));
        
        const func = await functions.get('dog-food-crawler');
        console.log(`✓ Function Name: ${func.name}`);
        console.log(`✓ Runtime: ${func.runtime}`);
        console.log(`✓ Enabled: ${func.enabled ? '✅ Yes' : '❌ No'}`);
        console.log(`✓ Schedule: ${func.schedule || 'None'}`);
        console.log(`✓ Timeout: ${func.timeout}s`);
        console.log(`✓ Active Deployment: ${func.deployment || 'None'}`);
        
        // 2. Check latest deployment
        console.log('\n2️⃣ DEPLOYMENT CHECK');
        console.log('-' . repeat(30));
        
        const deployments = await functions.listDeployments('dog-food-crawler');
        if (deployments.total > 0) {
            const latest = deployments.deployments[0];
            console.log(`✓ Latest Deployment ID: ${latest.$id}`);
            console.log(`✓ Status: ${latest.status}`);
            console.log(`✓ Created: ${latest.$createdAt}`);
            console.log(`✓ Active: ${latest.activate ? '✅' : '❌'}`);
            
            // Activate latest deployment if not active
            if (func.deployment !== latest.$id) {
                console.log('\n⚠️  Latest deployment is not active. Activating...');
                await functions.updateDeployment(
                    'dog-food-crawler',
                    latest.$id,
                    true // activate
                );
                console.log('✅ Deployment activated');
            }
        } else {
            console.log('❌ No deployments found!');
            return;
        }
        
        // 3. Verify environment variables
        console.log('\n3️⃣ ENVIRONMENT VARIABLES CHECK');
        console.log('-' . repeat(30));
        
        const variables = await functions.listVariables('dog-food-crawler');
        const requiredVars = [
            'APPWRITE_ENDPOINT',
            'APPWRITE_FUNCTION_PROJECT_ID',
            'DATABASE_ID',
            'APPWRITE_API_KEY'
        ];
        
        const foundVars = variables.variables.map(v => v.key);
        let allVarsPresent = true;
        
        requiredVars.forEach(varName => {
            const exists = foundVars.includes(varName);
            console.log(`${exists ? '✅' : '❌'} ${varName}`);
            if (!exists) allVarsPresent = false;
        });
        
        if (!allVarsPresent) {
            console.log('\n❌ Missing required variables!');
            return;
        }
        
        // 4. Create new async execution
        console.log('\n4️⃣ CREATING NEW EXECUTION');
        console.log('-' . repeat(30));
        
        const executionData = {
            test: true,
            limit: 5,
            source: 'test'
        };
        
        console.log('Execution data:', JSON.stringify(executionData));
        console.log('Creating async execution...');
        
        const execution = await functions.createExecution(
            'dog-food-crawler',
            JSON.stringify(executionData),
            true // async
        );
        
        console.log(`\n✅ Execution created: ${execution.$id}`);
        console.log(`Initial status: ${execution.status}`);
        
        // 5. Monitor execution
        console.log('\n5️⃣ MONITORING EXECUTION');
        console.log('-' . repeat(30));
        
        let attempts = 0;
        let maxAttempts = 30; // 30 seconds max
        let finalStatus = null;
        
        while (attempts < maxAttempts) {
            await new Promise(resolve => setTimeout(resolve, 1000)); // Wait 1 second
            
            const status = await functions.getExecution('dog-food-crawler', execution.$id);
            
            if (status.status !== 'waiting' && status.status !== 'processing') {
                finalStatus = status;
                break;
            }
            
            process.stdout.write(`\rStatus: ${status.status} (${attempts + 1}s)`);
            attempts++;
        }
        
        console.log('\n');
        
        if (finalStatus) {
            console.log(`Final Status: ${finalStatus.status}`);
            console.log(`Duration: ${finalStatus.duration}s`);
            console.log(`Response Code: ${finalStatus.responseStatusCode || 'N/A'}`);
            
            if (finalStatus.responseBody) {
                console.log('\nResponse Body:');
                try {
                    const response = JSON.parse(finalStatus.responseBody);
                    console.log(JSON.stringify(response, null, 2));
                } catch {
                    console.log(finalStatus.responseBody.substring(0, 500));
                }
            }
            
            if (finalStatus.errors) {
                console.log('\n❌ Errors:');
                console.log(finalStatus.errors);
            }
            
            if (finalStatus.logs) {
                console.log('\n📋 Logs:');
                console.log('-' . repeat(30));
                console.log(finalStatus.logs);
            } else {
                console.log('\n⚠️  No logs available');
            }
        } else {
            console.log('⏱️  Execution still running after 30 seconds');
        }
        
        // 6. List recent executions
        console.log('\n6️⃣ RECENT EXECUTIONS');
        console.log('-' . repeat(30));
        
        const recentExecutions = await functions.listExecutions(
            'dog-food-crawler',
            [],
            '',
            5 // limit
        );
        
        console.log(`Total executions: ${recentExecutions.total}`);
        console.log('\nLast 5 executions:');
        
        recentExecutions.executions.forEach((exec, index) => {
            console.log(`\n${index + 1}. ${exec.$id}`);
            console.log(`   Status: ${exec.status}`);
            console.log(`   Created: ${exec.$createdAt}`);
            console.log(`   Duration: ${exec.duration}s`);
            
            if (exec.errors) {
                console.log(`   Errors: ${exec.errors.substring(0, 100)}...`);
            }
        });
        
        // 7. Summary
        console.log('\n\n7️⃣ SUMMARY');
        console.log('=' . repeat(50));
        
        if (finalStatus) {
            if (finalStatus.status === 'completed') {
                console.log('✅ Function executed successfully!');
            } else if (finalStatus.status === 'failed') {
                console.log('❌ Function execution failed');
                console.log('Check errors and logs above for details');
            }
        } else {
            console.log('⏱️  Function is still processing or timed out');
        }
        
        console.log('\n💡 Next steps:');
        console.log('1. Check Appwrite Console for detailed logs');
        console.log('2. Review any error messages above');
        console.log('3. Verify deployment build was successful');
        console.log(`4. Direct link: ${ENDPOINT.replace('/v1', '')}/console/project-${PROJECT_ID}/functions/function-${func.$id}`);
        
    } catch (error) {
        console.error('\n❌ Test failed:', error);
        console.error('Error details:', error.response?.data || error.message);
        
        if (error.code === 404) {
            console.error('\n⚠️  Function not found. Make sure "dog-food-crawler" function exists.');
        }
    }
}

// Run the complete test
console.log('Starting complete function test...\n');
runCompleteTest().catch(console.error);