const sdk = require('node-appwrite');

// Configuration
const ENDPOINT = 'https://parse.nordburglarp.de/v1';
const PROJECT_ID = 'snackrack2';
const API_KEY = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';
const DATABASE_ID = 'snacktrack-db';

async function setupCrawler() {
    // Initialize SDK
    const client = new sdk.Client();
    client
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setKey(API_KEY);
    
    const databases = new sdk.Databases(client);
    const functions = new sdk.Functions(client);
    
    console.log('🚀 Starting Appwrite Crawler Setup...\n');
    
    try {
        // Step 1: Update foodSubmissions collection
        console.log('📋 Step 1: Updating foodSubmissions collection...');
        
        try {
            // Add crawlSessionId attribute
            await databases.createStringAttribute(
                DATABASE_ID,
                'foodSubmissions',
                'crawlSessionId',
                36,
                false
            );
            console.log('✅ Added crawlSessionId attribute');
        } catch (error) {
            if (error.code === 409) {
                console.log('⚠️  crawlSessionId attribute already exists');
            } else {
                throw error;
            }
        }
        
        try {
            // Add source attribute
            await databases.createStringAttribute(
                DATABASE_ID,
                'foodSubmissions',
                'source',
                20,
                false,
                'manual'
            );
            console.log('✅ Added source attribute');
        } catch (error) {
            if (error.code === 409) {
                console.log('⚠️  source attribute already exists');
            } else {
                throw error;
            }
        }
        
        try {
            // Add sourceUrl attribute
            await databases.createStringAttribute(
                DATABASE_ID,
                'foodSubmissions',
                'sourceUrl',
                500,
                false
            );
            console.log('✅ Added sourceUrl attribute');
        } catch (error) {
            if (error.code === 409) {
                console.log('⚠️  sourceUrl attribute already exists');
            } else {
                throw error;
            }
        }
        
        // Step 2: Create crawlState collection
        console.log('\n📋 Step 2: Creating crawlState collection...');
        
        try {
            const crawlStateCollection = await databases.createCollection(
                DATABASE_ID,
                'crawlState',
                'Crawler State',
                [
                    sdk.Permission.read(sdk.Role.any()),
                    sdk.Permission.create(sdk.Role.users()),
                    sdk.Permission.update(sdk.Role.users()),
                    sdk.Permission.delete(sdk.Role.users())
                ]
            );
            console.log('✅ Created crawlState collection');
            
            // Add attributes to crawlState
            const attributes = [
                { key: 'currentSource', type: 'string', size: 20, required: false, default: 'opff' },
                { key: 'lastCrawledUrl', type: 'string', size: 500, required: false },
                { key: 'lastCrawledEan', type: 'string', size: 13, required: false },
                { key: 'opffPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
                { key: 'fressnapfPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
                { key: 'zooplusPage', type: 'integer', required: false, default: 1, min: 1, max: 10000 },
                { key: 'totalProcessed', type: 'integer', required: false, default: 0, min: 0 },
                { key: 'lastRunDate', type: 'datetime', required: false },
                { key: 'lastError', type: 'string', size: 500, required: false },
                { key: 'lastErrorDate', type: 'datetime', required: false },
                { key: 'statistics', type: 'string', size: 5000, required: false },
                { key: 'updatedAt', type: 'datetime', required: false }
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
                    } else if (attr.type === 'datetime') {
                        await databases.createDatetimeAttribute(
                            DATABASE_ID,
                            'crawlState',
                            attr.key,
                            attr.required
                        );
                    }
                    console.log(`✅ Added ${attr.key} attribute`);
                } catch (error) {
                    console.error(`❌ Error adding ${attr.key}:`, error.message);
                }
            }
            
        } catch (error) {
            if (error.code === 409) {
                console.log('⚠️  crawlState collection already exists');
            } else {
                throw error;
            }
        }
        
        // Step 3: Create indexes
        console.log('\n📋 Step 3: Creating indexes...');
        
        const indexes = [
            {
                collection: 'foodSubmissions',
                key: 'crawl_session_idx',
                type: 'key',
                attributes: ['crawlSessionId']
            },
            {
                collection: 'foodSubmissions',
                key: 'source_idx',
                type: 'key',
                attributes: ['source']
            }
        ];
        
        for (const index of indexes) {
            try {
                await databases.createIndex(
                    DATABASE_ID,
                    index.collection,
                    index.key,
                    index.type,
                    index.attributes
                );
                console.log(`✅ Created index ${index.key}`);
            } catch (error) {
                if (error.code === 409) {
                    console.log(`⚠️  Index ${index.key} already exists`);
                } else {
                    console.error(`❌ Error creating index ${index.key}:`, error.message);
                }
            }
        }
        
        // Step 4: Create Function
        console.log('\n📋 Step 4: Creating Appwrite Function...');
        
        try {
            const functionData = await functions.create(
                sdk.ID.unique(),
                'Dog Food Crawler',
                sdk.Runtime.Node180,
                ['any'], // execute permissions
                [], // events
                '0 2 * * *', // Daily at 2 AM
                900, // 15 minutes timeout
                true, // enabled
                true, // logging
                'src/index.js',
                'npm install',
                ['users.read', 'documents.read', 'documents.write'] // scopes
            );
            
            console.log(`✅ Created function: ${functionData.$id}`);
            console.log(`\n🎯 Function ID: ${functionData.$id}`);
            console.log('\nNow you need to:');
            console.log('1. Deploy the function code using:');
            console.log(`   cd functions/dog-food-crawler`);
            console.log(`   tar -czf code.tar.gz *`);
            console.log(`2. Upload code.tar.gz in Appwrite Console`);
            console.log(`3. Set APPWRITE_API_KEY environment variable in function settings`);
            
        } catch (error) {
            console.error('❌ Error creating function:', error.message);
            console.log('\nYou may need to create the function manually in Appwrite Console');
        }
        
        console.log('\n✅ Setup completed successfully!');
        
    } catch (error) {
        console.error('\n❌ Setup failed:', error);
        console.error(error.response?.data || error.message);
    }
}

// Run setup
setupCrawler();