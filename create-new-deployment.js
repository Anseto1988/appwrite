const sdk = require('node-appwrite');
const fs = require('fs');
const path = require('path');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function createNewDeployment() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Creating New Deployment\n');
    
    try {
        // 1. Get function
        const func = await functions.get('dog-food-crawler');
        console.log(`Function: ${func.name}`);
        console.log(`Current deployment: ${func.deployment || 'None'}`);
        console.log(`Runtime: ${func.runtime}`);
        
        // 2. Check if tar.gz exists
        const tarPath = path.join(__dirname, 'dog-food-crawler.tar.gz');
        if (!fs.existsSync(tarPath)) {
            console.log('\n❌ dog-food-crawler.tar.gz not found!');
            console.log('Please ensure the deployment package exists.');
            return;
        }
        
        console.log('\n✅ Found deployment package: dog-food-crawler.tar.gz');
        const fileStats = fs.statSync(tarPath);
        console.log(`File size: ${(fileStats.size / 1024).toFixed(2)} KB`);
        
        // 3. Create deployment
        console.log('\nCreating new deployment...');
        console.log('This may take a few minutes...\n');
        
        // Read file as buffer
        const codeBuffer = fs.readFileSync(tarPath);
        
        // Create InputFile from buffer
        const code = sdk.InputFile.fromBuffer(codeBuffer, 'dog-food-crawler.tar.gz');
        
        const deployment = await functions.createDeployment(
            'dog-food-crawler',
            code,
            true,  // activate
            'src/index.js', // entrypoint
            'npm install' // commands
        );
        
        console.log('✅ Deployment created!');
        console.log(`Deployment ID: ${deployment.$id}`);
        console.log(`Status: ${deployment.status}`);
        console.log(`Activated: ${deployment.activate ? 'Yes' : 'No'}`);
        
        // 4. Monitor build progress
        console.log('\nMonitoring build progress...');
        let buildComplete = false;
        let attempts = 0;
        let maxAttempts = 60; // 5 minutes max
        
        while (!buildComplete && attempts < maxAttempts) {
            await new Promise(resolve => setTimeout(resolve, 5000)); // Wait 5 seconds
            
            const status = await functions.getDeployment('dog-food-crawler', deployment.$id);
            
            console.log(`Build status: ${status.status} (${(attempts + 1) * 5}s elapsed)`);
            
            if (status.status === 'ready' || status.status === 'failed') {
                buildComplete = true;
                
                if (status.status === 'ready') {
                    console.log('\n✅ Build completed successfully!');
                    
                    // Check if it's now the active deployment
                    const updatedFunc = await functions.get('dog-food-crawler');
                    if (updatedFunc.deployment === deployment.$id) {
                        console.log('✅ Deployment is now ACTIVE!');
                    } else {
                        console.log('⚠️  Deployment is ready but not active yet.');
                        
                        // Try to activate it
                        console.log('\nAttempting to activate...');
                        try {
                            await functions.updateFunctionDeployment('dog-food-crawler', deployment.$id);
                            console.log('✅ Activation request sent!');
                            
                            // Check again
                            const finalFunc = await functions.get('dog-food-crawler');
                            if (finalFunc.deployment === deployment.$id) {
                                console.log('✅ Deployment is now ACTIVE!');
                            } else {
                                console.log('❌ Still not active. Manual activation required.');
                            }
                        } catch (e) {
                            console.log('❌ Activation failed:', e.message);
                        }
                    }
                } else {
                    console.log('\n❌ Build failed!');
                    if (status.buildLogs) {
                        console.log('\nBuild logs:');
                        console.log(status.buildLogs);
                    }
                }
                
                break;
            }
            
            attempts++;
        }
        
        if (!buildComplete) {
            console.log('\n⏱️  Build is taking longer than expected.');
            console.log('Check Appwrite Console for status.');
        }
        
        // 5. Test execution
        if (buildComplete && deployment.status === 'ready') {
            console.log('\n📊 Testing execution...');
            
            const execution = await functions.createExecution(
                'dog-food-crawler',
                JSON.stringify({ test: true, limit: 1 }),
                true
            );
            
            console.log(`Execution created: ${execution.$id}`);
            console.log('Check Appwrite Console for results.');
        }
        
        console.log('\n📌 Direct links:');
        console.log(`Function: ${ENDPOINT.replace('/v1', '')}/console/project-${PROJECT_ID}/functions/function-${func.$id}`);
        console.log(`Deployment: ${ENDPOINT.replace('/v1', '')}/console/project-${PROJECT_ID}/functions/function-${func.$id}/deployment-${deployment.$id}`);
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run
createNewDeployment();