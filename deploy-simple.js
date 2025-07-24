const sdk = require('node-appwrite');
const fs = require('fs');
const path = require('path');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function deploySimple() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Simple Deployment Attempt\n');
    
    try {
        // Read tar.gz file
        const tarPath = path.join(__dirname, 'dog-food-crawler.tar.gz');
        const fileBuffer = fs.readFileSync(tarPath);
        
        console.log(`File loaded: ${fileBuffer.length} bytes`);
        
        // Try direct deployment with buffer
        console.log('Creating deployment...');
        
        const deployment = await functions.createDeployment(
            'dog-food-crawler',
            fileBuffer,  // Pass buffer directly
            true,  // activate
            'src/index.js', // entrypoint
            'npm install' // commands
        );
        
        console.log('✅ Deployment created!');
        console.log(`ID: ${deployment.$id}`);
        console.log(`Status: ${deployment.status}`);
        
    } catch (error) {
        console.error('❌ Error:', error.message);
        
        // Try alternative approach
        console.log('\nTrying alternative approach...');
        
        try {
            // Create a File-like object
            const file = {
                stream: fs.createReadStream(tarPath),
                size: fs.statSync(tarPath).size,
                filename: 'dog-food-crawler.tar.gz'
            };
            
            const deployment = await functions.createDeployment(
                'dog-food-crawler',
                file,
                true,
                'src/index.js',
                'npm install'
            );
            
            console.log('✅ Alternative approach worked!');
            console.log(`ID: ${deployment.$id}`);
            
        } catch (error2) {
            console.error('❌ Alternative also failed:', error2.message);
            
            console.log('\n💡 Manual Upload Required:');
            console.log('1. The SDK does not support file uploads properly');
            console.log('2. Please use Appwrite Console to upload dog-food-crawler.tar.gz');
            console.log('3. Direct link: https://parse.nordburglarp.de/console/project-snackrack2/functions/function-dog-food-crawler/deployments');
            console.log('4. Click "Create deployment" and upload the tar.gz file');
            console.log('5. Make sure to check "Activate deployment after build"');
        }
    }
}

// Run
deploySimple();