const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';
const DATABASE_ID = 'snacktrack-db';

async function setupMissingAttributes() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const databases = new sdk.Databases(client);
    
    console.log('🚀 Adding missing attributes to crawlState...\n');
    
    try {
        // Add missing attributes to crawlState
        const attributes = [
            { key: 'currentSource', type: 'string', size: 20, required: false, default: 'opff' },
            { key: 'opffPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
            { key: 'fressnapfPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
            { key: 'zooplusPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
            { key: 'totalProcessed', type: 'integer', required: false, default: 0, min: 0 }
        ];
        
        for (const attr of attributes) {
            try {
                if (attr.type === 'string') {
                    await databases.createStringAttribute(
                        DATABASE_ID,
                        'crawlState',
                        attr.key,
                        attr.size,
                        attr.required,
                        attr.default
                    );
                } else if (attr.type === 'integer') {
                    await databases.createIntegerAttribute(
                        DATABASE_ID,
                        'crawlState',
                        attr.key,
                        attr.required,
                        attr.min,
                        attr.max,
                        attr.default
                    );
                }
                console.log(`✅ Added ${attr.key} attribute`);
            } catch (error) {
                if (error.code === 409) {
                    console.log(`⚠️  Attribute ${attr.key} already exists`);
                } else {
                    console.error(`❌ Error adding ${attr.key}:`, error.message);
                }
            }
        }
        
        console.log('\n✅ All attributes added successfully!');
        
    } catch (error) {
        console.error('\n❌ Setup failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run setup
setupMissingAttributes();