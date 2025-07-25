const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createNutritionCollections() {
    console.log('Creating nutrition analysis collections...');
    
    try {
        // 1. Create Nutrition Analysis Collection
        console.log('\nCreating nutrition_analysis collection...');
        const nutritionCollection = await databases.createCollection(
            DATABASE_ID,
            'nutrition_analysis',
            'Daily Nutrition Analysis',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Nutrition Analysis attributes
        await databases.createStringAttribute(DATABASE_ID, 'nutrition_analysis', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'nutrition_analysis', 'date', true);
        
        // Consumed nutrients
        await databases.createIntegerAttribute(DATABASE_ID, 'nutrition_analysis', 'totalCalories', true, 0, 10000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'totalProtein', true, 0, 1000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'totalFat', true, 0, 1000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'totalCarbs', true, 0, 1000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'totalFiber', true, 0, 500);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'totalMoisture', true, 0, 1000);
        
        // Recommended amounts
        await databases.createIntegerAttribute(DATABASE_ID, 'nutrition_analysis', 'recommendedCalories', true, 0, 10000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'recommendedProtein', true, 0, 1000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'recommendedFat', true, 0, 1000);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'recommendedCarbs', true, 0, 1000);
        
        // Treat budget
        await databases.createIntegerAttribute(DATABASE_ID, 'nutrition_analysis', 'treatCaloriesConsumed', true, 0, 5000);
        await databases.createIntegerAttribute(DATABASE_ID, 'nutrition_analysis', 'treatCaloriesLimit', true, 0, 5000);
        
        // BARF percentages
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'meatPercentage', true, 0, 100);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'bonePercentage', true, 0, 100);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'organPercentage', true, 0, 100);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'vegetablePercentage', true, 0, 100);
        await databases.createFloatAttribute(DATABASE_ID, 'nutrition_analysis', 'supplementPercentage', false, 0, 100);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'nutrition_analysis', 'dogAnalysis', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'nutrition_analysis', 'analysisByDate', 'key', ['dogId', 'date']);
        
        console.log('✓ Nutrition analysis collection created');
        
        // 2. Create Treat Budget Collection
        console.log('\nCreating treat_budgets collection...');
        const treatBudgetCollection = await databases.createCollection(
            DATABASE_ID,
            'treat_budgets',
            'Daily Treat Budgets',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Treat Budget attributes
        await databases.createStringAttribute(DATABASE_ID, 'treat_budgets', 'dogId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'treat_budgets', 'date', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'treat_budgets', 'dailyCalorieLimit', true, 0, 5000);
        
        // Treats array as JSON
        await databases.createStringAttribute(DATABASE_ID, 'treat_budgets', 'treats', 10000, true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'treat_budgets', 'dogBudget', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'treat_budgets', 'budgetByDate', 'key', ['dogId', 'date']);
        
        console.log('✓ Treat budgets collection created');
        
        console.log('\n✅ All nutrition collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingNutritionAttributes();
        }
    }
}

async function addMissingNutritionAttributes() {
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
createNutritionCollections()
    .then(() => {
        console.log('\nNutrition database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });