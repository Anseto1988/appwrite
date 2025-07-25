const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createOfflineCollections() {
    console.log('Creating offline functionality collections...');
    
    try {
        // 1. Create Offline Configurations Collection
        console.log('\nCreating offline_configurations collection...');
        const offlineConfigurationsCollection = await databases.createCollection(
            DATABASE_ID,
            'offline_configurations',
            'Offline Configurations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Offline Configurations attributes
        await databases.createStringAttribute(DATABASE_ID, 'offline_configurations', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'offline_configurations', 'offlineMode', 
            ['DISABLED', 'BASIC', 'FULL', 'ADVANCED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'offline_configurations', 'syncConfiguration', 5000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_configurations', 'cacheConfiguration', 5000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_configurations', 'dataRetentionPolicy', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_configurations', 'conflictHandling', 3000, true); // JSON
        await databases.createBooleanAttribute(DATABASE_ID, 'offline_configurations', 'isEnabled', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_configurations', 'lastModified', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'offline_configurations', 'userConfig', 'key', ['userId']);
        
        console.log('✓ Offline configurations collection created');
        
        // 2. Create Sync State Collection
        console.log('\nCreating sync_state collection...');
        const syncStateCollection = await databases.createCollection(
            DATABASE_ID,
            'sync_state',
            'Sync State',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Sync State attributes
        await databases.createStringAttribute(DATABASE_ID, 'sync_state', 'userId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'sync_state', 'lastSyncTimestamp', false);
        await databases.createBooleanAttribute(DATABASE_ID, 'sync_state', 'syncInProgress', true);
        await databases.createEnumAttribute(DATABASE_ID, 'sync_state', 'currentSyncType', 
            ['FULL', 'INCREMENTAL', 'SELECTIVE', 'EMERGENCY', 'INITIAL'], false);
        await databases.createIntegerAttribute(DATABASE_ID, 'sync_state', 'pendingChanges', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'sync_state', 'failedItems', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'sync_state', 'conflicts', true, 0, 999999);
        await databases.createStringAttribute(DATABASE_ID, 'sync_state', 'lastSyncResult', 2000, false); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'sync_state', 'nextScheduledSync', false);
        await databases.createStringAttribute(DATABASE_ID, 'sync_state', 'syncHistory', 10000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'sync_state', 'userSyncState', 'key', ['userId']);
        
        console.log('✓ Sync state collection created');
        
        // 3. Create Offline Analytics Collection
        console.log('\nCreating offline_analytics collection...');
        const offlineAnalyticsCollection = await databases.createCollection(
            DATABASE_ID,
            'offline_analytics',
            'Offline Analytics',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Offline Analytics attributes
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'userId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_analytics', 'period', true);
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'offlineUsage', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'syncPerformance', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'cachePerformance', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'dataUsage', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_analytics', 'reliability', 3000, true); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'offline_analytics', 'userAnalytics', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'offline_analytics', 'analyticsByPeriod', 'key', ['period']);
        
        console.log('✓ Offline analytics collection created');
        
        // 4. Create Data Integrity Checks Collection
        console.log('\nCreating data_integrity_checks collection...');
        const dataIntegrityChecksCollection = await databases.createCollection(
            DATABASE_ID,
            'data_integrity_checks',
            'Data Integrity Checks',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Data Integrity Checks attributes
        await databases.createDatetimeAttribute(DATABASE_ID, 'data_integrity_checks', 'timestamp', true);
        await databases.createEnumAttribute(DATABASE_ID, 'data_integrity_checks', 'checkType', 
            ['QUICK', 'FULL', 'DEEP', 'REPAIR'], true);
        await databases.createIntegerAttribute(DATABASE_ID, 'data_integrity_checks', 'itemsChecked', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'data_integrity_checks', 'issuesFound', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'data_integrity_checks', 'issuesFixed', true, 0, 999999);
        await databases.createStringAttribute(DATABASE_ID, 'data_integrity_checks', 'corruptedItems', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'data_integrity_checks', 'recommendations', 2000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'data_integrity_checks', 'checksByTime', 'key', ['timestamp']);
        await databases.createIndex(DATABASE_ID, 'data_integrity_checks', 'checksByType', 'key', ['checkType']);
        
        console.log('✓ Data integrity checks collection created');
        
        // 5. Create Offline Migrations Collection
        console.log('\nCreating offline_migrations collection...');
        const offlineMigrationsCollection = await databases.createCollection(
            DATABASE_ID,
            'offline_migrations',
            'Offline Migrations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Offline Migrations attributes
        await databases.createIntegerAttribute(DATABASE_ID, 'offline_migrations', 'fromVersion', true, 0, 999);
        await databases.createIntegerAttribute(DATABASE_ID, 'offline_migrations', 'toVersion', true, 0, 999);
        await databases.createStringAttribute(DATABASE_ID, 'offline_migrations', 'migrationSteps', 5000, true); // JSON array
        await databases.createEnumAttribute(DATABASE_ID, 'offline_migrations', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_migrations', 'startedAt', false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_migrations', 'completedAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'offline_migrations', 'errors', 2000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'offline_migrations', 'migrationsByVersion', 'key', ['fromVersion', 'toVersion']);
        await databases.createIndex(DATABASE_ID, 'offline_migrations', 'migrationsByStatus', 'key', ['status']);
        
        console.log('✓ Offline migrations collection created');
        
        // 6. Create Network State Logs Collection (for analytics)
        console.log('\nCreating network_state_logs collection...');
        const networkStateLogsCollection = await databases.createCollection(
            DATABASE_ID,
            'network_state_logs',
            'Network State Logs',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Network State Logs attributes
        await databases.createStringAttribute(DATABASE_ID, 'network_state_logs', 'userId', 36, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'network_state_logs', 'isConnected', true);
        await databases.createEnumAttribute(DATABASE_ID, 'network_state_logs', 'connectionType', 
            ['NONE', 'WIFI', 'MOBILE_2G', 'MOBILE_3G', 'MOBILE_4G', 'MOBILE_5G', 'ETHERNET'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'network_state_logs', 'connectionQuality', 
            ['UNKNOWN', 'POOR', 'FAIR', 'GOOD', 'EXCELLENT'], true);
        await databases.createBooleanAttribute(DATABASE_ID, 'network_state_logs', 'isMetered', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'network_state_logs', 'bandwidthKbps', false, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'network_state_logs', 'latencyMs', false, 0, 99999);
        await databases.createDatetimeAttribute(DATABASE_ID, 'network_state_logs', 'timestamp', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'network_state_logs', 'userNetworkLogs', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'network_state_logs', 'logsByTime', 'key', ['timestamp']);
        
        console.log('✓ Network state logs collection created');
        
        // 7. Create Offline Capabilities Collection
        console.log('\nCreating offline_capabilities collection...');
        const offlineCapabilitiesCollection = await databases.createCollection(
            DATABASE_ID,
            'offline_capabilities',
            'Offline Capabilities',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Offline Capabilities attributes
        await databases.createStringAttribute(DATABASE_ID, 'offline_capabilities', 'supportedOperations', 5000, true); // JSON map
        await databases.createStringAttribute(DATABASE_ID, 'offline_capabilities', 'offlineFeatures', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'offline_capabilities', 'limitations', 3000, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_capabilities', 'lastUpdated', true);
        await databases.createStringAttribute(DATABASE_ID, 'offline_capabilities', 'version', 20, true);
        
        console.log('✓ Offline capabilities collection created');
        
        // 8. Create Queue Status Collection
        console.log('\nCreating queue_status collection...');
        const queueStatusCollection = await databases.createCollection(
            DATABASE_ID,
            'queue_status',
            'Queue Status',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Queue Status attributes
        await databases.createStringAttribute(DATABASE_ID, 'queue_status', 'userId', 36, true);
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'totalItems', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'pendingItems', true, 0, 999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'failedItems', true, 0, 999999);
        await databases.createDatetimeAttribute(DATABASE_ID, 'queue_status', 'oldestItem', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'estimatedSyncTime', true, 0, 9999999); // milliseconds
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'queueSizeBytes', true, 0, 999999999);
        await databases.createStringAttribute(DATABASE_ID, 'queue_status', 'itemsByType', 2000, true); // JSON map
        await databases.createIntegerAttribute(DATABASE_ID, 'queue_status', 'priorityItems', true, 0, 999999);
        await databases.createDatetimeAttribute(DATABASE_ID, 'queue_status', 'lastUpdated', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'queue_status', 'userQueueStatus', 'key', ['userId']);
        
        console.log('✓ Queue status collection created');
        
        // 9. Create Conflict Records Collection
        console.log('\nCreating conflict_records collection...');
        const conflictRecordsCollection = await databases.createCollection(
            DATABASE_ID,
            'conflict_records',
            'Conflict Records',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Conflict Records attributes
        await databases.createStringAttribute(DATABASE_ID, 'conflict_records', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'conflict_records', 'entityType', 
            ['DOG', 'FEEDING', 'HEALTH_ENTRY', 'MEDICATION', 'ALLERGY', 'WEIGHT_GOAL', 'PRODUCT', 'SYNC_QUEUE'], true);
        await databases.createStringAttribute(DATABASE_ID, 'conflict_records', 'entityId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'conflict_records', 'localTimestamp', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'conflict_records', 'serverTimestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'conflict_records', 'localData', 5000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'conflict_records', 'serverData', 5000, true); // JSON
        await databases.createEnumAttribute(DATABASE_ID, 'conflict_records', 'resolution', 
            ['LOCAL_WINS', 'SERVER_WINS', 'MERGE', 'MANUAL_REVIEW', 'PENDING'], false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'conflict_records', 'resolvedAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'conflict_records', 'resolvedBy', 36, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'conflict_records', 'userConflicts', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'conflict_records', 'pendingConflicts', 'key', ['resolution']);
        
        console.log('✓ Conflict records collection created');
        
        // 10. Create Offline Events Collection
        console.log('\nCreating offline_events collection...');
        const offlineEventsCollection = await databases.createCollection(
            DATABASE_ID,
            'offline_events',
            'Offline Events',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Offline Events attributes
        await databases.createStringAttribute(DATABASE_ID, 'offline_events', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'offline_events', 'eventType', 
            ['OFFLINE_START', 'OFFLINE_END', 'SYNC_START', 'SYNC_COMPLETE', 'SYNC_FAILED', 'CONFLICT_DETECTED', 'DATA_CORRUPTION'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'offline_events', 'timestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'offline_events', 'metadata', 2000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'offline_events', 'sessionId', 36, false);
        await databases.createStringAttribute(DATABASE_ID, 'offline_events', 'deviceInfo', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'offline_events', 'userEvents', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'offline_events', 'eventsByType', 'key', ['eventType']);
        await databases.createIndex(DATABASE_ID, 'offline_events', 'eventsByTime', 'key', ['timestamp']);
        
        console.log('✓ Offline events collection created');
        
        console.log('\n✅ All offline functionality collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingOfflineAttributes();
        }
    }
}

async function addMissingOfflineAttributes() {
    try {
        console.log('\nChecking for missing attributes in existing collections...');
        
        // This would need to be implemented based on what attributes are missing
        // For now, we'll just log that we tried
        console.log('Please manually verify that all required attributes exist in the collections.');
        
    } catch (error) {
        console.error('Error adding missing attributes:', error);
    }
}

// Run the setup
createOfflineCollections()
    .then(() => {
        console.log('\nOffline functionality database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });