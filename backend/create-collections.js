#!/usr/bin/env node
/**
 * Individual Collection Creation Script
 * Creates collections one by one with error handling
 */

const { Client, Databases, Permission, Role } = require('node-appwrite');
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

// Simple collection definitions for individual creation
const collectionDefinitions = {
    dogs: {
        name: 'Dogs',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    foods: {
        name: 'Foods',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    feedings: {
        name: 'Feedings',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    weightEntries: {
        name: 'Weight Entries',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    communities: {
        name: 'Communities',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    teams: {
        name: 'Teams',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    teamMembers: {
        name: 'Team Members',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    barcodes: {
        name: 'Barcodes',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    healthRecords: {
        name: 'Health Records',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    vaccinations: {
        name: 'Vaccinations',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    medications: {
        name: 'Medications',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    },
    notifications: {
        name: 'Notifications',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ]
    }
};

async function createCollection(collectionId, config) {
    try {
        console.log(`\n📦 Creating collection: ${config.name} (${collectionId})`);
        
        await databases.createCollection(
            DATABASE_ID,
            collectionId,
            config.name,
            config.permissions
        );
        
        console.log(`✅ Collection ${config.name} created successfully`);
        return true;
    } catch (error) {
        if (error.code === 409) {
            console.log(`ℹ️ Collection ${config.name} already exists`);
            return true;
        } else {
            console.error(`❌ Error creating collection ${config.name}:`, error.message);
            return false;
        }
    }
}

async function main() {
    console.log('🚀 Starting Collection Creation');
    console.log(`📍 Database: ${DATABASE_ID}`);
    console.log('');
    
    const collectionId = process.argv[2];
    
    if (collectionId) {
        // Create specific collection
        if (collectionDefinitions[collectionId]) {
            await createCollection(collectionId, collectionDefinitions[collectionId]);
        } else {
            console.error(`❌ Unknown collection: ${collectionId}`);
            console.log('\nAvailable collections:');
            Object.keys(collectionDefinitions).forEach(id => {
                console.log(`  - ${id}`);
            });
        }
    } else {
        // Create all collections
        let successCount = 0;
        let totalCount = Object.keys(collectionDefinitions).length;
        
        for (const [id, config] of Object.entries(collectionDefinitions)) {
            const success = await createCollection(id, config);
            if (success) successCount++;
        }
        
        console.log(`\n✅ Created ${successCount}/${totalCount} collections`);
    }
}

// Run the script
main().catch(console.error);