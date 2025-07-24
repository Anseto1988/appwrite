const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function checkDeploymentStatus() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔍 Checking Deployment Status\n');
    
    try {
        // Get function
        const func = await functions.get('dog-food-crawler');
        console.log(`Function: ${func.name}`);
        console.log(`Runtime: ${func.runtime}`);
        console.log(`Status: ${func.enabled ? 'Enabled' : 'Disabled'}`);
        console.log(`Active Deployment: ${func.deployment || 'NONE!'}`);
        
        // List all deployments
        console.log('\n📦 All Deployments:');
        console.log('==================\n');
        
        const deployments = await functions.listDeployments('dog-food-crawler');
        
        if (deployments.total === 0) {
            console.log('❌ No deployments found!');
            return;
        }
        
        deployments.deployments.forEach((dep, index) => {
            console.log(`${index + 1}. Deployment ${dep.$id}`);
            console.log(`   Created: ${dep.$createdAt}`);
            console.log(`   Status: ${dep.status}`);
            console.log(`   Activated: ${dep.activate ? 'Yes' : 'No'}`);
            console.log(`   Size: ${dep.size ? `${(dep.size / 1024).toFixed(2)} KB` : 'N/A'}`);
            
            if (dep.buildLogs && dep.status === 'failed') {
                console.log(`   ❌ Build Failed!`);
                console.log(`   Build Logs:\n${dep.buildLogs.substring(0, 500)}...`);
            }
            
            console.log('');
        });
        
        // Check for ready deployments
        const readyDeployments = deployments.deployments.filter(d => d.status === 'ready');
        
        if (readyDeployments.length > 0 && !func.deployment) {
            console.log('⚠️  PROBLEM IDENTIFIED:');
            console.log(`   - ${readyDeployments.length} deployment(s) are ready`);
            console.log('   - But NO deployment is active!');
            console.log('\n🔧 SOLUTION:');
            console.log('   1. Go to the Deployments tab in Appwrite Console');
            console.log(`   2. Find a "ready" deployment (e.g., ${readyDeployments[0].$id})`);
            console.log('   3. Click the three dots menu (⋮)');
            console.log('   4. Select "Activate"');
            
            // Try to activate programmatically
            console.log('\n🤖 Attempting automatic activation...');
            try {
                await functions.updateFunctionDeployment(
                    'dog-food-crawler',
                    readyDeployments[0].$id
                );
                console.log('✅ Activation request sent!');
                
                // Check if it worked
                const updatedFunc = await functions.get('dog-food-crawler');
                if (updatedFunc.deployment === readyDeployments[0].$id) {
                    console.log('✅ Deployment is now ACTIVE!');
                } else {
                    console.log('❌ Activation didn\'t work. Manual activation required.');
                }
            } catch (e) {
                console.log('❌ Automatic activation failed:', e.message);
                console.log('   Manual activation in Console required.');
            }
        }
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
checkDeploymentStatus();