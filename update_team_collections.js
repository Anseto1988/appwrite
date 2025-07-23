const sdk = require('node-appwrite');

// Initialize Appwrite client
const client = new sdk.Client();

// Constants from AppwriteService.kt
const ENDPOINT = "https://parse.nordburglarp.de/v1";
const PROJECT_ID = "snackrack2";
const DATABASE_ID = "snacktrack-db";
const API_KEY = "standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5";

// Team-related collection IDs from TeamRepository.kt
const COLLECTION_TEAMS = "teams";
const COLLECTION_TEAM_MEMBERS = "team_members";
const COLLECTION_TEAM_INVITATIONS = "team_invitations";
const COLLECTION_DOG_SHARING = "dog_sharing";

// Initialize the API client
client
    .setEndpoint(ENDPOINT)
    .setProject(PROJECT_ID)
    .setKey(API_KEY);

// Initialize Appwrite services
const databases = new sdk.Databases(client);

// Helper function to create an attribute if it doesn't exist
async function createAttributeIfNotExists(collectionId, attributeId, type, required, options = {}) {
    try {
        try {
            await databases.getAttribute(DATABASE_ID, collectionId, attributeId);
            console.log(`✓ Attribute ${attributeId} already exists in ${collectionId}.`);
        } catch (error) {
            if (error.code === 404) {
                // Create different attribute types based on the type parameter
                switch (type) {
                    case 'string':
                        await databases.createStringAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            options.size || 255,
                            required,
                            required ? null : (options.default || '')
                        );
                        break;
                    case 'integer':
                        await databases.createIntegerAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            options.min,
                            options.max,
                            required ? null : options.default
                        );
                        break;
                    case 'float':
                        await databases.createFloatAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            options.min,
                            options.max,
                            required ? null : options.default
                        );
                        break;
                    case 'boolean':
                        await databases.createBooleanAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            required ? null : options.default
                        );
                        break;
                    case 'datetime':
                        await databases.createDatetimeAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            required ? null : options.default
                        );
                        break;
                }
                console.log(`✅ Created attribute ${attributeId} in collection ${collectionId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`❌ Error with attribute ${attributeId} in collection ${collectionId}:`, error);
    }
}

// Helper function to create an index if it doesn't exist
async function createIndexIfNotExists(collectionId, indexId, type, attributes) {
    try {
        try {
            await databases.getIndex(DATABASE_ID, collectionId, indexId);
            console.log(`✓ Index ${indexId} already exists in ${collectionId}.`);
        } catch (error) {
            if (error.code === 404) {
                await databases.createIndex(
                    DATABASE_ID,
                    collectionId,
                    indexId,
                    type,
                    attributes
                );
                console.log(`✅ Created index ${indexId} in collection ${collectionId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`❌ Error with index ${indexId} in collection ${collectionId}:`, error);
    }
}

// Main function to update team collections
async function updateTeamCollections() {
    console.log("🔄 Starting team collections update...");
    
    try {
        // Update teams collection
        console.log("\n🔹 Updating teams collection...");
        
        // Required attributes for teams based on TeamRepository.kt
        await createAttributeIfNotExists(COLLECTION_TEAMS, 'name', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAMS, 'ownerId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAMS, 'createdAt', 'datetime', true);
        
        // Required indexes for teams
        await createIndexIfNotExists(COLLECTION_TEAMS, 'idx_owner', 'key', ['ownerId']);
        
        // Update team_members collection
        console.log("\n🔹 Updating team_members collection...");
        
        // Required attributes for team_members based on TeamRepository.kt
        await createAttributeIfNotExists(COLLECTION_TEAM_MEMBERS, 'teamId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_MEMBERS, 'userId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_MEMBERS, 'role', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_MEMBERS, 'joinedAt', 'datetime', true);
        
        // Required indexes for team_members
        await createIndexIfNotExists(COLLECTION_TEAM_MEMBERS, 'idx_team_user', 'key', ['teamId', 'userId']);
        await createIndexIfNotExists(COLLECTION_TEAM_MEMBERS, 'idx_user', 'key', ['userId']);
        
        // Update team_invitations collection
        console.log("\n🔹 Updating team_invitations collection...");
        
        // Required attributes for team_invitations based on TeamRepository.kt
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'teamId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'teamName', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'invitedByUserId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'invitedUserId', 'string', false);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'invitedEmail', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'role', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'status', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'secret', 'string', true);
        await createAttributeIfNotExists(COLLECTION_TEAM_INVITATIONS, 'createdAt', 'datetime', true);
        
        // Required indexes for team_invitations
        await createIndexIfNotExists(COLLECTION_TEAM_INVITATIONS, 'idx_team', 'key', ['teamId']);
        await createIndexIfNotExists(COLLECTION_TEAM_INVITATIONS, 'idx_invited_email', 'key', ['invitedEmail']);
        await createIndexIfNotExists(COLLECTION_TEAM_INVITATIONS, 'idx_status', 'key', ['status']);
        
        // Update dog_sharing collection
        console.log("\n🔹 Updating dog_sharing collection...");
        
        // Required attributes for dog_sharing based on TeamRepository.kt
        await createAttributeIfNotExists(COLLECTION_DOG_SHARING, 'dogId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_DOG_SHARING, 'teamId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_DOG_SHARING, 'sharedByUserId', 'string', true);
        await createAttributeIfNotExists(COLLECTION_DOG_SHARING, 'sharedAt', 'datetime', true);
        
        // Required indexes for dog_sharing
        await createIndexIfNotExists(COLLECTION_DOG_SHARING, 'idx_dog', 'key', ['dogId']);
        await createIndexIfNotExists(COLLECTION_DOG_SHARING, 'idx_team', 'key', ['teamId']);
        await createIndexIfNotExists(COLLECTION_DOG_SHARING, 'idx_dog_team', 'unique', ['dogId', 'teamId']);
        
        console.log("\n✅ Team collections update completed successfully!");
    } catch (error) {
        console.error("\n❌ Error during team collections update:", error);
    }
}

// Run the update
updateTeamCollections().then(() => {
    console.log("Script execution completed.");
});
