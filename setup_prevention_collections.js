const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createPreventionCollections() {
    console.log('Creating prevention tools collections...');
    
    try {
        // 1. Create Weight Goals Collection
        console.log('\nCreating weight_goals collection...');
        const weightGoalsCollection = await databases.createCollection(
            DATABASE_ID,
            'weight_goals',
            'Weight Goals',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Weight Goals attributes
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'dogId', 36, true);
        await databases.createFloatAttribute(DATABASE_ID, 'weight_goals', 'targetWeight', true);
        await databases.createFloatAttribute(DATABASE_ID, 'weight_goals', 'currentWeight', true);
        await databases.createFloatAttribute(DATABASE_ID, 'weight_goals', 'startWeight', true);
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'targetDate', 10, true); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'startDate', 10, true); // YYYY-MM-DD
        await databases.createEnumAttribute(DATABASE_ID, 'weight_goals', 'goalType', 
            ['LOSE_WEIGHT', 'GAIN_WEIGHT', 'MAINTAIN_WEIGHT'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'weight_goals', 'status', 
            ['DRAFT', 'ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'strategy', 5000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'progress', 2000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'weight_goals', 'recommendations', 5000, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'weight_goals', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'weight_goals', 'dogWeightGoals', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'weight_goals', 'activeGoals', 'key', ['status']);
        
        console.log('✓ Weight goals collection created');
        
        // 2. Create Allergy Prevention Collection
        console.log('\nCreating allergy_prevention collection...');
        const allergyPreventionCollection = await databases.createCollection(
            DATABASE_ID,
            'allergy_prevention',
            'Allergy Prevention',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Allergy Prevention attributes
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'knownAllergens', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'suspectedAllergens', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'eliminationDiet', 5000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'preventionProtocol', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'allergy_prevention', 'emergencyPlan', 5000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'allergy_prevention', 'lastUpdated', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'allergy_prevention', 'dogAllergyPrevention', 'key', ['dogId']);
        
        console.log('✓ Allergy prevention collection created');
        
        // 3. Create Health Screenings Collection
        console.log('\nCreating health_screenings collection...');
        const healthScreeningsCollection = await databases.createCollection(
            DATABASE_ID,
            'health_screenings',
            'Health Screenings',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Health Screenings attributes
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'health_screenings', 'screeningType', 
            ['ROUTINE', 'DIAGNOSTIC', 'FOLLOW_UP', 'EMERGENCY', 'PREVENTION'], true);
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'scheduledDate', 10, true); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'completedDate', 10, false); // YYYY-MM-DD
        await databases.createEnumAttribute(DATABASE_ID, 'health_screenings', 'status', 
            ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'OVERDUE'], true);
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'tests', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'results', 10000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'followUpActions', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'health_screenings', 'nextScreeningDate', 10, false); // YYYY-MM-DD
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'health_screenings', 'dogScreenings', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'health_screenings', 'screeningsByDate', 'key', ['scheduledDate']);
        await databases.createIndex(DATABASE_ID, 'health_screenings', 'screeningsByStatus', 'key', ['status']);
        
        console.log('✓ Health screenings collection created');
        
        // 4. Create Vaccination Schedules Collection
        console.log('\nCreating vaccination_schedules collection...');
        const vaccinationSchedulesCollection = await databases.createCollection(
            DATABASE_ID,
            'vaccination_schedules',
            'Vaccination Schedules',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Vaccination Schedules attributes
        await databases.createStringAttribute(DATABASE_ID, 'vaccination_schedules', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'vaccination_schedules', 'vaccines', 20000, true); // JSON array
        await databases.createBooleanAttribute(DATABASE_ID, 'vaccination_schedules', 'customSchedule', true);
        await databases.createStringAttribute(DATABASE_ID, 'vaccination_schedules', 'reminders', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'vaccination_schedules', 'complianceStatus', 2000, true); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'vaccination_schedules', 'lastUpdated', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'vaccination_schedules', 'dogVaccinations', 'key', ['dogId']);
        
        console.log('✓ Vaccination schedules collection created');
        
        // 5. Create Dental Care Collection
        console.log('\nCreating dental_care collection...');
        const dentalCareCollection = await databases.createCollection(
            DATABASE_ID,
            'dental_care',
            'Dental Care',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Dental Care attributes
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'currentStatus', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'careRoutine', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'professionalCleanings', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'homeCareLogs', 15000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'dental_care', 'preventionPlan', 3000, true); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'dental_care', 'dogDentalCare', 'key', ['dogId']);
        
        console.log('✓ Dental care collection created');
        
        // 6. Create Risk Assessments Collection
        console.log('\nCreating risk_assessments collection...');
        const riskAssessmentsCollection = await databases.createCollection(
            DATABASE_ID,
            'risk_assessments',
            'Risk Assessments',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Risk Assessments attributes
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'assessmentDate', 10, true); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'breedRisks', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'ageRelatedRisks', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'lifestyleRisks', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'environmentalRisks', 5000, true); // JSON array
        await databases.createFloatAttribute(DATABASE_ID, 'risk_assessments', 'overallRiskScore', true, 0.0, 1.0);
        await databases.createStringAttribute(DATABASE_ID, 'risk_assessments', 'recommendations', 10000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'risk_assessments', 'dogRiskAssessments', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'risk_assessments', 'assessmentsByDate', 'key', ['assessmentDate']);
        await databases.createIndex(DATABASE_ID, 'risk_assessments', 'assessmentsByRisk', 'key', ['overallRiskScore']);
        
        console.log('✓ Risk assessments collection created');
        
        // 7. Create Seasonal Care Collection
        console.log('\nCreating seasonal_care collection...');
        const seasonalCareCollection = await databases.createCollection(
            DATABASE_ID,
            'seasonal_care',
            'Seasonal Care',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Seasonal Care attributes
        await databases.createStringAttribute(DATABASE_ID, 'seasonal_care', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'seasonal_care', 'season', 
            ['SPRING', 'SUMMER', 'AUTUMN', 'WINTER'], true);
        await databases.createIntegerAttribute(DATABASE_ID, 'seasonal_care', 'year', true, 2020, 2100);
        await databases.createStringAttribute(DATABASE_ID, 'seasonal_care', 'hazards', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'seasonal_care', 'preventiveMeasures', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'seasonal_care', 'careAdjustments', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'seasonal_care', 'reminders', 5000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'seasonal_care', 'dogSeasonalCare', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'seasonal_care', 'careBySeasonYear', 'key', ['season', 'year']);
        
        console.log('✓ Seasonal care collection created');
        
        // 8. Create Prevention Analytics Collection
        console.log('\nCreating prevention_analytics collection...');
        const preventionAnalyticsCollection = await databases.createCollection(
            DATABASE_ID,
            'prevention_analytics',
            'Prevention Analytics',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Prevention Analytics attributes
        await databases.createStringAttribute(DATABASE_ID, 'prevention_analytics', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'prevention_analytics', 'period', true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_analytics', 'complianceMetrics', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'prevention_analytics', 'healthImprovements', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'prevention_analytics', 'costBenefitAnalysis', 2000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'prevention_analytics', 'trends', 5000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'prevention_analytics', 'dogAnalytics', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'prevention_analytics', 'analyticsByPeriod', 'key', ['period']);
        
        console.log('✓ Prevention analytics collection created');
        
        // 9. Create Prevention Recommendations Collection
        console.log('\nCreating prevention_recommendations collection...');
        const preventionRecommendationsCollection = await databases.createCollection(
            DATABASE_ID,
            'prevention_recommendations',
            'Prevention Recommendations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Prevention Recommendations attributes
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'dogId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_recommendations', 'category', 
            ['GENERAL', 'NUTRITION', 'EXERCISE', 'GROOMING', 'MEDICAL', 'BEHAVIORAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'title', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'description', 1000, true);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_recommendations', 'priority', 
            ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_recommendations', 'evidence', 
            ['ANECDOTAL', 'OBSERVATIONAL', 'CLINICAL_STUDY', 'RANDOMIZED_TRIAL', 'META_ANALYSIS'], true);
        await databases.createFloatAttribute(DATABASE_ID, 'prevention_recommendations', 'applicability', true, 0.0, 1.0);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'implementationSteps', 3000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'expectedBenefits', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'prevention_recommendations', 'resources', 3000, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'prevention_recommendations', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'prevention_recommendations', 'dogRecommendations', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'prevention_recommendations', 'recommendationsByCategory', 'key', ['category']);
        await databases.createIndex(DATABASE_ID, 'prevention_recommendations', 'recommendationsByPriority', 'key', ['priority']);
        
        console.log('✓ Prevention recommendations collection created');
        
        // 10. Create Prevention Tasks Collection
        console.log('\nCreating prevention_tasks collection...');
        const preventionTasksCollection = await databases.createCollection(
            DATABASE_ID,
            'prevention_tasks',
            'Prevention Tasks',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Prevention Tasks attributes
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'title', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'description', 1000, false);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_tasks', 'category', 
            ['GENERAL', 'NUTRITION', 'EXERCISE', 'GROOMING', 'MEDICAL', 'BEHAVIORAL'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_tasks', 'priority', 
            ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'prevention_tasks', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'dueDate', 10, false); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'completedDate', 10, false); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'prevention_tasks', 'notes', 1000, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'prevention_tasks', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'prevention_tasks', 'dogTasks', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'prevention_tasks', 'tasksByStatus', 'key', ['status']);
        await databases.createIndex(DATABASE_ID, 'prevention_tasks', 'tasksByDueDate', 'key', ['dueDate']);
        
        console.log('✓ Prevention tasks collection created');
        
        // 11. Create Prevention Activities Collection
        console.log('\nCreating prevention_activities collection...');
        const preventionActivitiesCollection = await databases.createCollection(
            DATABASE_ID,
            'prevention_activities',
            'Prevention Activities',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Prevention Activities attributes
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'type', 50, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'title', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'description', 1000, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'prevention_activities', 'date', true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'notes', 1000, true);
        await databases.createStringAttribute(DATABASE_ID, 'prevention_activities', 'metadata', 2000, false); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'prevention_activities', 'dogActivities', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'prevention_activities', 'activitiesByType', 'key', ['type']);
        await databases.createIndex(DATABASE_ID, 'prevention_activities', 'activitiesByDate', 'key', ['date']);
        
        console.log('✓ Prevention activities collection created');
        
        // 12. Create Weight History Collection
        console.log('\nCreating weight_history collection...');
        const weightHistoryCollection = await databases.createCollection(
            DATABASE_ID,
            'weight_history',
            'Weight History',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Weight History attributes
        await databases.createStringAttribute(DATABASE_ID, 'weight_history', 'dogId', 36, true);
        await databases.createFloatAttribute(DATABASE_ID, 'weight_history', 'weight', true);
        await databases.createStringAttribute(DATABASE_ID, 'weight_history', 'date', 10, true); // YYYY-MM-DD
        await databases.createStringAttribute(DATABASE_ID, 'weight_history', 'notes', 500, false);
        await databases.createStringAttribute(DATABASE_ID, 'weight_history', 'measuredBy', 100, false);
        await databases.createStringAttribute(DATABASE_ID, 'weight_history', 'bodyConditionScore', 10, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'weight_history', 'recordedAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'weight_history', 'dogWeightHistory', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'weight_history', 'weightByDate', 'key', ['date']);
        
        console.log('✓ Weight history collection created');
        
        console.log('\n✅ All prevention tools collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingPreventionAttributes();
        }
    }
}

async function addMissingPreventionAttributes() {
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
createPreventionCollections()
    .then(() => {
        console.log('\nPrevention tools database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });