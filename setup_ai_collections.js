const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createAICollections() {
    console.log('Creating AI/ML collections...');
    
    try {
        // 1. Create AI Recommendations Collection
        console.log('\nCreating ai_recommendations collection...');
        const recommendationsCollection = await databases.createCollection(
            DATABASE_ID,
            'ai_recommendations',
            'AI Food Recommendations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Recommendations attributes
        await databases.createStringAttribute(DATABASE_ID, 'ai_recommendations', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_recommendations', 'generatedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'ai_recommendations', 'recommendationType', 
            ['DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM'], true);
        await databases.createStringAttribute(DATABASE_ID, 'ai_recommendations', 'recommendations', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'ai_recommendations', 'reasoning', 2000, true);
        await databases.createFloatAttribute(DATABASE_ID, 'ai_recommendations', 'confidenceScore', true, 0, 1);
        await databases.createStringAttribute(DATABASE_ID, 'ai_recommendations', 'factors', 5000, true); // JSON object
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ai_recommendations', 'dogRecommendations', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'ai_recommendations', 'recommendationsByDate', 'key', ['dogId', 'generatedAt']);
        
        console.log('✓ AI recommendations collection created');
        
        // 2. Create AI Predictions Collection
        console.log('\nCreating ai_predictions collection...');
        const predictionsCollection = await databases.createCollection(
            DATABASE_ID,
            'ai_predictions',
            'Weight Predictions',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Predictions attributes
        await databases.createStringAttribute(DATABASE_ID, 'ai_predictions', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_predictions', 'predictionDate', true);
        await databases.createFloatAttribute(DATABASE_ID, 'ai_predictions', 'currentWeight', true, 0, 500);
        await databases.createStringAttribute(DATABASE_ID, 'ai_predictions', 'predictions', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'ai_predictions', 'modelVersion', 10, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ai_predictions', 'dogPredictions', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'ai_predictions', 'predictionsByDate', 'key', ['dogId', 'predictionDate']);
        
        console.log('✓ AI predictions collection created');
        
        // 3. Create AI Anomalies Collection
        console.log('\nCreating ai_anomalies collection...');
        const anomaliesCollection = await databases.createCollection(
            DATABASE_ID,
            'ai_anomalies',
            'Eating Anomalies',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Anomalies attributes
        await databases.createStringAttribute(DATABASE_ID, 'ai_anomalies', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_anomalies', 'detectedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'ai_anomalies', 'anomalyType', 
            ['UNUSUAL_AMOUNT', 'SCHEDULE_DEVIATION', 'HEALTH_CONCERN', 'BEHAVIORAL_CHANGE'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'ai_anomalies', 'severity', 
            ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'ai_anomalies', 'description', 2000, true);
        await databases.createStringAttribute(DATABASE_ID, 'ai_anomalies', 'recommendation', 1000, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'ai_anomalies', 'requiresVetAttention', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ai_anomalies', 'dogAnomalies', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'ai_anomalies', 'anomaliesByDate', 'key', ['dogId', 'detectedAt']);
        await databases.createIndex(DATABASE_ID, 'ai_anomalies', 'criticalAnomalies', 'key', ['severity', 'requiresVetAttention']);
        
        console.log('✓ AI anomalies collection created');
        
        // 4. Create AI Training Data Collection
        console.log('\nCreating ai_training_data collection...');
        const trainingDataCollection = await databases.createCollection(
            DATABASE_ID,
            'ai_training_data',
            'AI Training Data',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Training data attributes
        await databases.createEnumAttribute(DATABASE_ID, 'ai_training_data', 'modelType', 
            ['FOOD_RECOMMENDATION', 'HEALTH_MONITORING', 'BEHAVIOR_ANALYSIS', 'WEIGHT_MANAGEMENT'], true);
        await databases.createStringAttribute(DATABASE_ID, 'ai_training_data', 'inputData', 10000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'ai_training_data', 'actualOutcome', 10000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'ai_training_data', 'userFeedback', 2000, false); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_training_data', 'timestamp', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ai_training_data', 'trainingByModel', 'key', ['modelType']);
        await databases.createIndex(DATABASE_ID, 'ai_training_data', 'trainingByDate', 'key', ['timestamp']);
        
        console.log('✓ AI training data collection created');
        
        // 5. Create Risk Assessments Collection
        console.log('\nCreating ai_risk_assessments collection...');
        const riskAssessmentsCollection = await databases.createCollection(
            DATABASE_ID,
            'ai_risk_assessments',
            'Health Risk Assessments',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Risk assessments attributes
        await databases.createStringAttribute(DATABASE_ID, 'ai_risk_assessments', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_risk_assessments', 'assessmentDate', true);
        await databases.createFloatAttribute(DATABASE_ID, 'ai_risk_assessments', 'overallRiskScore', true, 0, 1);
        await databases.createStringAttribute(DATABASE_ID, 'ai_risk_assessments', 'riskFactors', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'ai_risk_assessments', 'recommendations', 10000, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'ai_risk_assessments', 'nextAssessmentDate', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ai_risk_assessments', 'dogAssessments', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'ai_risk_assessments', 'assessmentsByDate', 'key', ['dogId', 'assessmentDate']);
        await databases.createIndex(DATABASE_ID, 'ai_risk_assessments', 'highRiskDogs', 'key', ['overallRiskScore']);
        
        console.log('✓ Risk assessments collection created');
        
        console.log('\n✅ All AI/ML collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingAIAttributes();
        }
    }
}

async function addMissingAIAttributes() {
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
createAICollections()
    .then(() => {
        console.log('\nAI/ML database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });