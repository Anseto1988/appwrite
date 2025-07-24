require('dotenv').config();
const sdk = require('node-appwrite');
const { CrawlerManager } = require('./crawler');
const { CrawlStateManager } = require('./utils/crawlState');

/**
 * Local Dog Food Crawler
 * Runs the crawler function locally instead of in Appwrite
 */
async function runLocalCrawler() {
    console.log('🐕 Local Dog Food Crawler Starting...\n');
    
    // Initialize Appwrite SDK
    const client = new sdk.Client();
    const databases = new sdk.Databases(client);
    
    // Configure client from environment variables
    client
        .setEndpoint(process.env.APPWRITE_ENDPOINT)
        .setProject(process.env.APPWRITE_PROJECT_ID)
        .setKey(process.env.APPWRITE_API_KEY);
    
    // Configuration
    const config = {
        databaseId: process.env.DATABASE_ID,
        submissionsCollectionId: process.env.SUBMISSIONS_COLLECTION_ID,
        crawlStateCollectionId: process.env.CRAWL_STATE_COLLECTION_ID,
        maxRuntime: 5 * 60 * 1000, // 5 minutes for local run
        maxProductsPerRun: parseInt(process.env.MAX_PRODUCTS_PER_RUN || '10', 10),
        systemUserId: process.env.CRAWLER_USER_ID
    };
    
    // Initialize session
    const sessionId = sdk.ID.unique();
    const startTime = Date.now();
    
    console.log(`Session ID: ${sessionId}`);
    console.log(`Max runtime: ${config.maxRuntime / 1000}s`);
    console.log(`Max products: ${config.maxProductsPerRun}`);
    console.log(`Endpoint: ${process.env.APPWRITE_ENDPOINT}`);
    console.log(`Project: ${process.env.APPWRITE_PROJECT_ID}\n`);
    
    try {
        // Test connection first
        console.log('Testing Appwrite connection...');
        const testDb = await databases.list();
        console.log(`✅ Connected! Found ${testDb.total} databases\n`);
        
        // Initialize managers
        const stateManager = new CrawlStateManager(databases, config);
        const crawlerManager = new CrawlerManager(databases, config, sessionId);
        
        // Load previous crawl state
        console.log('Loading crawl state...');
        const crawlState = await stateManager.loadState();
        console.log('Crawl state:', JSON.stringify(crawlState, null, 2), '\n');
        
        // Track results
        const results = {
            openPetFoodFacts: 0,
            fressnapf: 0,
            zooplus: 0,
            duplicates: 0,
            errors: 0,
            totalProcessed: 0
        };
        
        console.log('Starting crawl loop...\n');
        
        // Main crawling loop
        while (Date.now() - startTime < config.maxRuntime && 
               results.totalProcessed < config.maxProductsPerRun) {
            
            // Check remaining time
            const remainingTime = config.maxRuntime - (Date.now() - startTime);
            const remainingSeconds = Math.round(remainingTime / 1000);
            
            if (remainingTime < 30000) { // Less than 30 seconds
                console.log(`\n⏱️  Less than 30 seconds remaining (${remainingSeconds}s), stopping crawl`);
                break;
            }
            
            console.log(`\n📊 Progress: ${results.totalProcessed}/${config.maxProductsPerRun} products`);
            console.log(`⏱️  Time remaining: ${remainingSeconds}s`);
            
            // Crawl based on current source
            switch (crawlState.currentSource) {
                case 'opff':
                    console.log('🔍 Crawling Open Pet Food Facts...');
                    const opffResult = await crawlerManager.crawlOpenPetFoodFacts(crawlState);
                    
                    console.log(`  Processed: ${opffResult.processed}`);
                    console.log(`  Duplicates: ${opffResult.duplicates}`);
                    console.log(`  Errors: ${opffResult.errors}`);
                    
                    results.openPetFoodFacts += opffResult.processed;
                    results.duplicates += opffResult.duplicates;
                    results.errors += opffResult.errors;
                    results.totalProcessed += opffResult.processed;
                    
                    // If no more products from OPFF, switch to next source
                    if (opffResult.processed === 0 && opffResult.errors === 0) {
                        console.log('  No more products from OPFF, switching to next source');
                        crawlState.currentSource = 'fressnapf';
                        crawlState.lastCrawledUrl = null;
                        crawlState.lastCrawledEan = null;
                    }
                    break;
                    
                case 'fressnapf':
                    console.log('🔍 Crawling Fressnapf...');
                    const fressnapfResult = await crawlerManager.crawlFressnapf(crawlState);
                    results.fressnapf += fressnapfResult.processed;
                    results.duplicates += fressnapfResult.duplicates;
                    results.errors += fressnapfResult.errors;
                    results.totalProcessed += fressnapfResult.processed;
                    
                    // If no more products from Fressnapf, switch to next source
                    if (fressnapfResult.processed === 0 && fressnapfResult.errors === 0) {
                        console.log('  No more products from Fressnapf, switching to Zooplus');
                        crawlState.currentSource = 'zooplus';
                        crawlState.lastCrawledUrl = null;
                        crawlState.lastCrawledEan = null;
                    }
                    break;
                    
                case 'zooplus':
                    console.log('🔍 Crawling Zooplus...');
                    const zooplusResult = await crawlerManager.crawlZooplus(crawlState);
                    results.zooplus += zooplusResult.processed;
                    results.duplicates += zooplusResult.duplicates;
                    results.errors += zooplusResult.errors;
                    results.totalProcessed += zooplusResult.processed;
                    
                    // If no more products from Zooplus, reset to beginning
                    if (zooplusResult.processed === 0 && zooplusResult.errors === 0) {
                        console.log('  No more products from Zooplus, resetting to OPFF');
                        crawlState.currentSource = 'opff';
                        crawlState.lastCrawledUrl = null;
                        crawlState.lastCrawledEan = null;
                    }
                    break;
                    
                default:
                    console.log('❓ Unknown source, resetting to OPFF');
                    crawlState.currentSource = 'opff';
            }
            
            // Update total processed in state
            crawlState.totalProcessed = (crawlState.totalProcessed || 0) + results.totalProcessed;
            
            // Save state periodically
            if (results.totalProcessed % 5 === 0 && results.totalProcessed > 0) {
                console.log('\n💾 Saving crawl state...');
                await stateManager.saveState(crawlState);
            }
            
            // Rate limiting between operations
            await new Promise(resolve => setTimeout(resolve, 2000));
        }
        
        // Final state save
        console.log('\n💾 Saving final crawl state...');
        crawlState.lastRunDate = new Date().toISOString();
        await stateManager.saveState(crawlState);
        
        // Calculate duration
        const duration = Date.now() - startTime;
        
        // Print summary
        console.log('\n' + '='.repeat(60));
        console.log('📊 CRAWL SESSION COMPLETED');
        console.log('='.repeat(60));
        console.log(`Session ID: ${sessionId}`);
        console.log(`Duration: ${Math.round(duration / 1000)}s`);
        console.log(`\nResults:`);
        console.log(`  Total processed: ${results.totalProcessed}`);
        console.log(`  New products: ${results.totalProcessed - results.duplicates}`);
        console.log(`  Duplicates: ${results.duplicates}`);
        console.log(`  Errors: ${results.errors}`);
        console.log(`  Open Pet Food Facts: ${results.openPetFoodFacts}`);
        console.log('\n✅ Local crawler finished successfully!');
        
    } catch (error) {
        console.error('\n❌ Crawler error:', error.message);
        console.error('Stack:', error.stack);
        
        // Try to save error state
        try {
            const stateManager = new CrawlStateManager(databases, config);
            const crawlState = await stateManager.loadState();
            crawlState.lastError = error.message;
            crawlState.lastErrorDate = new Date().toISOString();
            await stateManager.saveState(crawlState);
            console.log('💾 Error state saved');
        } catch (stateError) {
            console.error('Failed to save error state:', stateError.message);
        }
        
        process.exit(1);
    }
}

// Run the crawler
runLocalCrawler().catch(console.error);