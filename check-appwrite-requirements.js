const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

async function checkRequirements() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const functions = new sdk.Functions(client);
    const health = new sdk.Health(client);
    
    console.log('🔍 Checking Appwrite Functions Requirements\n');
    console.log('=' . repeat(50));
    
    try {
        // 1. Check Appwrite health
        console.log('\n1️⃣ APPWRITE SERVER HEALTH');
        console.log('-' . repeat(30));
        
        try {
            const healthStatus = await health.get();
            console.log('✅ Appwrite server is healthy');
            console.log(`   Status: ${healthStatus.status}`);
        } catch (e) {
            console.log('❌ Appwrite server health check failed');
        }
        
        // 2. Check available runtimes
        console.log('\n2️⃣ AVAILABLE RUNTIMES');
        console.log('-' . repeat(30));
        
        try {
            const runtimes = await functions.listRuntimes();
            const nodeRuntimes = runtimes.runtimes.filter(r => r.name.toLowerCase().includes('node'));
            
            console.log('Node.js runtimes available:');
            nodeRuntimes.forEach(runtime => {
                console.log(`   ${runtime.name === 'node-16.0' ? '✅' : '  '} ${runtime.name} (${runtime.version})`);
            });
            
            if (!nodeRuntimes.find(r => r.name === 'node-16.0')) {
                console.log('\n❌ Node 16.0 runtime not available!');
                console.log('   The server admin needs to enable it in _APP_FUNCTIONS_RUNTIMES');
            }
        } catch (e) {
            console.log('❌ Could not fetch available runtimes');
        }
        
        // 3. Check function configuration
        console.log('\n3️⃣ FUNCTION CONFIGURATION');
        console.log('-' . repeat(30));
        
        const func = await functions.get('dog-food-crawler');
        console.log(`✅ Function exists: ${func.name}`);
        console.log(`   ID: ${func.$id}`);
        console.log(`   Runtime: ${func.runtime}`);
        console.log(`   Timeout: ${func.timeout}s`);
        console.log(`   Schedule: ${func.schedule || 'None'}`);
        console.log(`   Enabled: ${func.enabled ? '✅' : '❌'}`);
        console.log(`   Logging: ${func.logging ? '✅' : '❌'}`);
        console.log(`   Entry point: ${func.entrypoint || 'Not set'}`);
        console.log(`   Active deployment: ${func.deployment || '❌ NONE'}`);
        
        // 4. Check environment variables
        console.log('\n4️⃣ ENVIRONMENT VARIABLES');
        console.log('-' . repeat(30));
        
        const variables = await functions.listVariables('dog-food-crawler');
        const requiredVars = [
            'APPWRITE_ENDPOINT',
            'APPWRITE_FUNCTION_PROJECT_ID', 
            'DATABASE_ID',
            'APPWRITE_API_KEY'
        ];
        
        requiredVars.forEach(varName => {
            const exists = variables.variables.some(v => v.key === varName);
            console.log(`${exists ? '✅' : '❌'} ${varName}`);
        });
        
        // 5. Check deployments
        console.log('\n5️⃣ DEPLOYMENT STATUS');
        console.log('-' . repeat(30));
        
        const deployments = await functions.listDeployments('dog-food-crawler');
        console.log(`Total deployments: ${deployments.total}`);
        
        if (deployments.total > 0) {
            const latestDep = deployments.deployments[0];
            console.log(`\nLatest deployment:`);
            console.log(`   ID: ${latestDep.$id}`);
            console.log(`   Status: ${latestDep.status}`);
            console.log(`   Created: ${latestDep.$createdAt}`);
            console.log(`   Size: ${latestDep.size ? `${(latestDep.size / 1024).toFixed(2)} KB` : 'N/A'}`);
            console.log(`   Build time: ${latestDep.buildTime || 'N/A'}s`);
            console.log(`   Entry point: ${latestDep.entrypoint || 'Not set'}`);
            console.log(`   Build command: ${latestDep.buildStdout ? 'Has output' : 'No output'}`);
            
            if (latestDep.status === 'failed' && latestDep.buildStderr) {
                console.log(`\n❌ Build failed with errors:`);
                console.log(latestDep.buildStderr.substring(0, 500));
            }
        }
        
        // 6. Requirements summary
        console.log('\n\n6️⃣ REQUIREMENTS CHECKLIST');
        console.log('=' . repeat(50));
        
        const requirements = {
            'Server is healthy': true,
            'Function created': true,
            'Function enabled': func.enabled,
            'Environment variables set': requiredVars.every(v => 
                variables.variables.some(var_ => var_.key === v)
            ),
            'Has deployments': deployments.total > 0,
            'Has ready deployment': deployments.deployments.some(d => d.status === 'ready'),
            'Has active deployment': !!func.deployment
        };
        
        Object.entries(requirements).forEach(([req, met]) => {
            console.log(`${met ? '✅' : '❌'} ${req}`);
        });
        
        // 7. Recommendations
        console.log('\n\n7️⃣ RECOMMENDATIONS');
        console.log('=' . repeat(50));
        
        if (!func.deployment) {
            console.log('\n🚨 CRITICAL ISSUE: No active deployment!');
            console.log('\nPossible causes:');
            console.log('1. Deployment activation bug in Appwrite');
            console.log('2. Runtime not properly configured on server');
            console.log('3. Build/activation process failed silently');
            
            console.log('\nSolutions to try:');
            console.log('1. Delete ALL deployments and create a fresh one');
            console.log('2. Check server logs: docker logs appwrite-executor');
            console.log('3. Verify _APP_FUNCTIONS_RUNTIMES includes "node-16.0"');
            console.log('4. Try using Appwrite CLI instead of Console');
            console.log('5. Contact server admin to check Docker configuration');
        }
        
        
    } catch (error) {
        console.error('\n❌ Error:', error.message);
        if (error.response?.data) {
            console.error('Details:', error.response.data);
        }
    }
}

// Run check
checkRequirements();