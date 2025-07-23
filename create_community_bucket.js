const sdk = require('node-appwrite');

const client = new sdk.Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const storage = new sdk.Storage(client);

async function createCommunityBucket() {
    try {
        const bucket = await storage.createBucket(
            'community_images',
            'Community Images',
            ['read("any")', 'write("any")'], // Updated permission format
            true, // fileSecurity - boolean for file-level security
            undefined, // enabled - default true
            10000000 // maximum file size limit
        );
        console.log('Community Images bucket created:', bucket);
    } catch (error) {
        console.error('Error creating bucket:', error);
    }
}

createCommunityBucket();
