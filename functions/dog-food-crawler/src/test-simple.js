// Simple test function to verify deployment works
module.exports = async function(req, res) {
    console.log('🚀 Test function started!');
    console.log('Environment check:');
    console.log('- NODE_VERSION:', process.version);
    console.log('- APPWRITE_FUNCTION_ID:', process.env.APPWRITE_FUNCTION_ID);
    console.log('- APPWRITE_FUNCTION_NAME:', process.env.APPWRITE_FUNCTION_NAME);
    
    try {
        // Check if required env vars exist
        const requiredVars = [
            'APPWRITE_ENDPOINT',
            'APPWRITE_FUNCTION_PROJECT_ID',
            'DATABASE_ID',
            'SUBMISSIONS_COLLECTION_ID',
            'CRAWL_STATE_COLLECTION_ID',
            'CRAWLER_USER_ID',
            'MAX_PRODUCTS_PER_RUN',
            'APPWRITE_API_KEY'
        ];
        
        console.log('\nEnvironment variables:');
        for (const varName of requiredVars) {
            const exists = !!process.env[varName];
            console.log(`- ${varName}: ${exists ? '✅ Set' : '❌ Missing'}`);
        }
        
        // Test basic functionality
        console.log('\nTesting basic imports...');
        const sdk = require('node-appwrite');
        console.log('✅ node-appwrite loaded');
        
        const axios = require('axios');
        console.log('✅ axios loaded');
        
        const cheerio = require('cheerio');
        console.log('✅ cheerio loaded');
        
        // Return success
        console.log('\n✅ All basic tests passed!');
        
        res.json({
            success: true,
            message: 'Test function working correctly',
            timestamp: new Date().toISOString(),
            environment: {
                nodeVersion: process.version,
                functionId: process.env.APPWRITE_FUNCTION_ID,
                functionName: process.env.APPWRITE_FUNCTION_NAME
            }
        });
        
    } catch (error) {
        console.error('❌ Test failed:', error);
        res.json({
            success: false,
            error: error.message,
            stack: error.stack
        });
    }
};