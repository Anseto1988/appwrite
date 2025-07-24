require('dotenv').config();
const sdk = require('node-appwrite');
const { CrawlStateManager } = require('./utils/crawlState');

async function resetFressnapfState() {
    console.log('🔧 Resetting Fressnapf crawl state...\n');
    
    // Initialize Appwrite SDK
    const client = new sdk.Client();
    const databases = new sdk.Databases(client);
    
    client
        .setEndpoint(process.env.APPWRITE_ENDPOINT)
        .setProject(process.env.APPWRITE_PROJECT_ID)
        .setKey(process.env.APPWRITE_API_KEY);
    
    const config = {
        databaseId: process.env.DATABASE_ID,
        crawlStateCollectionId: process.env.CRAWL_STATE_COLLECTION_ID
    };
    
    try {
        const stateManager = new CrawlStateManager(databases, config);
        
        // Load current state
        const currentState = await stateManager.loadState();
        console.log('Current state:', currentState);
        
        // Reset Fressnapf page to 1
        currentState.fressnapfPage = 1;
        currentState.currentSource = 'fressnapf'; // Set to Fressnapf so it starts there
        
        // Save updated state
        await stateManager.saveState(currentState);
        console.log('\n✅ Fressnapf state reset successfully!');
        console.log('Next crawl will start from Fressnapf page 1');
        
    } catch (error) {
        console.error('❌ Error resetting state:', error.message);
    }
}

resetFressnapfState().catch(console.error);