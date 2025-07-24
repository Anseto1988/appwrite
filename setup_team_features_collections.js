const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v2')
    .setProject('672f86170022b9645901')
    .setKey(process.env.APPWRITE_API_KEY);

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack_db';

async function createTeamFeaturesCollections() {
    console.log('Creating team features collections...');
    
    try {
        // 1. Create Feeding Tasks Collection
        console.log('\nCreating feeding_tasks collection...');
        const feedingTasksCollection = await databases.createCollection(
            DATABASE_ID,
            'feeding_tasks',
            'Team Feeding Tasks',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Feeding Tasks attributes
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'teamId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'assignedToUserId', 36, false);
        await databases.createEnumAttribute(DATABASE_ID, 'feeding_tasks', 'taskType', 
            ['FEEDING', 'MEDICATION', 'WALK', 'VET_APPOINTMENT', 'GROOMING', 'TRAINING', 'WEIGHT_CHECK', 'OTHER'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'feeding_tasks', 'scheduledDate', true);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'scheduledTime', 5, false); // HH:mm
        await databases.createEnumAttribute(DATABASE_ID, 'feeding_tasks', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE', 'CANCELLED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'completedByUserId', 36, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'feeding_tasks', 'completedAt', false);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'notes', 1000, false);
        await databases.createBooleanAttribute(DATABASE_ID, 'feeding_tasks', 'reminderEnabled', true, true);
        await databases.createIntegerAttribute(DATABASE_ID, 'feeding_tasks', 'reminderMinutesBefore', false, 0, 1440, 30);
        await databases.createStringAttribute(DATABASE_ID, 'feeding_tasks', 'recurrenceRule', 1000, false); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'feeding_tasks', 'teamTasks', 'key', ['teamId']);
        await databases.createIndex(DATABASE_ID, 'feeding_tasks', 'tasksByDate', 'key', ['teamId', 'scheduledDate']);
        await databases.createIndex(DATABASE_ID, 'feeding_tasks', 'tasksByStatus', 'key', ['teamId', 'status']);
        
        console.log('✓ Feeding tasks collection created');
        
        // 2. Create Shopping Lists Collection
        console.log('\nCreating shopping_lists collection...');
        const shoppingListsCollection = await databases.createCollection(
            DATABASE_ID,
            'shopping_lists',
            'Team Shopping Lists',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Shopping Lists attributes
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'teamId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'name', 255, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_lists', 'createdAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_lists', 'lastUpdated', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'shopping_lists', 'isActive', true, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'shopping_lists', 'teamLists', 'key', ['teamId']);
        await databases.createIndex(DATABASE_ID, 'shopping_lists', 'activeLists', 'key', ['teamId', 'isActive']);
        
        console.log('✓ Shopping lists collection created');
        
        // 3. Create Shopping Items Collection
        console.log('\nCreating shopping_items collection...');
        const shoppingItemsCollection = await databases.createCollection(
            DATABASE_ID,
            'shopping_items',
            'Shopping List Items',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Shopping Items attributes
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'shoppingListId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'productName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'brand', 255, false);
        await databases.createIntegerAttribute(DATABASE_ID, 'shopping_items', 'quantity', true, 1, 999);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'unit', 50, true);
        await databases.createEnumAttribute(DATABASE_ID, 'shopping_items', 'category', 
            ['FOOD', 'TREATS', 'MEDICATION', 'SUPPLEMENTS', 'TOYS', 'GROOMING', 'ACCESSORIES', 'OTHER'], true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'addedByUserId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_items', 'addedAt', true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'purchasedByUserId', 36, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_items', 'purchasedAt', false);
        await databases.createBooleanAttribute(DATABASE_ID, 'shopping_items', 'isPurchased', true, false);
        await databases.createBooleanAttribute(DATABASE_ID, 'shopping_items', 'isUrgent', true, false);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'notes', 500, false);
        await databases.createFloatAttribute(DATABASE_ID, 'shopping_items', 'estimatedPrice', false, 0, 9999);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_items', 'linkedFoodId', 36, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'shopping_items', 'listItems', 'key', ['shoppingListId']);
        await databases.createIndex(DATABASE_ID, 'shopping_items', 'unpurchasedItems', 'key', ['shoppingListId', 'isPurchased']);
        
        console.log('✓ Shopping items collection created');
        
        // 4. Create Team Activities Collection
        console.log('\nCreating team_activities collection...');
        const teamActivitiesCollection = await databases.createCollection(
            DATABASE_ID,
            'team_activities',
            'Team Activity Feed',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Team Activities attributes
        await databases.createStringAttribute(DATABASE_ID, 'team_activities', 'teamId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'team_activities', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'team_activities', 'dogId', 36, false);
        await databases.createEnumAttribute(DATABASE_ID, 'team_activities', 'activityType', 
            ['FEEDING', 'WEIGHT_ENTRY', 'MEDICATION_GIVEN', 'VET_VISIT', 'WALK_COMPLETED', 
             'TASK_COMPLETED', 'TASK_ASSIGNED', 'SHOPPING_ITEM_ADDED', 'SHOPPING_ITEM_PURCHASED',
             'DOG_ADDED', 'TEAM_MEMBER_JOINED', 'HEALTH_ISSUE_REPORTED', 'MILESTONE_REACHED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'team_activities', 'timestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'team_activities', 'description', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'team_activities', 'details', 2000, false); // JSON
        await databases.createBooleanAttribute(DATABASE_ID, 'team_activities', 'isImportant', true, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'team_activities', 'teamActivities', 'key', ['teamId']);
        await databases.createIndex(DATABASE_ID, 'team_activities', 'activitiesByDate', 'key', ['teamId', 'timestamp']);
        await databases.createIndex(DATABASE_ID, 'team_activities', 'userActivities', 'key', ['userId', 'timestamp']);
        
        console.log('✓ Team activities collection created');
        
        // 5. Create Task Templates Collection
        console.log('\nCreating task_templates collection...');
        const taskTemplatesCollection = await databases.createCollection(
            DATABASE_ID,
            'task_templates',
            'Task Templates',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Task Templates attributes
        await databases.createStringAttribute(DATABASE_ID, 'task_templates', 'teamId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'task_templates', 'name', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'task_templates', 'taskType', 
            ['FEEDING', 'MEDICATION', 'WALK', 'VET_APPOINTMENT', 'GROOMING', 'TRAINING', 'WEIGHT_CHECK', 'OTHER'], true);
        await databases.createStringAttribute(DATABASE_ID, 'task_templates', 'defaultTime', 5, false);
        await databases.createIntegerAttribute(DATABASE_ID, 'task_templates', 'defaultDuration', false, 0, 1440);
        await databases.createStringAttribute(DATABASE_ID, 'task_templates', 'defaultNotes', 1000, false);
        await databases.createStringAttribute(DATABASE_ID, 'task_templates', 'recurrenceRule', 1000, false); // JSON
        await databases.createBooleanAttribute(DATABASE_ID, 'task_templates', 'isActive', true, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'task_templates', 'teamTemplates', 'key', ['teamId']);
        await databases.createIndex(DATABASE_ID, 'task_templates', 'activeTemplates', 'key', ['teamId', 'isActive']);
        
        console.log('✓ Task templates collection created');
        
        // 6. Create Consumption Predictions Collection
        console.log('\nCreating consumption_predictions collection...');
        const consumptionPredictionsCollection = await databases.createCollection(
            DATABASE_ID,
            'consumption_predictions',
            'Smart Shopping Predictions',
            [
                { read: ["users"], write: ["users"] }
            ]
        );
        
        // Consumption Predictions attributes
        await databases.createStringAttribute(DATABASE_ID, 'consumption_predictions', 'foodId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'consumption_predictions', 'foodName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'consumption_predictions', 'brand', 255, true);
        await databases.createFloatAttribute(DATABASE_ID, 'consumption_predictions', 'currentStock', true, 0, 99999);
        await databases.createFloatAttribute(DATABASE_ID, 'consumption_predictions', 'dailyConsumption', true, 0, 9999);
        await databases.createIntegerAttribute(DATABASE_ID, 'consumption_predictions', 'daysUntilEmpty', true, 0, 365);
        await databases.createDatetimeAttribute(DATABASE_ID, 'consumption_predictions', 'recommendedOrderDate', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'consumption_predictions', 'recommendedOrderQuantity', true, 1, 99);
        await databases.createFloatAttribute(DATABASE_ID, 'consumption_predictions', 'confidence', true, 0, 1);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'consumption_predictions', 'urgentPredictions', 'key', ['daysUntilEmpty']);
        
        console.log('✓ Consumption predictions collection created');
        
        console.log('\n✅ All team features collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingTeamAttributes();
        }
    }
}

async function addMissingTeamAttributes() {
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
createTeamFeaturesCollections()
    .then(() => {
        console.log('\nTeam features database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });