const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v2')
    .setProject('672f86170022b9645901')
    .setKey(process.env.APPWRITE_API_KEY);

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack_db';

async function createHealthCollections() {
    console.log('Creating health tracking collections...');
    
    try {
        // 1. Create Allergies Collection
        console.log('\nCreating allergies collection...');
        const allergiesCollection = await databases.createCollection(
            DATABASE_ID,
            'allergies',
            'Dog Allergies',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Allergies attributes
        await databases.createStringAttribute(DATABASE_ID, 'allergies', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'allergies', 'allergen', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'allergies', 'allergyType', 
            ['FOOD', 'ENVIRONMENTAL', 'CONTACT', 'MEDICATION', 'OTHER'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'allergies', 'severity', 
            ['MILD', 'MODERATE', 'SEVERE', 'CRITICAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'allergies', 'symptoms', 1000, false, undefined, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'allergies', 'diagnosedDate', false);
        await databases.createStringAttribute(DATABASE_ID, 'allergies', 'diagnosedBy', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'allergies', 'notes', 2000, false);
        await databases.createBooleanAttribute(DATABASE_ID, 'allergies', 'isActive', true, true);
        
        // Allergies indexes
        await databases.createIndex(DATABASE_ID, 'allergies', 'dogId', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'allergies', 'activeAllergies', 'key', ['dogId', 'isActive']);
        
        console.log('✓ Allergies collection created');
        
        // 2. Create Medications Collection
        console.log('\nCreating medications collection...');
        const medicationsCollection = await databases.createCollection(
            DATABASE_ID,
            'medications',
            'Dog Medications',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Medications attributes
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'medicationName', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'medications', 'medicationType', 
            ['ORAL', 'TOPICAL', 'INJECTION', 'EYE_DROPS', 'EAR_DROPS', 'OTHER'], true);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'dosage', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'medications', 'frequency', 
            ['ONCE', 'DAILY', 'TWICE_DAILY', 'THREE_TIMES_DAILY', 'WEEKLY', 'AS_NEEDED', 'CUSTOM'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'medications', 'startDate', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'medications', 'endDate', false);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'reminderTimes', 500, false, undefined, true);
        await databases.createEnumAttribute(DATABASE_ID, 'medications', 'foodInteraction', 
            ['NONE', 'WITH_FOOD', 'EMPTY_STOMACH', 'BEFORE_FOOD', 'AFTER_FOOD'], true);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'purpose', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'veterinarianName', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'medications', 'notes', 2000, false);
        await databases.createBooleanAttribute(DATABASE_ID, 'medications', 'isActive', true, true);
        
        // Medications indexes
        await databases.createIndex(DATABASE_ID, 'medications', 'dogMedications', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'medications', 'activeMedications', 'key', ['dogId', 'isActive']);
        
        console.log('✓ Medications collection created');
        
        // 3. Create Health Entries Collection
        console.log('\nCreating health_entries collection...');
        const healthEntriesCollection = await databases.createCollection(
            DATABASE_ID,
            'health_entries',
            'Health Diary Entries',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Health Entries attributes
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'health_entries', 'entryDate', true);
        await databases.createEnumAttribute(DATABASE_ID, 'health_entries', 'entryType', 
            ['OBSERVATION', 'SYMPTOM', 'MEDICATION_GIVEN', 'VET_VISIT', 'VACCINATION', 'ROUTINE_CHECK'], true);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'symptoms', 1000, false, undefined, true);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'behaviorChanges', 500, false, undefined, true);
        await databases.createEnumAttribute(DATABASE_ID, 'health_entries', 'appetite', 
            ['NO_APPETITE', 'DECREASED', 'NORMAL', 'INCREASED', 'EXCESSIVE'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'health_entries', 'energyLevel', 
            ['VERY_LOW', 'LOW', 'NORMAL', 'HIGH', 'HYPERACTIVE'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'health_entries', 'stoolQuality', 
            ['VERY_HARD', 'HARD', 'IDEAL', 'SOFT_FORMED', 'VERY_SOFT', 'LIQUID'], false);
        await databases.createBooleanAttribute(DATABASE_ID, 'health_entries', 'vomiting', true, false);
        await databases.createFloatAttribute(DATABASE_ID, 'health_entries', 'temperature', false, 30, 45);
        await databases.createFloatAttribute(DATABASE_ID, 'health_entries', 'weight', false, 0, 200);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'possibleTriggers', 1000, false, undefined, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'health_entries', 'veterinaryVisit', true, false);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'treatment', 1000, false);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'notes', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'health_entries', 'attachedImageIds', 500, false, undefined, true);
        
        // Health Entries indexes
        await databases.createIndex(DATABASE_ID, 'health_entries', 'dogEntries', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'health_entries', 'entriesByDate', 'key', ['dogId', 'entryDate']);
        await databases.createIndex(DATABASE_ID, 'health_entries', 'entriesByType', 'key', ['dogId', 'entryType']);
        
        console.log('✓ Health entries collection created');
        
        console.log('\n✅ All health tracking collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingAttributes();
        }
    }
}

async function addMissingAttributes() {
    try {
        // Check and add any missing attributes for existing collections
        console.log('\nChecking for missing attributes in existing collections...');
        
        // This would need to be implemented based on what attributes are missing
        // For now, we'll just log that we tried
        console.log('Please manually verify that all required attributes exist in the collections.');
        
    } catch (error) {
        console.error('Error adding missing attributes:', error);
    }
}

// Run the setup
createHealthCollections()
    .then(() => {
        console.log('\nHealth tracking database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });