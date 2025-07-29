#!/usr/bin/env node
/**
 * Database Schema Initialization Script for SnackTrack
 * Creates all required collections, attributes, and indexes
 */

const { Client, Databases, ID, Permission, Role } = require('node-appwrite');
require('dotenv').config({ path: '../.env' });

const APPWRITE_ENDPOINT = process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v2';
const APPWRITE_PROJECT_ID = process.env.APPWRITE_PROJECT_ID;
const APPWRITE_API_KEY = process.env.APPWRITE_API_KEY;
const DATABASE_ID = process.env.APPWRITE_DATABASE_ID || 'snacktrack-db';

// Initialize client
const client = new Client()
    .setEndpoint(APPWRITE_ENDPOINT)
    .setProject(APPWRITE_PROJECT_ID)
    .setKey(APPWRITE_API_KEY);

const databases = new Databases(client);

// Collection Schemas
const collections = {
    dogs: {
        name: 'Dogs',
        attributes: [
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'name', type: 'string', size: 100, required: true },
            { key: 'breed', type: 'string', size: 100, required: false },
            { key: 'dateOfBirth', type: 'datetime', required: false },
            { key: 'weight', type: 'double', required: false, min: 0, max: 200 },
            { key: 'targetWeight', type: 'double', required: false, min: 0, max: 200 },
            { key: 'dailyCalories', type: 'integer', required: false, min: 0, max: 5000 },
            { key: 'activityLevel', type: 'string', size: 50, required: false },
            { key: 'imageUrl', type: 'string', size: 500, required: false },
            { key: 'notes', type: 'string', size: 1000, required: false },
            { key: 'isActive', type: 'boolean', required: true, default: true }
        ],
        indexes: [
            { key: 'userId', type: 'key', attributes: ['userId'] },
            { key: 'isActive', type: 'key', attributes: ['isActive'] },
            { key: 'userActive', type: 'key', attributes: ['userId', 'isActive'] }
        ]
    },
    
    foods: {
        name: 'Foods',
        attributes: [
            { key: 'name', type: 'string', size: 200, required: true },
            { key: 'brand', type: 'string', size: 100, required: false },
            { key: 'barcode', type: 'string', size: 50, required: false },
            { key: 'calories', type: 'double', required: true, min: 0, max: 1000 },
            { key: 'protein', type: 'double', required: false, min: 0, max: 100 },
            { key: 'fat', type: 'double', required: false, min: 0, max: 100 },
            { key: 'carbohydrates', type: 'double', required: false, min: 0, max: 100 },
            { key: 'fiber', type: 'double', required: false, min: 0, max: 100 },
            { key: 'moisture', type: 'double', required: false, min: 0, max: 100 },
            { key: 'category', type: 'string', size: 50, required: false },
            { key: 'imageUrl', type: 'string', size: 500, required: false },
            { key: 'isVerified', type: 'boolean', required: true, default: false },
            { key: 'createdBy', type: 'string', size: 255, required: false }
        ],
        indexes: [
            { key: 'barcode', type: 'unique', attributes: ['barcode'] },
            { key: 'name', type: 'fulltext', attributes: ['name'] },
            { key: 'brand', type: 'key', attributes: ['brand'] },
            { key: 'isVerified', type: 'key', attributes: ['isVerified'] }
        ]
    },
    
    feedings: {
        name: 'Feedings',
        attributes: [
            { key: 'dogId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'foodId', type: 'string', size: 255, required: true },
            { key: 'amount', type: 'double', required: true, min: 0, max: 5000 },
            { key: 'unit', type: 'string', size: 20, required: true, default: 'g' },
            { key: 'timestamp', type: 'datetime', required: true },
            { key: 'mealType', type: 'string', size: 50, required: false },
            { key: 'notes', type: 'string', size: 500, required: false },
            { key: 'calories', type: 'double', required: false, min: 0 }
        ],
        indexes: [
            { key: 'dogId', type: 'key', attributes: ['dogId'] },
            { key: 'userId', type: 'key', attributes: ['userId'] },
            { key: 'timestamp', type: 'key', attributes: ['timestamp'] },
            { key: 'dogTimestamp', type: 'key', attributes: ['dogId', 'timestamp'] }
        ]
    },
    
    weightEntries: {
        name: 'Weight Entries',
        attributes: [
            { key: 'dogId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'weight', type: 'double', required: true, min: 0, max: 200 },
            { key: 'date', type: 'datetime', required: true },
            { key: 'notes', type: 'string', size: 500, required: false }
        ],
        indexes: [
            { key: 'dogId', type: 'key', attributes: ['dogId'] },
            { key: 'date', type: 'key', attributes: ['date'] },
            { key: 'dogDate', type: 'key', attributes: ['dogId', 'date'] }
        ]
    },
    
    communities: {
        name: 'Communities',
        attributes: [
            { key: 'name', type: 'string', size: 100, required: true },
            { key: 'description', type: 'string', size: 1000, required: false },
            { key: 'creatorId', type: 'string', size: 255, required: true },
            { key: 'isPublic', type: 'boolean', required: true, default: true },
            { key: 'memberCount', type: 'integer', required: true, default: 0 },
            { key: 'tags', type: 'string', size: 500, required: false, array: true },
            { key: 'imageUrl', type: 'string', size: 500, required: false },
            { key: 'createdAt', type: 'datetime', required: true }
        ],
        indexes: [
            { key: 'creatorId', type: 'key', attributes: ['creatorId'] },
            { key: 'isPublic', type: 'key', attributes: ['isPublic'] },
            { key: 'name', type: 'fulltext', attributes: ['name'] }
        ]
    },
    
    teams: {
        name: 'Teams',
        attributes: [
            { key: 'name', type: 'string', size: 100, required: true },
            { key: 'description', type: 'string', size: 500, required: false },
            { key: 'ownerId', type: 'string', size: 255, required: true },
            { key: 'memberCount', type: 'integer', required: true, default: 1 },
            { key: 'createdAt', type: 'datetime', required: true }
        ],
        indexes: [
            { key: 'ownerId', type: 'key', attributes: ['ownerId'] }
        ]
    },
    
    teamMembers: {
        name: 'Team Members',
        attributes: [
            { key: 'teamId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'role', type: 'string', size: 50, required: true, default: 'member' },
            { key: 'joinedAt', type: 'datetime', required: true }
        ],
        indexes: [
            { key: 'teamId', type: 'key', attributes: ['teamId'] },
            { key: 'userId', type: 'key', attributes: ['userId'] },
            { key: 'teamUser', type: 'unique', attributes: ['teamId', 'userId'] }
        ]
    },
    
    barcodes: {
        name: 'Barcodes',
        attributes: [
            { key: 'barcode', type: 'string', size: 50, required: true },
            { key: 'foodId', type: 'string', size: 255, required: true },
            { key: 'source', type: 'string', size: 50, required: false },
            { key: 'verifiedAt', type: 'datetime', required: false },
            { key: 'createdBy', type: 'string', size: 255, required: false }
        ],
        indexes: [
            { key: 'barcode', type: 'unique', attributes: ['barcode'] },
            { key: 'foodId', type: 'key', attributes: ['foodId'] }
        ]
    },
    
    healthRecords: {
        name: 'Health Records',
        attributes: [
            { key: 'dogId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'type', type: 'string', size: 50, required: true },
            { key: 'title', type: 'string', size: 200, required: true },
            { key: 'description', type: 'string', size: 2000, required: false },
            { key: 'date', type: 'datetime', required: true },
            { key: 'veterinarian', type: 'string', size: 200, required: false },
            { key: 'cost', type: 'double', required: false, min: 0 },
            { key: 'attachments', type: 'string', size: 500, required: false, array: true }
        ],
        indexes: [
            { key: 'dogId', type: 'key', attributes: ['dogId'] },
            { key: 'type', type: 'key', attributes: ['type'] },
            { key: 'date', type: 'key', attributes: ['date'] }
        ]
    },
    
    vaccinations: {
        name: 'Vaccinations',
        attributes: [
            { key: 'dogId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'vaccine', type: 'string', size: 100, required: true },
            { key: 'date', type: 'datetime', required: true },
            { key: 'nextDue', type: 'datetime', required: false },
            { key: 'veterinarian', type: 'string', size: 200, required: false },
            { key: 'batchNumber', type: 'string', size: 100, required: false },
            { key: 'notes', type: 'string', size: 500, required: false }
        ],
        indexes: [
            { key: 'dogId', type: 'key', attributes: ['dogId'] },
            { key: 'nextDue', type: 'key', attributes: ['nextDue'] }
        ]
    },
    
    medications: {
        name: 'Medications',
        attributes: [
            { key: 'dogId', type: 'string', size: 255, required: true },
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'name', type: 'string', size: 200, required: true },
            { key: 'dosage', type: 'string', size: 100, required: true },
            { key: 'frequency', type: 'string', size: 100, required: true },
            { key: 'startDate', type: 'datetime', required: true },
            { key: 'endDate', type: 'datetime', required: false },
            { key: 'reason', type: 'string', size: 500, required: false },
            { key: 'prescribedBy', type: 'string', size: 200, required: false },
            { key: 'isActive', type: 'boolean', required: true, default: true }
        ],
        indexes: [
            { key: 'dogId', type: 'key', attributes: ['dogId'] },
            { key: 'isActive', type: 'key', attributes: ['isActive'] }
        ]
    },
    
    notifications: {
        name: 'Notifications',
        attributes: [
            { key: 'userId', type: 'string', size: 255, required: true },
            { key: 'type', type: 'string', size: 50, required: true },
            { key: 'title', type: 'string', size: 200, required: true },
            { key: 'message', type: 'string', size: 1000, required: true },
            { key: 'data', type: 'string', size: 1000, required: false },
            { key: 'isRead', type: 'boolean', required: true, default: false },
            { key: 'createdAt', type: 'datetime', required: true }
        ],
        indexes: [
            { key: 'userId', type: 'key', attributes: ['userId'] },
            { key: 'isRead', type: 'key', attributes: ['isRead'] },
            { key: 'userUnread', type: 'key', attributes: ['userId', 'isRead'] }
        ]
    }
};

async function createDatabase() {
    try {
        console.log(`🏗️ Creating database: ${DATABASE_ID}`);
        await databases.create(DATABASE_ID, 'SnackTrack Database');
        console.log('✅ Database created successfully');
    } catch (error) {
        if (error.code === 409) {
            console.log('ℹ️ Database already exists');
        } else {
            throw error;
        }
    }
}

async function createCollection(collectionId, collectionConfig) {
    try {
        console.log(`\n📦 Creating collection: ${collectionConfig.name} (${collectionId})`);
        
        // Create collection
        await databases.createCollection(
            DATABASE_ID,
            collectionId,
            collectionConfig.name,
            [
                Permission.read(Role.any()),
                Permission.create(Role.users()),
                Permission.update(Role.users()),
                Permission.delete(Role.users())
            ]
        );
        console.log('✅ Collection created');
        
        // Create attributes
        console.log('🔧 Creating attributes...');
        for (const attr of collectionConfig.attributes) {
            await createAttribute(collectionId, attr);
        }
        
        // Wait for attributes to be ready
        console.log('⏳ Waiting for attributes to be ready...');
        await new Promise(resolve => setTimeout(resolve, 5000));
        
        // Create indexes
        console.log('📇 Creating indexes...');
        for (const index of collectionConfig.indexes) {
            await createIndex(collectionId, index);
        }
        
        console.log(`✅ Collection ${collectionConfig.name} setup complete`);
        
    } catch (error) {
        if (error.code === 409) {
            console.log(`ℹ️ Collection ${collectionConfig.name} already exists`);
        } else {
            console.error(`❌ Error creating collection ${collectionConfig.name}:`, error.message);
        }
    }
}

async function createAttribute(collectionId, attr) {
    try {
        switch (attr.type) {
            case 'string':
                if (attr.array) {
                    await databases.createStringAttribute(
                        DATABASE_ID,
                        collectionId,
                        attr.key,
                        attr.size,
                        attr.required,
                        attr.default,
                        true
                    );
                } else {
                    await databases.createStringAttribute(
                        DATABASE_ID,
                        collectionId,
                        attr.key,
                        attr.size,
                        attr.required,
                        attr.default
                    );
                }
                break;
            case 'integer':
                await databases.createIntegerAttribute(
                    DATABASE_ID,
                    collectionId,
                    attr.key,
                    attr.required,
                    attr.min,
                    attr.max,
                    attr.default
                );
                break;
            case 'double':
                await databases.createFloatAttribute(
                    DATABASE_ID,
                    collectionId,
                    attr.key,
                    attr.required,
                    attr.min,
                    attr.max,
                    attr.default
                );
                break;
            case 'boolean':
                await databases.createBooleanAttribute(
                    DATABASE_ID,
                    collectionId,
                    attr.key,
                    attr.required,
                    attr.default
                );
                break;
            case 'datetime':
                await databases.createDatetimeAttribute(
                    DATABASE_ID,
                    collectionId,
                    attr.key,
                    attr.required,
                    attr.default
                );
                break;
        }
        console.log(`   ✅ Attribute ${attr.key} created`);
    } catch (error) {
        if (error.code === 409) {
            console.log(`   ℹ️ Attribute ${attr.key} already exists`);
        } else {
            console.error(`   ❌ Error creating attribute ${attr.key}:`, error.message);
        }
    }
}

async function createIndex(collectionId, index) {
    try {
        await databases.createIndex(
            DATABASE_ID,
            collectionId,
            index.key,
            index.type,
            index.attributes
        );
        console.log(`   ✅ Index ${index.key} created`);
    } catch (error) {
        if (error.code === 409) {
            console.log(`   ℹ️ Index ${index.key} already exists`);
        } else {
            console.error(`   ❌ Error creating index ${index.key}:`, error.message);
        }
    }
}

async function initializeDatabase() {
    console.log('🚀 Starting SnackTrack Database Initialization');
    console.log(`📍 Endpoint: ${APPWRITE_ENDPOINT}`);
    console.log(`🏗️ Project: ${APPWRITE_PROJECT_ID}`);
    console.log('');
    
    try {
        // Create database
        await createDatabase();
        
        // Create collections
        for (const [collectionId, config] of Object.entries(collections)) {
            await createCollection(collectionId, config);
        }
        
        console.log('\n✅ Database initialization completed successfully!');
        console.log('🎉 SnackTrack backend is ready to use.');
        
    } catch (error) {
        console.error('\n❌ Database initialization failed!');
        console.error('Error:', error.message);
        process.exit(1);
    }
}

// Run initialization
initializeDatabase();