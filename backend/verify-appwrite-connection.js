#!/usr/bin/env node
/**
 * Appwrite Connection Verification Script
 * Tests the connection to the Appwrite backend using environment variables
 */

const { Client, Databases, Storage, Teams, Users, Health } = require('node-appwrite');

// Load environment variables
require('dotenv').config({ path: '../.env' });

const APPWRITE_ENDPOINT = process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v2';
const APPWRITE_PROJECT_ID = process.env.APPWRITE_PROJECT_ID;
const APPWRITE_API_KEY = process.env.APPWRITE_API_KEY;

if (!APPWRITE_PROJECT_ID || !APPWRITE_API_KEY) {
    console.error('❌ Missing required environment variables!');
    console.error('Please ensure APPWRITE_PROJECT_ID and APPWRITE_API_KEY are set in your .env file');
    process.exit(1);
}

// Initialize Appwrite client
const client = new Client()
    .setEndpoint(APPWRITE_ENDPOINT)
    .setProject(APPWRITE_PROJECT_ID)
    .setKey(APPWRITE_API_KEY);

// Initialize services
const databases = new Databases(client);
const storage = new Storage(client);
const teams = new Teams(client);
const users = new Users(client);
const health = new Health(client);

async function verifyConnection() {
    console.log('🔍 Verifying Appwrite Connection...');
    console.log(`📍 Endpoint: ${APPWRITE_ENDPOINT}`);
    console.log(`🏗️ Project ID: ${APPWRITE_PROJECT_ID}`);
    console.log(`🔑 API Key: ${APPWRITE_API_KEY.substring(0, 20)}...`);
    console.log('');

    try {
        // Test 1: Check API health
        console.log('1️⃣ Checking API Health...');
        const apiHealth = await health.get();
        console.log('✅ API Health:', apiHealth.status);
        
        // Test 2: Check database health
        console.log('\n2️⃣ Checking Database Health...');
        const dbHealth = await health.getDB();
        console.log('✅ Database Health:', dbHealth.status);
        
        // Test 3: Check storage health
        console.log('\n3️⃣ Checking Storage Health...');
        const storageHealth = await health.getStorageLocal();
        console.log('✅ Storage Health:', storageHealth.status);
        
        // Test 4: List databases
        console.log('\n4️⃣ Listing Databases...');
        try {
            const databasesList = await databases.list();
            console.log(`✅ Found ${databasesList.total} databases`);
            databasesList.databases.forEach(db => {
                console.log(`   - ${db.name} (${db.$id})`);
            });
        } catch (error) {
            console.log('⚠️ Could not list databases (might need additional permissions)');
        }
        
        // Test 5: List storage buckets
        console.log('\n5️⃣ Listing Storage Buckets...');
        try {
            const bucketsList = await storage.listBuckets();
            console.log(`✅ Found ${bucketsList.total} storage buckets`);
            bucketsList.buckets.forEach(bucket => {
                console.log(`   - ${bucket.name} (${bucket.$id})`);
            });
        } catch (error) {
            console.log('⚠️ Could not list storage buckets (might need additional permissions)');
        }
        
        // Test 6: Check users
        console.log('\n6️⃣ Checking Users Service...');
        try {
            const usersList = await users.list(['limit(1)']);
            console.log(`✅ Users service accessible (${usersList.total} users)`);
        } catch (error) {
            console.log('⚠️ Could not access users (might need additional permissions)');
        }
        
        console.log('\n✅ Connection verification completed successfully!');
        console.log('🎉 Appwrite backend is reachable and operational.');
        
    } catch (error) {
        console.error('\n❌ Connection verification failed!');
        console.error('Error:', error.message);
        if (error.code) {
            console.error('Error Code:', error.code);
        }
        if (error.type) {
            console.error('Error Type:', error.type);
        }
        process.exit(1);
    }
}

// Run verification
verifyConnection();