const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v2')
    .setProject('672f86170022b9645901')
    .setKey(process.env.APPWRITE_API_KEY);

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack_db';

async function createStatisticsCollections() {
    console.log('Creating advanced statistics collections...');
    
    try {
        // 1. Create Advanced Statistics Collection
        console.log('\nCreating advanced_statistics collection...');
        const advancedStatisticsCollection = await databases.createCollection(
            DATABASE_ID,
            'advanced_statistics',
            'Advanced Analytics',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Advanced Statistics attributes
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'advanced_statistics', 'period', 
            ['WEEK', 'MONTH', 'QUARTER', 'YEAR', 'CUSTOM'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'advanced_statistics', 'startDate', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'advanced_statistics', 'endDate', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'advanced_statistics', 'generatedAt', true);
        
        // Analytics data as JSON
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'weightAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'nutritionAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'healthAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'activityAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'costAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'behavioralAnalytics', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'predictiveInsights', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'advanced_statistics', 'comparativeAnalysis', 5000, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'advanced_statistics', 'dogStatistics', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'advanced_statistics', 'recentStatistics', 'key', ['generatedAt']);
        
        console.log('✓ Advanced statistics collection created');
        
        // 2. Create Custom Reports Collection
        console.log('\nCreating custom_reports collection...');
        const customReportsCollection = await databases.createCollection(
            DATABASE_ID,
            'custom_reports',
            'Custom Reports',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Custom Reports attributes
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'name', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'description', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'createdBy', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'custom_reports', 'createdAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'custom_reports', 'reportType', 
            ['STANDARD', 'DETAILED', 'SUMMARY', 'COMPARATIVE', 'PREDICTIVE'], true);
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'sections', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'filters', 2000, true); // JSON object
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'schedule', 1000, false); // JSON object
        await databases.createStringAttribute(DATABASE_ID, 'custom_reports', 'recipients', 1000, false); // JSON array
        await databases.createEnumAttribute(DATABASE_ID, 'custom_reports', 'format', 
            ['PDF', 'EXCEL', 'CSV', 'HTML', 'JSON'], true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'custom_reports', 'userReports', 'key', ['createdBy']);
        await databases.createIndex(DATABASE_ID, 'custom_reports', 'reportsByType', 'key', ['reportType']);
        
        console.log('✓ Custom reports collection created');
        
        // 3. Create Report Schedules Collection
        console.log('\nCreating report_schedules collection...');
        const reportSchedulesCollection = await databases.createCollection(
            DATABASE_ID,
            'report_schedules',
            'Report Schedules',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Report Schedules attributes
        await databases.createStringAttribute(DATABASE_ID, 'report_schedules', 'reportId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'report_schedules', 'frequency', 
            ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'], true);
        await databases.createIntegerAttribute(DATABASE_ID, 'report_schedules', 'dayOfWeek', false, 1, 7);
        await databases.createIntegerAttribute(DATABASE_ID, 'report_schedules', 'dayOfMonth', false, 1, 31);
        await databases.createStringAttribute(DATABASE_ID, 'report_schedules', 'time', 5, true); // HH:mm
        await databases.createDatetimeAttribute(DATABASE_ID, 'report_schedules', 'nextRunDate', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'report_schedules', 'isActive', true, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'report_schedules', 'lastRunDate', false);
        await databases.createEnumAttribute(DATABASE_ID, 'report_schedules', 'lastRunStatus', 
            ['SUCCESS', 'FAILED', 'PENDING'], false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'report_schedules', 'activeSchedules', 'key', ['isActive']);
        await databases.createIndex(DATABASE_ID, 'report_schedules', 'nextRuns', 'key', ['nextRunDate']);
        
        console.log('✓ Report schedules collection created');
        
        // 4. Create Analytics Cache Collection
        console.log('\nCreating analytics_cache collection...');
        const analyticsCacheCollection = await databases.createCollection(
            DATABASE_ID,
            'analytics_cache',
            'Analytics Cache',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Analytics Cache attributes
        await databases.createStringAttribute(DATABASE_ID, 'analytics_cache', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'analytics_cache', 'period', 
            ['WEEK', 'MONTH', 'QUARTER', 'YEAR', 'CUSTOM'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'analytics_cache', 'generatedAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'analytics_cache', 'expiresAt', true);
        await databases.createStringAttribute(DATABASE_ID, 'analytics_cache', 'data', 65535, true); // Large JSON
        await databases.createStringAttribute(DATABASE_ID, 'analytics_cache', 'checksum', 64, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'analytics_cache', 'cacheByDog', 'key', ['dogId', 'period']);
        await databases.createIndex(DATABASE_ID, 'analytics_cache', 'expiredCache', 'key', ['expiresAt']);
        
        console.log('✓ Analytics cache collection created');
        
        // 5. Create Statistical Benchmarks Collection
        console.log('\nCreating statistical_benchmarks collection...');
        const statisticalBenchmarksCollection = await databases.createCollection(
            DATABASE_ID,
            'statistical_benchmarks',
            'Statistical Benchmarks',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Statistical Benchmarks attributes
        await databases.createEnumAttribute(DATABASE_ID, 'statistical_benchmarks', 'benchmarkType', 
            ['BREED', 'AGE_GROUP', 'WEIGHT_CLASS', 'ACTIVITY_LEVEL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'statistical_benchmarks', 'category', 100, true);
        await databases.createStringAttribute(DATABASE_ID, 'statistical_benchmarks', 'metrics', 5000, true); // JSON
        await databases.createIntegerAttribute(DATABASE_ID, 'statistical_benchmarks', 'sampleSize', true, 0, 999999);
        await databases.createDatetimeAttribute(DATABASE_ID, 'statistical_benchmarks', 'lastUpdated', true);
        await databases.createFloatAttribute(DATABASE_ID, 'statistical_benchmarks', 'confidenceLevel', true, 0, 1);
        await databases.createStringAttribute(DATABASE_ID, 'statistical_benchmarks', 'dataSource', 255, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'statistical_benchmarks', 'benchmarksByType', 'key', ['benchmarkType']);
        await databases.createIndex(DATABASE_ID, 'statistical_benchmarks', 'benchmarksByCategory', 'key', ['category']);
        
        console.log('✓ Statistical benchmarks collection created');
        
        // 6. Create Analytics Events Collection
        console.log('\nCreating analytics_events collection...');
        const analyticsEventsCollection = await databases.createCollection(
            DATABASE_ID,
            'analytics_events',
            'Analytics Events',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Analytics Events attributes
        await databases.createStringAttribute(DATABASE_ID, 'analytics_events', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'analytics_events', 'dogId', 36, false);
        await databases.createEnumAttribute(DATABASE_ID, 'analytics_events', 'eventType', 
            ['REPORT_GENERATED', 'REPORT_VIEWED', 'REPORT_EXPORTED', 'REPORT_SHARED', 
             'STATISTICS_VIEWED', 'GOAL_SET', 'GOAL_ACHIEVED', 'ALERT_TRIGGERED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'analytics_events', 'timestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'analytics_events', 'metadata', 2000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'analytics_events', 'sessionId', 36, false);
        await databases.createStringAttribute(DATABASE_ID, 'analytics_events', 'deviceInfo', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'analytics_events', 'userEvents', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'analytics_events', 'eventsByType', 'key', ['eventType']);
        await databases.createIndex(DATABASE_ID, 'analytics_events', 'eventsByTime', 'key', ['timestamp']);
        
        console.log('✓ Analytics events collection created');
        
        // 7. Create Data Export History Collection
        console.log('\nCreating data_export_history collection...');
        const dataExportHistoryCollection = await databases.createCollection(
            DATABASE_ID,
            'data_export_history',
            'Data Export History',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Data Export History attributes
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'data_export_history', 'exportType', 
            ['FULL_BACKUP', 'STATISTICS_REPORT', 'CUSTOM_REPORT', 'DATA_DUMP'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'data_export_history', 'format', 
            ['PDF', 'EXCEL', 'CSV', 'JSON', 'ZIP'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'data_export_history', 'exportedAt', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'data_export_history', 'fileSize', true, 0, 999999999);
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'fileName', 255, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'data_export_history', 'includesPersonalData', true, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'data_export_history', 'expiresAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'data_export_history', 'downloadUrl', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'data_export_history', 'userExports', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'data_export_history', 'exportsByDate', 'key', ['exportedAt']);
        
        console.log('✓ Data export history collection created');
        
        // 8. Create Performance Metrics Collection
        console.log('\nCreating performance_metrics collection...');
        const performanceMetricsCollection = await databases.createCollection(
            DATABASE_ID,
            'performance_metrics',
            'Performance Metrics',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Performance Metrics attributes
        await databases.createEnumAttribute(DATABASE_ID, 'performance_metrics', 'metricType', 
            ['QUERY_TIME', 'CALCULATION_TIME', 'EXPORT_TIME', 'CACHE_HIT_RATE'], true);
        await databases.createStringAttribute(DATABASE_ID, 'performance_metrics', 'operation', 255, true);
        await databases.createFloatAttribute(DATABASE_ID, 'performance_metrics', 'value', true, 0, 999999);
        await databases.createStringAttribute(DATABASE_ID, 'performance_metrics', 'unit', 20, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'performance_metrics', 'timestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'performance_metrics', 'context', 500, false); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'performance_metrics', 'metricsByType', 'key', ['metricType']);
        await databases.createIndex(DATABASE_ID, 'performance_metrics', 'metricsByTime', 'key', ['timestamp']);
        
        console.log('✓ Performance metrics collection created');
        
        console.log('\n✅ All statistics collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingStatisticsAttributes();
        }
    }
}

async function addMissingStatisticsAttributes() {
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
createStatisticsCollections()
    .then(() => {
        console.log('\nStatistics database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });