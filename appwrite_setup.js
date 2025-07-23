const sdk = require('node-appwrite');

// Initialize Appwrite client
const client = new sdk.Client();

// Constants from AppwriteService.kt
const ENDPOINT = "https://parse.nordburglarp.de/v1";
const PROJECT_ID = "snackrack2";
const DATABASE_ID = "snacktrack-db";
const API_KEY = "standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5";

// Collection IDs from AppwriteService.kt
const COLLECTION_DOGS = "dogs";
const COLLECTION_WEIGHT_ENTRIES = "weightEntries";
const COLLECTION_FOOD_INTAKE = "foodIntake";
const COLLECTION_FOOD_DB = "foodDB";
const COLLECTION_FOOD_SUBMISSIONS = "foodSubmissions";
const COLLECTION_TEAMS = "teams";

// Additional team-related collections from TeamRepository.kt
const COLLECTION_TEAM_MEMBERS = "team_members";
const COLLECTION_TEAM_INVITATIONS = "team_invitations";
const COLLECTION_DOG_SHARING = "dog_sharing";

// Community-related collections from CommunityRepository.kt
const COLLECTION_COMMUNITY_POSTS = "community_posts";
const COLLECTION_COMMUNITY_PROFILES = "community_profiles";
const COLLECTION_COMMUNITY_COMMENTS = "community_comments";
const COLLECTION_COMMUNITY_LIKES = "community_likes";
const COLLECTION_COMMUNITY_FOLLOWS = "community_follows";

// Bucket IDs from AppwriteService.kt
const BUCKET_DOG_IMAGES = "dog_images";
const BUCKET_COMMUNITY_IMAGES = "community_images";

// Initialize the API client
client
    .setEndpoint(ENDPOINT)
    .setProject(PROJECT_ID)
    .setKey(API_KEY);

// Initialize Appwrite services
const databases = new sdk.Databases(client);
const storage = new sdk.Storage(client);

