const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function activateDeployment() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔧 Fixing Deployment Issue\n');
    
    try {
        // 1. Get function
        const func = await functions.get('dog-food-crawler');
        console.log(`Function: ${func.name}`);
        console.log(`Current active deployment: ${func.deployment || 'None'}`);
        
        // 2. List deployments
        console.log('\nListing deployments...');
        const deployments = await functions.listDeployments('dog-food-crawler');
        
        if (deployments.total === 0) {
            console.log('❌ No deployments found!');
            console.log('\nYou need to:');
            console.log('1. Go to Appwrite Console');
            console.log('2. Upload dog-food-crawler.tar.gz');
            console.log('3. Create a deployment');
            return;
        }
        
        console.log(`Found ${deployments.total} deployments:\n`);
        
        deployments.deployments.forEach((dep, index) => {
            console.log(`${index + 1}. Deployment ${dep.$id}`);
            console.log(`   Status: ${dep.status}`);
            console.log(`   Created: ${dep.$createdAt}`);
            console.log(`   Activated: ${dep.activate ? 'Yes' : 'No'}`);
            console.log(`   Build Status: ${dep.buildStatus || 'Unknown'}`);
            
            if (dep.buildLogs) {
                console.log(`   Build Logs: ${dep.buildLogs.substring(0, 100)}...`);
            }
            console.log('');
        });
        
        // Find a ready deployment
        const readyDeployment = deployments.deployments.find(d => d.status === 'ready');
        
        if (!readyDeployment) {
            console.log('❌ No ready deployments found!');
            console.log('All deployments might have failed during build.');
            return;
        }
        
        console.log(`\n🎯 Solution: Need to activate deployment ${readyDeployment.$id}`);
        console.log('\nTo activate this deployment:');
        console.log('1. Go to: https://parse.nordburglarp.de/console');
        console.log('2. Navigate to: Functions → dog-food-crawler');
        console.log('3. Go to "Deployments" tab');
        console.log(`4. Find deployment ${readyDeployment.$id}`);
        console.log('5. Click the three dots menu (⋮)');
        console.log('6. Select "Activate"');
        console.log('\nOr create a new deployment with the fixed code.');
        
        // Try to create a new execution anyway
        console.log('\n\nTrying to create execution with specific deployment...');
        
        // Note: The SDK doesn't support specifying deployment in execution
        // But let's try anyway
        const execution = await functions.createExecution(
            'dog-food-crawler',
            JSON.stringify({ test: true }),
            true
        );
        
        console.log(`\nExecution created: ${execution.$id}`);
        console.log('Check if it runs with the inactive deployment.');
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
activateDeployment();