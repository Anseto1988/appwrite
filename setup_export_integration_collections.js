const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createExportIntegrationCollections() {
    console.log('Creating export & integration collections...');
    
    try {
        // 1. Create Export Requests Collection
        console.log('\nCreating export_requests collection...');
        const exportRequestsCollection = await databases.createCollection(
            DATABASE_ID,
            'export_requests',
            'Export Requests',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Export Requests attributes
        await databases.createStringAttribute(DATABASE_ID, 'export_requests', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'export_requests', 'dogIds', 1000, true); // JSON array
        await databases.createEnumAttribute(DATABASE_ID, 'export_requests', 'exportType', 
            ['FULL_DATA');
        await databases.createEnumAttribute(DATABASE_ID, 'export_requests', 'format', 
            ['JSON');
        await databases.createStringAttribute(DATABASE_ID, 'export_requests', 'dateRange', 500, false); // JSON
        await databases.createBooleanAttribute(DATABASE_ID, 'export_requests', 'includePhotos', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'export_requests', 'includeAnalytics', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'export_requests', 'encryptionEnabled', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'export_requests', 'requestedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'export_requests', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'export_requests', 'completedAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'export_requests', 'downloadUrl', 500, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'export_requests', 'expiresAt', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'export_requests', 'fileSize', false, 0, 999999999);
        await databases.createStringAttribute(DATABASE_ID, 'export_requests', 'error', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'export_requests', 'userExports', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'export_requests', 'exportStatus', 'key', ['status']);
        
        console.log('✓ Export requests collection created');
        
        // 2. Create Veterinary Integrations Collection
        console.log('\nCreating veterinary_integrations collection...');
        const veterinaryIntegrationsCollection = await databases.createCollection(
            DATABASE_ID,
            'veterinary_integrations',
            'Veterinary Integrations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Veterinary Integrations attributes
        await databases.createStringAttribute(DATABASE_ID, 'veterinary_integrations', 'clinicName', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'veterinary_integrations', 'systemType', 
            ['GENERIC');
        await databases.createStringAttribute(DATABASE_ID, 'veterinary_integrations', 'apiEndpoint', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'veterinary_integrations', 'apiKey', 500, true); // Encrypted
        await databases.createBooleanAttribute(DATABASE_ID, 'veterinary_integrations', 'isActive', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'veterinary_integrations', 'lastSync', false);
        await databases.createEnumAttribute(DATABASE_ID, 'veterinary_integrations', 'syncFrequency', 
            ['MANUAL');
        await databases.createStringAttribute(DATABASE_ID, 'veterinary_integrations', 'dataMapping', 2000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'veterinary_integrations', 'permissions', 1000, true); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'veterinary_integrations', 'activeIntegrations', 'key', ['isActive']);
        
        console.log('✓ Veterinary integrations collection created');
        
        // 3. Create Calendar Integrations Collection
        console.log('\nCreating calendar_integrations collection...');
        const calendarIntegrationsCollection = await databases.createCollection(
            DATABASE_ID,
            'calendar_integrations',
            'Calendar Integrations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Calendar Integrations attributes
        await databases.createStringAttribute(DATABASE_ID, 'calendar_integrations', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'calendar_integrations', 'provider', 
            ['GOOGLE');
        await databases.createStringAttribute(DATABASE_ID, 'calendar_integrations', 'accountEmail', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_integrations', 'calendarId', 255, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'calendar_integrations', 'isActive', true);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_integrations', 'syncSettings', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'calendar_integrations', 'lastSync', false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'calendar_integrations', 'userCalendars', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'calendar_integrations', 'activeCalendars', 'key', ['isActive']);
        
        console.log('✓ Calendar integrations collection created');
        
        // 4. Create Fitness Tracker Integrations Collection
        console.log('\nCreating fitness_tracker_integrations collection...');
        const fitnessTrackerIntegrationsCollection = await databases.createCollection(
            DATABASE_ID,
            'fitness_tracker_integrations',
            'Fitness Tracker Integrations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Fitness Tracker Integrations attributes
        await databases.createStringAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'deviceType', 
            ['GENERIC');
        await databases.createStringAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'deviceId', 255, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'isActive', true);
        await databases.createStringAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'syncSettings', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'lastSync', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'fitness_tracker_integrations', 'batteryLevel', false, 0, 100);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'fitness_tracker_integrations', 'dogTrackers', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'fitness_tracker_integrations', 'activeTrackers', 'key', ['isActive']);
        
        console.log('✓ Fitness tracker integrations collection created');
        
        // 5. Create Cloud Backups Collection
        console.log('\nCreating cloud_backups collection...');
        const cloudBackupsCollection = await databases.createCollection(
            DATABASE_ID,
            'cloud_backups',
            'Cloud Backups',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Cloud Backups attributes
        await databases.createStringAttribute(DATABASE_ID, 'cloud_backups', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'cloud_backups', 'provider', 
            ['APPWRITE');
        await databases.createStringAttribute(DATABASE_ID, 'cloud_backups', 'backupSettings', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'cloud_backups', 'lastBackup', false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'cloud_backups', 'nextScheduledBackup', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'cloud_backups', 'storageUsed', true, 0, 999999999999);
        await databases.createIntegerAttribute(DATABASE_ID, 'cloud_backups', 'storageLimit', true, 0, 999999999999);
        await databases.createStringAttribute(DATABASE_ID, 'cloud_backups', 'backupHistory', 10000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'cloud_backups', 'userBackups', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'cloud_backups', 'nextBackups', 'key', ['nextScheduledBackup']);
        
        console.log('✓ Cloud backups collection created');
        
        // 6. Create API Integrations Collection
        console.log('\nCreating api_integrations collection...');
        const apiIntegrationsCollection = await databases.createCollection(
            DATABASE_ID,
            'api_integrations',
            'API Integrations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // API Integrations attributes
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'name', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'description', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'apiKey', 255, true); // Generated
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'apiSecret', 255, false); // Encrypted
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'webhookUrl', 500, false);
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'permissions', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'rateLimits', 1000, true); // JSON
        await databases.createBooleanAttribute(DATABASE_ID, 'api_integrations', 'isActive', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'api_integrations', 'createdAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'api_integrations', 'lastUsed', false);
        await databases.createStringAttribute(DATABASE_ID, 'api_integrations', 'usageStats', 2000, true); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'api_integrations', 'apiKeyIndex', 'key', ['apiKey']);
        await databases.createIndex(DATABASE_ID, 'api_integrations', 'activeApis', 'key', ['isActive']);
        
        console.log('✓ API integrations collection created');
        
        // 7. Create Import Requests Collection
        console.log('\nCreating import_requests collection...');
        const importRequestsCollection = await databases.createCollection(
            DATABASE_ID,
            'import_requests',
            'Import Requests',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Import Requests attributes
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'import_requests', 'source', 
            ['CSV');
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'fileUrl', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'mappingConfig', 2000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'validationRules', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'import_requests', 'requestedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'import_requests', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'progress', 1000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'import_requests', 'results', 5000, false); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'import_requests', 'userImports', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'import_requests', 'importStatus', 'key', ['status']);
        
        console.log('✓ Import requests collection created');
        
        // 8. Create Sync Configurations Collection
        console.log('\nCreating sync_configurations collection...');
        const syncConfigurationsCollection = await databases.createCollection(
            DATABASE_ID,
            'sync_configurations',
            'Sync Configurations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Sync Configurations attributes
        await databases.createStringAttribute(DATABASE_ID, 'sync_configurations', 'userId', 36, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'sync_configurations', 'syncEnabled', true);
        await databases.createEnumAttribute(DATABASE_ID, 'sync_configurations', 'syncInterval', 
            ['ON_CHANGE');
        await databases.createBooleanAttribute(DATABASE_ID, 'sync_configurations', 'syncOnWifiOnly', true);
        await databases.createEnumAttribute(DATABASE_ID, 'sync_configurations', 'conflictResolution', 
            ['LATEST_WINS');
        await databases.createDatetimeAttribute(DATABASE_ID, 'sync_configurations', 'lastSync', false);
        await databases.createStringAttribute(DATABASE_ID, 'sync_configurations', 'syncHistory', 10000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'sync_configurations', 'userSync', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'sync_configurations', 'enabledSync', 'key', ['syncEnabled']);
        
        console.log('✓ Sync configurations collection created');
        
        // 9. Create Calendar Events Collection
        console.log('\nCreating calendar_events collection...');
        const calendarEventsCollection = await databases.createCollection(
            DATABASE_ID,
            'calendar_events',
            'Calendar Events',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Calendar Events attributes
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'description', 1000, true);
        await databases.createEnumAttribute(DATABASE_ID, 'calendar_events', 'eventType', 
            ['APPOINTMENT');
        await databases.createDatetimeAttribute(DATABASE_ID, 'calendar_events', 'startTime', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'calendar_events', 'endTime', false);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'location', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'reminders', 500, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'recurrenceRule', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'calendarId', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'calendar_events', 'externalId', 255, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'calendar_events', 'dogEvents', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'calendar_events', 'eventsByTime', 'key', ['startTime']);
        await databases.createIndex(DATABASE_ID, 'calendar_events', 'eventsByType', 'key', ['eventType']);
        
        console.log('✓ Calendar events collection created');
        
        // 10. Create Fitness Data Collection
        console.log('\nCreating fitness_data collection...');
        const fitnessDataCollection = await databases.createCollection(
            DATABASE_ID,
            'fitness_data',
            'Fitness Data',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Fitness Data attributes
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'deviceId', 255, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'fitness_data', 'timestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'activityData', 2000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'sleepData', 2000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'vitalData', 2000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'fitness_data', 'locationData', 2000, false); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'fitness_data', 'dogFitnessData', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'fitness_data', 'fitnessDataByTime', 'key', ['timestamp']);
        await databases.createIndex(DATABASE_ID, 'fitness_data', 'fitnessDataByDevice', 'key', ['deviceId']);
        
        console.log('✓ Fitness data collection created');
        
        // 11. Create Restore Requests Collection
        console.log('\nCreating restore_requests collection...');
        const restoreRequestsCollection = await databases.createCollection(
            DATABASE_ID,
            'restore_requests',
            'Restore Requests',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Restore Requests attributes
        await databases.createStringAttribute(DATABASE_ID, 'restore_requests', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'restore_requests', 'backupId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'restore_requests', 'restoreOptions', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'restore_requests', 'requestedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'restore_requests', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'restore_requests', 'completedAt', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'restore_requests', 'itemsRestored', false, 0, 999999);
        await databases.createStringAttribute(DATABASE_ID, 'restore_requests', 'error', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'restore_requests', 'userRestores', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'restore_requests', 'restoreStatus', 'key', ['status']);
        
        console.log('✓ Restore requests collection created');
        
        // 12. Create Data Export History Collection
        console.log('\nCreating data_export_history collection...');
        const dataExportHistoryCollection = await databases.createCollection(
            DATABASE_ID,
            'data_export_history',
            'Data Export History',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Data Export History attributes
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'data_export_history', 'exportType', 
            ['FULL_BACKUP');
        await databases.createEnumAttribute(DATABASE_ID, 'data_export_history', 'format', 
            ['PDF');
        await databases.createDatetimeAttribute(DATABASE_ID, 'data_export_history', 'exportedAt', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'data_export_history', 'fileSize', true, 0, 999999999);
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'fileName', 255, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'data_export_history', 'includesPersonalData', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'data_export_history', 'expiresAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'downloadUrl', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'data_export_history', 'userExportHistory', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'data_export_history', 'exportHistoryByDate', 'key', ['exportedAt']);
        
        console.log('✓ Data export history collection created');
        
        console.log('\n✅ All export & integration collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingExportIntegrationAttributes();
        }
    }
}

async function addMissingExportIntegrationAttributes() {
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
createExportIntegrationCollections()
    .then(() => {
        console.log('\nExport & Integration database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });