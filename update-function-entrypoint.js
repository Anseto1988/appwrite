const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function updateEntrypoint() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    
    console.log('🔧 Creating test deployment...\n');
    
    try {
        // First create the test tar.gz
        const { execSync } = require('child_process');
        
        console.log('Creating test deployment package...');
        execSync('cd functions/dog-food-crawler && tar -czf ../../dog-food-crawler-test.tar.gz .');
        console.log('✅ Created dog-food-crawler-test.tar.gz');
        
        console.log('\n📋 Next steps for testing:');
        console.log('1. Upload dog-food-crawler-test.tar.gz to Appwrite Console');
        console.log('2. Set entrypoint to: src/test-simple.js');
        console.log('3. Set build command to: npm install');
        console.log('4. Deploy and test execution');
        console.log('\nThis will help identify if the issue is with:');
        console.log('- The deployment process');
        console.log('- Missing dependencies');
        console.log('- Environment variables');
        console.log('- The main crawler code');
        
        // Also create a minimal test function
        const minimalTest = `
// Minimal test without any dependencies
module.exports = async function(req, res) {
    console.log('Minimal test function started');
    res.json({
        success: true,
        message: 'Minimal test working',
        timestamp: new Date().toISOString()
    });
};`;
        
        require('fs').writeFileSync('functions/dog-food-crawler/src/minimal-test.js', minimalTest);
        console.log('\nAlso created src/minimal-test.js for testing without dependencies');
        
    } catch (error) {
        console.error('\n❌ Failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run update
updateEntrypoint();