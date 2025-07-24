/**
 * Local test script for the dog food crawler
 * Run with: node test-local.js
 */

require('dotenv').config();

// Mock Appwrite request/response objects
const mockReq = {
    variables: {
        APPWRITE_ENDPOINT: process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v1',
        APPWRITE_FUNCTION_PROJECT_ID: process.env.APPWRITE_FUNCTION_PROJECT_ID || 'snackrack2',
        APPWRITE_API_KEY: process.env.APPWRITE_API_KEY,
        DATABASE_ID: process.env.DATABASE_ID || 'snacktrack-db',
        SUBMISSIONS_COLLECTION_ID: process.env.SUBMISSIONS_COLLECTION_ID || 'foodSubmissions',
        CRAWL_STATE_COLLECTION_ID: process.env.CRAWL_STATE_COLLECTION_ID || 'crawlState',
        CRAWLER_USER_ID: process.env.CRAWLER_USER_ID || 'system_crawler',
        MAX_PRODUCTS_PER_RUN: process.env.MAX_PRODUCTS_PER_RUN || '10' // Limited for testing
    },
    payload: {}
};

const mockRes = {
    json: (data) => {
        console.log('\n=== Function Response ===');
        console.log(JSON.stringify(data, null, 2));
        console.log('========================\n');
    },
    send: (data) => {
        console.log('\n=== Function Response ===');
        console.log(data);
        console.log('========================\n');
    }
};

// Check for API key
if (!mockReq.variables.APPWRITE_API_KEY) {
    console.error('ERROR: APPWRITE_API_KEY is required');
    console.error('Please create a .env file with your Appwrite API key');
    process.exit(1);
}

// Import and run the function
console.log('Starting local test of dog food crawler...\n');
console.log('Configuration:');
console.log(`- Endpoint: ${mockReq.variables.APPWRITE_ENDPOINT}`);
console.log(`- Project: ${mockReq.variables.APPWRITE_FUNCTION_PROJECT_ID}`);
console.log(`- Max Products: ${mockReq.variables.MAX_PRODUCTS_PER_RUN}`);
console.log('\n');

const crawlerFunction = require('./src/index');

// Run the function
crawlerFunction(mockReq, mockRes)
    .then(() => {
        console.log('Test completed successfully');
        process.exit(0);
    })
    .catch((error) => {
        console.error('Test failed:', error);
        process.exit(1);
    });