// Helper function to create a collection if it doesn't exist
async function createCollectionIfNotExists(collectionId, name, permissions) {
    try {
        try {
            // Check if collection exists
            await databases.getCollection(DATABASE_ID, collectionId);
            console.log(`Collection ${collectionId} already exists.`);
            statusTracker.collections.existing.push(collectionId);
            return;
        } catch (error) {
            if (error.code === 404) {
                // Collection doesn't exist, create it
                await databases.createCollection(
                    DATABASE_ID,
                    collectionId,
                    name,
                    permissions
                );
                console.log(`Created collection: ${collectionId}`);
                statusTracker.collections.created.push(collectionId);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`Error with collection ${collectionId}:`, error);
    }
}

// Helper function to create an attribute in a collection
async function createAttribute(collectionId, attributeId, type, required, options = {}) {
    try {
        try {
            await databases.getAttribute(DATABASE_ID, collectionId, attributeId);
            console.log(`Attribute ${attributeId} already exists in ${collectionId}.`);
            statusTracker.attributes.existing.push(`${collectionId}.${attributeId}`);
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
                            required ? undefined : (options.default || '')
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
                            options.default
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
                            options.default
                        );
                        break;
                    case 'boolean':
                        await databases.createBooleanAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            options.default
                        );
                        break;
                    case 'datetime':
                        await databases.createDatetimeAttribute(
                            DATABASE_ID,
                            collectionId,
                            attributeId,
                            required,
                            options.default
                        );
                        break;
                }
                console.log(`Created attribute ${attributeId} in collection ${collectionId}`);
                statusTracker.attributes.created.push(`${collectionId}.${attributeId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`Error with attribute ${attributeId} in collection ${collectionId}:`, error);
    }
}

// Helper function to create an index in a collection
async function createIndex(collectionId, indexId, type, attributes) {
    try {
        try {
            await databases.getIndex(DATABASE_ID, collectionId, indexId);
            console.log(`Index ${indexId} already exists in ${collectionId}.`);
            statusTracker.indexes.existing.push(`${collectionId}.${indexId}`);
        } catch (error) {
            if (error.code === 404) {
                await databases.createIndex(
                    DATABASE_ID,
                    collectionId,
                    indexId,
                    type,
                    attributes
                );
                console.log(`Created index ${indexId} in collection ${collectionId}`);
                statusTracker.indexes.created.push(`${collectionId}.${indexId}`);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`Error with index ${indexId} in collection ${collectionId}:`, error);
    }
}

// Helper function to create a bucket if it doesn't exist
async function createBucketIfNotExists(bucketId, name, permissions, fileSizeLimit = 30000000) {
    try {
        try {
            // Check if bucket exists
            await storage.getBucket(bucketId);
            console.log(`Bucket ${bucketId} already exists.`);
            statusTracker.buckets.existing.push(bucketId);
        } catch (error) {
            if (error.code === 404) {
                // Bucket doesn't exist, create it
                await storage.createBucket(
                    bucketId,
                    name,
                    permissions,
                    fileSizeLimit
                );
                console.log(`Created bucket: ${bucketId}`);
                statusTracker.buckets.created.push(bucketId);
            } else {
                throw error;
            }
        }
    } catch (error) {
        console.error(`Error with bucket ${bucketId}:`, error);
    }
}

// Main setup function
// Status tracking for clear output
const statusTracker = {
    collections: {
        created: [],
        existing: []
    },
    attributes: {
        created: [],
        existing: []
    },
    indexes: {
        created: [],
        existing: []
    },
    buckets: {
        created: [],
        existing: []
    }
};

// Helper function to print status
function printStatus() {
    console.log("\n=== APPWRITE SETUP STATUS ===");
    
    console.log("\nCollections:");
    console.log("- Already existing:", statusTracker.collections.existing.join(", ") || "None");
    console.log("- Newly created:", statusTracker.collections.created.join(", ") || "None");
    
    console.log("\nAttributes:");
    console.log("- Already existing:", statusTracker.attributes.existing.length > 0 ? statusTracker.attributes.existing.length + " attributes" : "None");
    console.log("- Newly created:", statusTracker.attributes.created.length > 0 ? statusTracker.attributes.created.length + " attributes" : "None");
    
    console.log("\nIndexes:");
    console.log("- Already existing:", statusTracker.indexes.existing.length > 0 ? statusTracker.indexes.existing.length + " indexes" : "None");
    console.log("- Newly created:", statusTracker.indexes.created.length > 0 ? statusTracker.indexes.created.length + " indexes" : "None");
    
    console.log("\nBuckets:");
    console.log("- Already existing:", statusTracker.buckets.existing.join(", ") || "None");
    console.log("- Newly created:", statusTracker.buckets.created.join(", ") || "None");
    
    console.log("\n===========================\n");
}

async function setupAppwrite() {
    console.log("Starting Appwrite setup...");

    try {
        // Check if database exists
        try {
            await databases.get(DATABASE_ID);
            console.log(`Database ${DATABASE_ID} already exists.`);
        } catch (error) {
            if (error.code === 404) {
                // Database doesn't exist, create it
                await databases.create(DATABASE_ID, DATABASE_ID);
                console.log(`Created database: ${DATABASE_ID}`);
            } else {
                throw error;
            }
        }

        // Define permissions (default to all)
        const permissions = ['read("any")', 'write("any")'];

        // Create collections
        await createCollectionIfNotExists(COLLECTION_DOGS, 'Dogs', permissions);
        await createCollectionIfNotExists(COLLECTION_WEIGHT_ENTRIES, 'Weight Entries', permissions);
        await createCollectionIfNotExists(COLLECTION_FOOD_INTAKE, 'Food Intake', permissions);
        await createCollectionIfNotExists(COLLECTION_FOOD_DB, 'Food Database', permissions);
        await createCollectionIfNotExists(COLLECTION_FOOD_SUBMISSIONS, 'Food Submissions', permissions);
        await createCollectionIfNotExists(COLLECTION_TEAMS, 'Teams', permissions);
        await createCollectionIfNotExists(COLLECTION_TEAM_MEMBERS, 'Team Members', permissions);
        await createCollectionIfNotExists(COLLECTION_TEAM_INVITATIONS, 'Team Invitations', permissions);
        await createCollectionIfNotExists(COLLECTION_DOG_SHARING, 'Dog Sharing', permissions);

        // Create buckets
        await createBucketIfNotExists(BUCKET_DOG_IMAGES, 'Dog Images', permissions);

        // Set up dogs collection
        await createAttribute(COLLECTION_DOGS, 'name', 'string', true);
        await createAttribute(COLLECTION_DOGS, 'breed', 'string', false);
        await createAttribute(COLLECTION_DOGS, 'birthDate', 'datetime', false);
        await createAttribute(COLLECTION_DOGS, 'imageUrl', 'string', false);
        await createAttribute(COLLECTION_DOGS, 'userId', 'string', true);
        await createAttribute(COLLECTION_DOGS, 'weight', 'float', false);
        await createIndex(COLLECTION_DOGS, 'idx_user', 'key', ['userId']);
        await createIndex(COLLECTION_DOGS, 'idx_name', 'fulltext', ['name']);

        // Set up weight entries collection
        await createAttribute(COLLECTION_WEIGHT_ENTRIES, 'dogId', 'string', true);
        await createAttribute(COLLECTION_WEIGHT_ENTRIES, 'date', 'datetime', true);
        await createAttribute(COLLECTION_WEIGHT_ENTRIES, 'weight', 'float', true);
        await createAttribute(COLLECTION_WEIGHT_ENTRIES, 'notes', 'string', false);
        await createIndex(COLLECTION_WEIGHT_ENTRIES, 'idx_dog_date', 'key', ['dogId', 'date']);

        // Set up food intake collection
        await createAttribute(COLLECTION_FOOD_INTAKE, 'dogId', 'string', true);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'foodId', 'string', false);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'name', 'string', true);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'date', 'datetime', true);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'amount', 'float', true);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'calories', 'integer', false);
        await createAttribute(COLLECTION_FOOD_INTAKE, 'notes', 'string', false);
        await createIndex(COLLECTION_FOOD_INTAKE, 'idx_dog_date', 'key', ['dogId', 'date']);

        // Set up food database collection
        await createAttribute(COLLECTION_FOOD_DB, 'ean', 'string', false);
        await createAttribute(COLLECTION_FOOD_DB, 'brand', 'string', true);
        await createAttribute(COLLECTION_FOOD_DB, 'product', 'string', true);
        await createAttribute(COLLECTION_FOOD_DB, 'protein', 'float', false);
        await createAttribute(COLLECTION_FOOD_DB, 'fat', 'float', false);
        await createAttribute(COLLECTION_FOOD_DB, 'crudeFiber', 'float', false);
        await createAttribute(COLLECTION_FOOD_DB, 'rawAsh', 'float', false);
        await createAttribute(COLLECTION_FOOD_DB, 'moisture', 'float', false);
        await createAttribute(COLLECTION_FOOD_DB, 'additives', 'string', false);
        await createAttribute(COLLECTION_FOOD_DB, 'imageUrl', 'string', false);
        await createIndex(COLLECTION_FOOD_DB, 'idx_ean', 'unique', ['ean']);
        await createIndex(COLLECTION_FOOD_DB, 'idx_brand', 'fulltext', ['brand']);
        await createIndex(COLLECTION_FOOD_DB, 'idx_product', 'fulltext', ['product']);

        // Set up food submissions collection
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'userId', 'string', true);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'ean', 'string', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'brand', 'string', true);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'product', 'string', true);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'protein', 'float', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'fat', 'float', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'crudeFiber', 'float', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'rawAsh', 'float', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'moisture', 'float', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'additives', 'string', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'imageUrl', 'string', false);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'status', 'string', true);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'submittedAt', 'datetime', true);
        await createAttribute(COLLECTION_FOOD_SUBMISSIONS, 'reviewedAt', 'datetime', false);
        await createIndex(COLLECTION_FOOD_SUBMISSIONS, 'idx_status', 'key', ['status']);
        await createIndex(COLLECTION_FOOD_SUBMISSIONS, 'idx_user', 'key', ['userId']);

        // Set up teams collection
        await createAttribute(COLLECTION_TEAMS, 'name', 'string', true);
        await createAttribute(COLLECTION_TEAMS, 'ownerId', 'string', true);
        await createAttribute(COLLECTION_TEAMS, 'createdAt', 'datetime', true);
        await createIndex(COLLECTION_TEAMS, 'idx_owner', 'key', ['ownerId']);
        
        // Set up team_members collection
        await createAttribute(COLLECTION_TEAM_MEMBERS, 'teamId', 'string', true);
        await createAttribute(COLLECTION_TEAM_MEMBERS, 'userId', 'string', true);
        await createAttribute(COLLECTION_TEAM_MEMBERS, 'role', 'string', true);
        await createAttribute(COLLECTION_TEAM_MEMBERS, 'joinedAt', 'datetime', true);
        await createIndex(COLLECTION_TEAM_MEMBERS, 'idx_team_user', 'key', ['teamId', 'userId']);
        await createIndex(COLLECTION_TEAM_MEMBERS, 'idx_user', 'key', ['userId']);
        
        // Set up team_invitations collection
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'teamId', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'teamName', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'invitedByUserId', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'invitedUserId', 'string', false);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'invitedEmail', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'role', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'status', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'secret', 'string', true);
        await createAttribute(COLLECTION_TEAM_INVITATIONS, 'createdAt', 'datetime', true);
        await createIndex(COLLECTION_TEAM_INVITATIONS, 'idx_team', 'key', ['teamId']);
        await createIndex(COLLECTION_TEAM_INVITATIONS, 'idx_invited_email', 'key', ['invitedEmail']);
        await createIndex(COLLECTION_TEAM_INVITATIONS, 'idx_status', 'key', ['status']);
        
        // Set up dog_sharing collection
        await createAttribute(COLLECTION_DOG_SHARING, 'dogId', 'string', true);
        await createAttribute(COLLECTION_DOG_SHARING, 'teamId', 'string', true);
        await createAttribute(COLLECTION_DOG_SHARING, 'sharedByUserId', 'string', true);
        await createAttribute(COLLECTION_DOG_SHARING, 'sharedAt', 'datetime', true);
        await createIndex(COLLECTION_DOG_SHARING, 'idx_dog', 'key', ['dogId']);
        await createIndex(COLLECTION_DOG_SHARING, 'idx_team', 'key', ['teamId']);
        await createIndex(COLLECTION_DOG_SHARING, 'idx_dog_team', 'unique', ['dogId', 'teamId']);

        // Set up community_posts collection
        await createCollectionIfNotExists(COLLECTION_COMMUNITY_POSTS, 'Community Posts', permissions);
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'userId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'dogId', 'string', false);
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'content', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'postType', 'string', true);
        // Note: imageUrls will be stored as JSON string array
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'imageUrls', 'string', false, {size: 2000});
        // Note: hashtags will be stored as JSON string array
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'hashtags', 'string', false, {size: 1000});
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'likesCount', 'integer', true, {default: 0});
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'commentsCount', 'integer', true, {default: 0});
        await createAttribute(COLLECTION_COMMUNITY_POSTS, 'created_at', 'datetime', true);
        await createIndex(COLLECTION_COMMUNITY_POSTS, 'idx_user', 'key', ['userId']);
        await createIndex(COLLECTION_COMMUNITY_POSTS, 'idx_dog', 'key', ['dogId']);
        await createIndex(COLLECTION_COMMUNITY_POSTS, 'created', 'key', ['created_at']);

        // Set up community_profiles collection
        await createCollectionIfNotExists(COLLECTION_COMMUNITY_PROFILES, 'Community Profiles', permissions);
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'userId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'displayName', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'bio', 'string', false);
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'profileImageUrl', 'string', false);
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'isPremium', 'boolean', true, {default: false});
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'followersCount', 'integer', true, {default: 0});
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'followingCount', 'integer', true, {default: 0});
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'postsCount', 'integer', true, {default: 0});
        await createAttribute(COLLECTION_COMMUNITY_PROFILES, 'createdAt', 'datetime', true);
        await createIndex(COLLECTION_COMMUNITY_PROFILES, 'idx_user', 'unique', ['userId']);
        await createIndex(COLLECTION_COMMUNITY_PROFILES, 'created', 'key', ['createdAt']);

        // Set up community_comments collection
        await createCollectionIfNotExists(COLLECTION_COMMUNITY_COMMENTS, 'Community Comments', permissions);
        await createAttribute(COLLECTION_COMMUNITY_COMMENTS, 'postId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_COMMENTS, 'userId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_COMMENTS, 'content', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_COMMENTS, 'created_at', 'datetime', true);
        await createIndex(COLLECTION_COMMUNITY_COMMENTS, 'idx_post', 'key', ['postId']);
        await createIndex(COLLECTION_COMMUNITY_COMMENTS, 'idx_user', 'key', ['userId']);
        await createIndex(COLLECTION_COMMUNITY_COMMENTS, 'created', 'key', ['created_at']);

        // Set up community_likes collection
        await createCollectionIfNotExists(COLLECTION_COMMUNITY_LIKES, 'Community Likes', permissions);
        await createAttribute(COLLECTION_COMMUNITY_LIKES, 'postId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_LIKES, 'userId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_LIKES, 'created_at', 'datetime', true);
        await createIndex(COLLECTION_COMMUNITY_LIKES, 'idx_post', 'key', ['postId']);
        await createIndex(COLLECTION_COMMUNITY_LIKES, 'idx_user', 'key', ['userId']);
        await createIndex(COLLECTION_COMMUNITY_LIKES, 'idx_post_user', 'unique', ['postId', 'userId']);

        // Set up community_follows collection
        await createCollectionIfNotExists(COLLECTION_COMMUNITY_FOLLOWS, 'Community Follows', permissions);
        await createAttribute(COLLECTION_COMMUNITY_FOLLOWS, 'followerId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_FOLLOWS, 'followedId', 'string', true);
        await createAttribute(COLLECTION_COMMUNITY_FOLLOWS, 'created_at', 'datetime', true);
        await createIndex(COLLECTION_COMMUNITY_FOLLOWS, 'idx_follower', 'key', ['followerId']);
        await createIndex(COLLECTION_COMMUNITY_FOLLOWS, 'idx_followed', 'key', ['followedId']);
        await createIndex(COLLECTION_COMMUNITY_FOLLOWS, 'idx_relation', 'unique', ['followerId', 'followedId']);

        // Set up community_images bucket
        try {
            await storage.getBucket(BUCKET_COMMUNITY_IMAGES);
            console.log(`Bucket ${BUCKET_COMMUNITY_IMAGES} already exists.`);
            statusTracker.buckets.existing.push(BUCKET_COMMUNITY_IMAGES);
        } catch (error) {
            if (error.code === 404) {
                await storage.createBucket(
                    BUCKET_COMMUNITY_IMAGES,
                    'Community Images',
                    permissions
                );
                console.log(`Bucket ${BUCKET_COMMUNITY_IMAGES} created.`);
                statusTracker.buckets.created.push(BUCKET_COMMUNITY_IMAGES);
            } else {
                console.error(`Error checking bucket ${BUCKET_COMMUNITY_IMAGES}:`, error);
            }
        }

        // Print status summary
        printStatus();
        console.log("Appwrite setup completed successfully!");
    } catch (error) {
        console.error("Error during Appwrite setup:", error);
        // Print status even if there was an error
        printStatus();
    }
}

// Run the setup
setupAppwrite();
