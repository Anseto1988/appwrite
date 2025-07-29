#!/usr/bin/env node
/**
 * Appwrite Operations Test Script
 * Tests various CRUD operations to ensure the backend is properly configured
 */

const { Client, Databases, Storage, ID, Query } = require('node-appwrite');
require('dotenv').config({ path: '../.env' });

const APPWRITE_ENDPOINT = process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v2';
const APPWRITE_PROJECT_ID = process.env.APPWRITE_PROJECT_ID;
const APPWRITE_API_KEY = process.env.APPWRITE_API_KEY;
const DATABASE_ID = process.env.APPWRITE_DATABASE_ID || 'snacktrack-db';

const client = new Client()
    .setEndpoint(APPWRITE_ENDPOINT)
    .setProject(APPWRITE_PROJECT_ID)
    .setKey(APPWRITE_API_KEY);

const databases = new Databases(client);
const storage = new Storage(client);

// Test data
const testDog = {
    userId: 'test-user-123',
    name: 'Test Dog',
    breed: 'Golden Retriever',
    weight: 30.5,
    targetWeight: 28.0,
    dailyCalories: 1200,
    activityLevel: 'medium',
    isActive: true,
    notes: 'This is a test dog entry'
};

const testFood = {
    name: 'Test Dog Food',
    brand: 'Test Brand',
    barcode: '1234567890123',
    calories: 350.5,
    protein: 25.0,
    fat: 15.0,
    carbohydrates: 45.0,
    fiber: 5.0,
    moisture: 10.0,
    category: 'dry-food',
    isVerified: false,
    createdBy: 'test-user-123'
};

async function testDatabaseOperations() {
    console.log('\n🧪 Testing Database Operations\n');
    
    let createdDogId;
    let createdFoodId;
    
    try {
        // Test 1: Create a dog
        console.log('1️⃣ Creating a test dog...');
        const dog = await databases.createDocument(
            DATABASE_ID,
            'dogs',
            ID.unique(),
            testDog
        );
        createdDogId = dog.$id;
        console.log(`✅ Dog created successfully with ID: ${createdDogId}`);
        
        // Test 2: Create a food item
        console.log('\n2️⃣ Creating a test food item...');
        const food = await databases.createDocument(
            DATABASE_ID,
            'foods',
            ID.unique(),
            testFood
        );
        createdFoodId = food.$id;
        console.log(`✅ Food created successfully with ID: ${createdFoodId}`);
        
        // Test 3: List dogs
        console.log('\n3️⃣ Listing dogs...');
        const dogsList = await databases.listDocuments(
            DATABASE_ID,
            'dogs',
            [
                Query.equal('userId', 'test-user-123'),
                Query.limit(10)
            ]
        );
        console.log(`✅ Found ${dogsList.total} dogs for test user`);
        
        // Test 4: Update dog
        console.log('\n4️⃣ Updating test dog...');
        const updatedDog = await databases.updateDocument(
            DATABASE_ID,
            'dogs',
            createdDogId,
            {
                weight: 29.5,
                notes: 'Updated test dog entry'
            }
        );
        console.log(`✅ Dog updated successfully. New weight: ${updatedDog.weight}kg`);
        
        // Test 5: Create a feeding record
        console.log('\n5️⃣ Creating a feeding record...');
        const feeding = await databases.createDocument(
            DATABASE_ID,
            'feedings',
            ID.unique(),
            {
                dogId: createdDogId,
                userId: 'test-user-123',
                foodId: createdFoodId,
                amount: 200,
                unit: 'g',
                timestamp: new Date().toISOString(),
                mealType: 'breakfast',
                calories: 701 // 200g * 350.5 calories/100g
            }
        );
        console.log(`✅ Feeding record created successfully with ID: ${feeding.$id}`);
        
        // Test 6: Query feedings
        console.log('\n6️⃣ Querying feeding records...');
        const feedingsList = await databases.listDocuments(
            DATABASE_ID,
            'feedings',
            [
                Query.equal('dogId', createdDogId),
                Query.orderDesc('timestamp'),
                Query.limit(5)
            ]
        );
        console.log(`✅ Found ${feedingsList.total} feeding records for test dog`);
        
        // Cleanup
        console.log('\n🧹 Cleaning up test data...');
        
        // Delete feeding
        await databases.deleteDocument(DATABASE_ID, 'feedings', feeding.$id);
        console.log('✅ Feeding record deleted');
        
        // Delete dog
        await databases.deleteDocument(DATABASE_ID, 'dogs', createdDogId);
        console.log('✅ Dog deleted');
        
        // Delete food
        await databases.deleteDocument(DATABASE_ID, 'foods', createdFoodId);
        console.log('✅ Food deleted');
        
        console.log('\n✅ All database operations completed successfully!');
        
    } catch (error) {
        console.error('\n❌ Database operation failed!');
        console.error('Error:', error.message);
        
        // Attempt cleanup on error
        if (createdDogId) {
            try {
                await databases.deleteDocument(DATABASE_ID, 'dogs', createdDogId);
            } catch (e) {}
        }
        if (createdFoodId) {
            try {
                await databases.deleteDocument(DATABASE_ID, 'foods', createdFoodId);
            } catch (e) {}
        }
    }
}

async function testStorageOperations() {
    console.log('\n\n🧪 Testing Storage Operations\n');
    
    try {
        // Test 1: List buckets
        console.log('1️⃣ Listing storage buckets...');
        const buckets = await storage.listBuckets();
        console.log(`✅ Found ${buckets.total} storage buckets`);
        buckets.buckets.forEach(bucket => {
            console.log(`   - ${bucket.name} (${bucket.$id})`);
        });
        
        // Test 2: Create a test file (simulated)
        console.log('\n2️⃣ Simulating file upload...');
        console.log('ℹ️ File upload test skipped (requires actual file)');
        console.log('✅ Storage service is accessible');
        
    } catch (error) {
        console.error('\n❌ Storage operation failed!');
        console.error('Error:', error.message);
    }
}

async function runAllTests() {
    console.log('🚀 Starting Appwrite Operations Test');
    console.log(`📍 Endpoint: ${APPWRITE_ENDPOINT}`);
    console.log(`🏗️ Project: ${APPWRITE_PROJECT_ID}`);
    console.log(`💾 Database: ${DATABASE_ID}`);
    
    await testDatabaseOperations();
    await testStorageOperations();
    
    console.log('\n\n🎉 All tests completed!');
}

// Run tests
runAllTests().catch(console.error);