const sdk = require('node-appwrite');

const client = new sdk.Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new sdk.Databases(client);

const DATABASE_ID = 'snacktrack-db';

async function checkCollectionAttributes() {
    const collections = [
        'community_posts',
        'community_profiles', 
        'community_comments',
        'community_likes',
        'community_follows'
    ];

    for (const collectionId of collections) {
        try {
            console.log(`\n=== ${collectionId.toUpperCase()} COLLECTION ===`);
            const collection = await databases.getCollection(DATABASE_ID, collectionId);
            
            console.log(`Attributes (${collection.attributes.length}):`);
            collection.attributes.forEach(attr => {
                console.log(`  - ${attr.key}: ${attr.type} (required: ${attr.required})`);
            });

            console.log(`Indexes (${collection.indexes.length}):`);
            collection.indexes.forEach(index => {
                console.log(`  - ${index.key}: ${index.type} on [${index.attributes.join(', ')}]`);
            });

        } catch (error) {
            console.error(`❌ Error checking ${collectionId}:`, error.message);
        }
    }
}

checkCollectionAttributes().catch(console.error);