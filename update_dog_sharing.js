const sdk = require('node-appwrite');

// Initialize Appwrite client
const client = new sdk.Client();

// Constants from AppwriteService.kt
const ENDPOINT = "https://parse.nordburglarp.de/v1";
const PROJECT_ID = "snackrack2";
const DATABASE_ID = "snacktrack-db";
const API_KEY = "standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5";

// Collection ID for dog sharing
const COLLECTION_DOG_SHARING = "dog_sharing";

// Initialize the API client
client
    .setEndpoint(ENDPOINT)
    .setProject(PROJECT_ID)
    .setKey(API_KEY);

// Initialize Appwrite services
const databases = new sdk.Databases(client);

// Helper function to create an attribute if it doesn't exist
async function createAttributeIfNotExists(attributeId, type, required) {
    try {
        try {
            await databases.getAttribute(DATABASE_ID, COLLECTION_DOG_SHARING, attributeId);
            console.log(`✓ Attribute ${attributeId} already exists.`);
        } catch (error) {
            if (error.code === 404) {
                // Create different attribute types based on the type parameter
                switch (type) {
                    case 'string':
                        await databases.createStringAttribute(
                            DATABASE_ID,
                            COLLECTION_DOG_SHARING,
                            attributeId,
                            255,
                            required,
                            required ? null : ''
                        );
                        break;
                    case 'datetime':
                        await databases.createDatetimeAttribute(
                            DATABASE_ID,
                            COLLECTION_DOG_SHARING,
                            attributeId,
                            required,
                            required ? null : null
                        );
                        break;
                }
                console.log(`✅ Created attribute ${attributeId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`❌ Error with attribute ${attributeId}:`, error);
    }
}

// Helper function to create an index if it doesn't exist
async function createIndexIfNotExists(indexId, type, attributes) {
    try {
        try {
            await databases.getIndex(DATABASE_ID, COLLECTION_DOG_SHARING, indexId);
            console.log(`✓ Index ${indexId} already exists.`);
        } catch (error) {
            if (error.code === 404) {
                await databases.createIndex(
                    DATABASE_ID,
                    COLLECTION_DOG_SHARING,
                    indexId,
                    type,
                    attributes
                );
                console.log(`✅ Created index ${indexId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`❌ Error with index ${indexId}:`, error);
    }
}

// Main function to update dog_sharing collection
async function updateDogSharingCollection() {
    console.log("🔄 Updating dog_sharing collection...");
    
    try {
        // Required attributes for dog_sharing based on TeamRepository.kt
        await createAttributeIfNotExists('dogId', 'string', true);
        await createAttributeIfNotExists('teamId', 'string', true);
        await createAttributeIfNotExists('sharedByUserId', 'string', true);
        await createAttributeIfNotExists('sharedAt', 'datetime', true);
        
        // Required indexes for dog_sharing
        await createIndexIfNotExists('idx_dog', 'key', ['dogId']);
        await createIndexIfNotExists('idx_team', 'key', ['teamId']);
        await createIndexIfNotExists('idx_dog_team', 'unique', ['dogId', 'teamId']);
        
        console.log("✅ dog_sharing collection update completed successfully!");
    } catch (error) {
        console.error("❌ Error during dog_sharing collection update:", error);
    }
}

// Run the update
updateDogSharingCollection().then(() => {
    console.log("Script execution completed.");
});
