#!/usr/bin/env node
/**
 * Storage Buckets Setup Script
 * Creates storage buckets for images and files
 */

const { Client, Storage, Permission, Role } = require('node-appwrite');
require('dotenv').config({ path: '../.env' });

const APPWRITE_ENDPOINT = process.env.APPWRITE_ENDPOINT || 'https://parse.nordburglarp.de/v2';
const APPWRITE_PROJECT_ID = process.env.APPWRITE_PROJECT_ID;
const APPWRITE_API_KEY = process.env.APPWRITE_API_KEY;

const client = new Client()
    .setEndpoint(APPWRITE_ENDPOINT)
    .setProject(APPWRITE_PROJECT_ID)
    .setKey(APPWRITE_API_KEY);

const storage = new Storage(client);

// Bucket configurations
const buckets = {
    dog_images: {
        name: 'Dog Images',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ],
        maximumFileSize: 5 * 1024 * 1024, // 5MB
        allowedFileExtensions: ['jpg', 'jpeg', 'png', 'webp'],
        enabled: true,
        encryption: true,
        antivirus: true
    },
    food_images: {
        name: 'Food Images',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ],
        maximumFileSize: 5 * 1024 * 1024, // 5MB
        allowedFileExtensions: ['jpg', 'jpeg', 'png', 'webp'],
        enabled: true,
        encryption: true,
        antivirus: true
    },
    avatars: {
        name: 'User Avatars',
        permissions: [
            Permission.read(Role.any()),
            Permission.create(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.users())
        ],
        maximumFileSize: 2 * 1024 * 1024, // 2MB
        allowedFileExtensions: ['jpg', 'jpeg', 'png', 'webp'],
        enabled: true,
        encryption: true,
        antivirus: true
    }
};

async function createBucket(bucketId, config) {
    try {
        console.log(`\n📦 Creating storage bucket: ${config.name} (${bucketId})`);
        
        await storage.createBucket(
            bucketId,
            config.name,
            config.permissions,
            config.enabled,
            config.maximumFileSize,
            config.allowedFileExtensions,
            config.compression || 'none',
            config.encryption,
            config.antivirus
        );
        
        console.log(`✅ Bucket ${config.name} created successfully`);
        console.log(`   - Max file size: ${(config.maximumFileSize / 1024 / 1024).toFixed(1)}MB`);
        console.log(`   - Allowed types: ${config.allowedFileExtensions.join(', ')}`);
        console.log(`   - Encryption: ${config.encryption ? 'Yes' : 'No'}`);
        console.log(`   - Antivirus: ${config.antivirus ? 'Yes' : 'No'}`);
        
        return true;
    } catch (error) {
        if (error.code === 409) {
            console.log(`ℹ️ Bucket ${config.name} already exists`);
            return true;
        } else {
            console.error(`❌ Error creating bucket ${config.name}:`, error.message);
            return false;
        }
    }
}

async function listExistingBuckets() {
    try {
        console.log('📋 Checking existing buckets...');
        const bucketsList = await storage.listBuckets();
        
        if (bucketsList.total > 0) {
            console.log(`\nFound ${bucketsList.total} existing buckets:`);
            bucketsList.buckets.forEach(bucket => {
                console.log(`   - ${bucket.name} (${bucket.$id})`);
            });
        } else {
            console.log('No existing buckets found.');
        }
        console.log('');
        
    } catch (error) {
        console.error('Could not list existing buckets:', error.message);
    }
}

async function main() {
    console.log('🚀 Starting Storage Buckets Setup');
    console.log(`📍 Endpoint: ${APPWRITE_ENDPOINT}`);
    console.log(`🏗️ Project: ${APPWRITE_PROJECT_ID}`);
    console.log('');
    
    // List existing buckets first
    await listExistingBuckets();
    
    const bucketId = process.argv[2];
    
    if (bucketId) {
        // Create specific bucket
        if (buckets[bucketId]) {
            await createBucket(bucketId, buckets[bucketId]);
        } else {
            console.error(`❌ Unknown bucket: ${bucketId}`);
            console.log('\nAvailable buckets:');
            Object.keys(buckets).forEach(id => {
                console.log(`  - ${id}`);
            });
        }
    } else {
        // Create all buckets
        let successCount = 0;
        let totalCount = Object.keys(buckets).length;
        
        for (const [id, config] of Object.entries(buckets)) {
            const success = await createBucket(id, config);
            if (success) successCount++;
        }
        
        console.log(`\n✅ Created ${successCount}/${totalCount} storage buckets`);
        console.log('\n🎉 Storage setup complete!');
    }
}

// Run the script
main().catch(console.error);