const sdk = require('node-appwrite');
const { CrawlerManager } = require('./crawler');
const { CrawlStateManager } = require('./utils/crawlState');

/**
 * Appwrite Cloud Function for crawling dog food data
 * Runs nightly for up to 55 minutes to collect product information
 */
module.exports = async function(req, res) {
    // Initialize Appwrite SDK
    const client = new sdk.Client();
    const databases = new sdk.Databases(client);
    
    // Configure client
    client
        .setEndpoint(process.env.APPWRITE_ENDPOINT || req.variables.APPWRITE_ENDPOINT)
        .setProject(process.env.APPWRITE_FUNCTION_PROJECT_ID || req.variables.APPWRITE_FUNCTION_PROJECT_ID)
        .setKey(process.env.APPWRITE_API_KEY || req.variables.APPWRITE_API_KEY);
    
    // Configuration
    const config = {
        databaseId: process.env.DATABASE_ID || 'snacktrack-db',
        submissionsCollectionId: process.env.SUBMISSIONS_COLLECTION_ID || 'foodSubmissions',
        crawlStateCollectionId: process.env.CRAWL_STATE_COLLECTION_ID || 'crawlState',
        maxRuntime: 55 * 60 * 1000, // 55 minutes
        maxProductsPerRun: parseInt(process.env.MAX_PRODUCTS_PER_RUN || '500', 10),
        systemUserId: process.env.CRAWLER_USER_ID || 'system_crawler'
    };
    
    // Initialize session
    const sessionId = sdk.ID.unique();
    const startTime = Date.now();
    
    console.log(`Starting crawler session: ${sessionId}`);
    console.log(`Max runtime: ${config.maxRuntime}ms`);
    console.log(`Max products: ${config.maxProductsPerRun}`);
    
    try {
        // Initialize managers
        const stateManager = new CrawlStateManager(databases, config);
        const crawlerManager = new CrawlerManager(databases, config, sessionId);
        
        // Load previous crawl state
        const crawlState = await stateManager.loadState();
        console.log('Loaded crawl state:', JSON.stringify(crawlState));
        
        // Track results
        const results = {
            openPetFoodFacts: 0,
            fressnapf: 0,
            zooplus: 0,
            duplicates: 0,
            errors: 0,
            totalProcessed: 0
        };
        
        // Main crawling loop
        while (Date.now() - startTime < config.maxRuntime && 
               results.totalProcessed < config.maxProductsPerRun) {
            
            // Check remaining time
            const remainingTime = config.maxRuntime - (Date.now() - startTime);
            if (remainingTime < 60000) { // Less than 1 minute
                console.log('Less than 1 minute remaining, stopping crawl');
                break;
            }
            
            // Crawl based on current source
            switch (crawlState.currentSource) {
                case 'opff':
                    console.log('Crawling Open Pet Food Facts...');
                    const opffResult = await crawlerManager.crawlOpenPetFoodFacts(crawlState);
                    results.openPetFoodFacts += opffResult.processed;
                    results.duplicates += opffResult.duplicates;
                    results.errors += opffResult.errors;
                    results.totalProcessed += opffResult.processed;
                    
                    // If no more products from OPFF, switch to next source
                    if (opffResult.processed === 0 && opffResult.errors === 0) {
                        console.log('No more products from OPFF, switching to Fressnapf');
                        crawlState.currentSource = 'fressnapf';
                        crawlState.lastCrawledUrl = null;
                        crawlState.lastCrawledEan = null;
                    }
                    break;
                    
                case 'fressnapf':
                    // TODO: Implement Fressnapf crawler in Phase 2
                    console.log('Fressnapf crawler not yet implemented');
                    crawlState.currentSource = 'zooplus';
                    break;
                    
                case 'zooplus':
                    // TODO: Implement Zooplus crawler in Phase 2
                    console.log('Zooplus crawler not yet implemented');
                    crawlState.currentSource = 'opff'; // Reset to beginning
                    break;
                    
                default:
                    console.log('Unknown source, resetting to OPFF');
                    crawlState.currentSource = 'opff';
            }
            
            // Update total processed in state
            crawlState.totalProcessed = (crawlState.totalProcessed || 0) + results.totalProcessed;
            
            // Save state periodically (every 10 products)
            if (results.totalProcessed % 10 === 0 && results.totalProcessed > 0) {
                await stateManager.saveState(crawlState);
            }
            
            // Rate limiting between source switches
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
        
        // Final state save
        crawlState.lastRunDate = new Date().toISOString();
        await stateManager.saveState(crawlState);
        
        // Calculate duration
        const duration = Date.now() - startTime;
        
        // Log summary
        console.log('Crawl session completed:', {
            sessionId,
            duration: `${Math.round(duration / 1000)}s`,
            results
        });
        
        // Return success response
        res.json({
            success: true,
            sessionId,
            duration,
            results,
            message: `Successfully processed ${results.totalProcessed} products (${results.duplicates} duplicates, ${results.errors} errors)`
        });
        
    } catch (error) {
        console.error('Crawler error:', error);
        
        // Try to save current state
        try {
            const stateManager = new CrawlStateManager(databases, config);
            const crawlState = await stateManager.loadState();
            crawlState.lastError = error.message;
            crawlState.lastErrorDate = new Date().toISOString();
            await stateManager.saveState(crawlState);
        } catch (stateError) {
            console.error('Failed to save error state:', stateError);
        }
        
        // Return error response
        res.json({
            success: false,
            sessionId,
            error: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};