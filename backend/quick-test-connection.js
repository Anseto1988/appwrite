#!/usr/bin/env node
/**
 * Quick Connection Test
 * Can be used with command line arguments if environment variables are not set
 */

const { Client, Health } = require('node-appwrite');

// Get values from environment or command line
const ENDPOINT = process.argv[2] || process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v2';
const PROJECT_ID = process.argv[3] || process.env.APPWRITE_PROJECT_ID;
const API_KEY = process.argv[4] || process.env.APPWRITE_API_KEY;

if (!PROJECT_ID || !API_KEY) {
    console.log('Usage: node quick-test-connection.js [endpoint] <project_id> <api_key>');
    console.log('Or set APPWRITE_PROJECT_ID and APPWRITE_API_KEY environment variables');
    process.exit(1);
}

console.log('Testing connection to Appwrite...');
console.log(`Endpoint: ${ENDPOINT}`);
console.log(`Project ID: ${PROJECT_ID}`);
console.log(`API Key: ${API_KEY.substring(0, 20)}...`);

const client = new Client()
    .setEndpoint(ENDPOINT)
    .setProject(PROJECT_ID)
    .setKey(API_KEY);

const health = new Health(client);

async function testConnection() {
    try {
        const result = await health.get();
        console.log('\n✅ Connection successful!');
        console.log('API Status:', result.status);
        return true;
    } catch (error) {
        console.error('\n❌ Connection failed!');
        console.error('Error:', error.message);
        if (error.code) console.error('Code:', error.code);
        if (error.type) console.error('Type:', error.type);
        return false;
    }
}

testConnection();