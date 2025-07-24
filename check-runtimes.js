const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function checkRuntimes() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Checking available runtimes...\n');
    
    try {
        // List available runtimes
        const runtimes = await functions.listRuntimes();
        
        console.log('Available runtimes:');
        runtimes.runtimes.forEach(runtime => {
            console.log(`- ${runtime.name} (${runtime.$id})`);
        });
        
    } catch (error) {
        console.error('Error:', error.message);
        
        // Try common runtime names
        console.log('\nTrying common runtime names...');
        const commonRuntimes = [
            'node-16.0',
            'node-17.0',
            'node-18.0',
            'node-19.0',
            'node-20.0',
            'nodejs-16.0',
            'nodejs-17.0',
            'nodejs-18.0',
            'nodejs-19.0',
            'nodejs-20.0'
        ];
        
        for (const runtime of commonRuntimes) {
            try {
                await functions.create(
                    'test-runtime-' + runtime,
                    'Test Runtime',
                    runtime
                );
                console.log(`✅ ${runtime} is supported`);
                // Delete test function
                await functions.delete('test-runtime-' + runtime);
            } catch (error) {
                if (error.code === 404) {
                    console.log(`❌ ${runtime} is not supported`);
                }
            }
        }
    }
}

// Run check
checkRuntimes();