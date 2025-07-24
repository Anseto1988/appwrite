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
    
    console.log('🔧 Attempting to Activate Deployment\n');
    
    try {
        // 1. Get function
        const func = await functions.get('dog-food-crawler');
        console.log(`Function: ${func.name}`);
        console.log(`Current deployment: ${func.deployment || 'None'}`);
        
        // 2. List deployments
        console.log('\nChecking deployments...');
        const deployments = await functions.listDeployments('dog-food-crawler');
        
        if (deployments.total === 0) {
            console.log('❌ No deployments found!');
            return;
        }
        
        // Find a ready deployment
        const readyDeployment = deployments.deployments.find(d => d.status === 'ready');
        
        if (!readyDeployment) {
            console.log('❌ No ready deployments found!');
            return;
        }
        
        console.log(`\nFound ready deployment: ${readyDeployment.$id}`);
        console.log(`Created: ${readyDeployment.$createdAt}`);
        console.log(`Status: ${readyDeployment.status}`);
        
        // Try using updateDeploymentStatus
        console.log('\nAttempting to activate via updateDeploymentStatus...');
        try {
            await functions.updateDeploymentStatus(
                'dog-food-crawler',
                readyDeployment.$id,
                'active'
            );
            console.log('✅ Deployment status updated!');
        } catch (e) {
            console.log('❌ updateDeploymentStatus failed:', e.message);
        }
        
        // Try using updateFunctionDeployment
        console.log('\nAttempting to activate via updateFunctionDeployment...');
        try {
            await functions.updateFunctionDeployment(
                'dog-food-crawler',
                readyDeployment.$id
            );
            console.log('✅ Function deployment updated!');
        } catch (e) {
            console.log('❌ updateFunctionDeployment failed:', e.message);
        }
        
        // Try using update method
        console.log('\nAttempting to update function with deployment...');
        try {
            await functions.update(
                'dog-food-crawler',
                func.name,
                {
                    deployment: readyDeployment.$id
                }
            );
            console.log('✅ Function updated with deployment!');
        } catch (e) {
            console.log('❌ Function update failed:', e.message);
        }
        
        // Check if activation worked
        console.log('\nChecking if activation worked...');
        const updatedFunc = await functions.get('dog-food-crawler');
        
        if (updatedFunc.deployment) {
            console.log(`\n✅ SUCCESS! Active deployment: ${updatedFunc.deployment}`);
        } else {
            console.log('\n❌ Still no active deployment!');
            console.log('\n⚠️  MANUAL ACTION REQUIRED:');
            console.log('1. Go to: https://parse.nordburglarp.de/console');
            console.log('2. Navigate to: Functions → dog-food-crawler → Deployments');
            console.log(`3. Find deployment ${readyDeployment.$id}`);
            console.log('4. Click the three dots menu (⋮)');
            console.log('5. Select "Activate"');
            console.log('\nThe Appwrite API does not seem to support programmatic deployment activation.');
        }
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
activateDeployment();