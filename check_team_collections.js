const sdk = require('node-appwrite');

// Initialize Appwrite client
const client = new sdk.Client();

// Constants from AppwriteService.kt
const ENDPOINT = "https://parse.nordburglarp.de/v1";
const PROJECT_ID = "snackrack2";
const DATABASE_ID = "snacktrack-db";
const API_KEY = "standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5";

// Collection IDs for team-related collections
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

// Function to check collection attributes and indexes
async function checkCollection(collectionId) {
    console.log(`\n====== Checking collection: ${collectionId} ======`);
    
    try {
        // Get collection details
        const collection = await databases.getCollection(DATABASE_ID, collectionId);
        console.log(`Collection exists: ${collection.name}`);
        
        // Get attributes
        const attributes = await databases.listAttributes(DATABASE_ID, collectionId);
        console.log(`\nAttributes (${attributes.total}):`);
        attributes.attributes.forEach(attr => {
            console.log(`- ${attr.key} (${attr.type}), Required: ${attr.required}`);
        });
        
        // Get indexes
        const indexes = await databases.listIndexes(DATABASE_ID, collectionId);
        console.log(`\nIndexes (${indexes.total}):`);
        indexes.indexes.forEach(index => {
            console.log(`- ${index.key} (${index.type}), Attributes: ${index.attributes.join(', ')}`);
        });
        
    } catch (error) {
        console.error(`Error checking collection ${collectionId}:`, error);
    }
    
    console.log(`======================================\n`);
}

// Main function to check all team-related collections
async function checkTeamCollections() {
    console.log("Starting team collections check...");
    
    try {
        await checkCollection(COLLECTION_TEAMS);
        await checkCollection(COLLECTION_TEAM_MEMBERS);
        await checkCollection(COLLECTION_TEAM_INVITATIONS);
        await checkCollection(COLLECTION_DOG_SHARING);
        
        console.log("Team collections check completed!");
    } catch (error) {
        console.error("Error during check:", error);
    }
}

// Run the check
checkTeamCollections();
