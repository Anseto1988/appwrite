require('dotenv').config();
const sdk = require('node-appwrite');

async function testConnection() {
    console.log('🔍 Testing Appwrite Connection\n');
    
    const client = new sdk.Client();
    client
        .setEndpoint(process.env.APPWRITE_ENDPOINT)
        .setProject(process.env.APPWRITE_PROJECT_ID)
        .setKey(process.env.APPWRITE_API_KEY);
    
    const databases = new sdk.Databases(client);
    
    console.log('Configuration:');
    console.log(`  Endpoint: ${process.env.APPWRITE_ENDPOINT}`);
    console.log(`  Project: ${process.env.APPWRITE_PROJECT_ID}`);
    console.log(`  Database: ${process.env.DATABASE_ID}\n`);
    
    try {
        // 1. Test database connection
        console.log('1. Testing database connection...');
        const dbList = await databases.list();
        console.log(`✅ Connected! Found ${dbList.total} databases`);
        
        // 2. Check if our database exists
        console.log('\n2. Checking for snacktrack-db...');
        try {
            const db = await databases.get(process.env.DATABASE_ID);
            console.log(`✅ Database '${db.name}' found`);
        } catch (e) {
            console.log(`❌ Database '${process.env.DATABASE_ID}' not found`);
            console.log('   Creating database...');
            await databases.create(process.env.DATABASE_ID, 'SnackTrack Database');
            console.log('✅ Database created');
        }
        
        // 3. Check collections
        console.log('\n3. Checking collections...');
        const collections = await databases.listCollections(process.env.DATABASE_ID);
        console.log(`Found ${collections.total} collections:`);
        
        collections.collections.forEach(col => {
            console.log(`  - ${col.$id} (${col.name})`);
        });
        
        // 4. Check specific collections
        const requiredCollections = ['foodSubmissions', 'crawlState'];
        console.log('\n4. Checking required collections:');
        
        for (const colId of requiredCollections) {
            const exists = collections.collections.some(c => c.$id === colId);
            console.log(`  ${exists ? '✅' : '❌'} ${colId}`);
        }
        
        console.log('\n✅ Connection test completed successfully!');
        
    } catch (error) {
        console.error('\n❌ Connection test failed:', error.message);
        if (error.code === 401) {
            console.error('   Authentication error - check your API key');
        } else if (error.code === 404) {
            console.error('   Resource not found - check endpoint and project ID');
        } else {
            console.error('   Details:', error);
        }
    }
}

testConnection();