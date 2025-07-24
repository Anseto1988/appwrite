const sdk = require('node-appwrite');
const { OpenPetFoodFactsParser } = require('./parsers/openpetfoodfacts');
const { ProductValidator } = require('./utils/validation');
const { DeduplicationService } = require('./utils/deduplication');

/**
 * Main crawler manager that coordinates different data sources
 */
class CrawlerManager {
    constructor(databases, config, sessionId) {
        this.databases = databases;
        this.config = config;
        this.sessionId = sessionId;
        
        // Initialize services
        this.validator = new ProductValidator();
        this.deduplicationService = new DeduplicationService(databases, config);
        
        // Initialize parsers
        this.opffParser = new OpenPetFoodFactsParser();
    }
    
    /**
     * Crawl products from Open Pet Food Facts
     */
    async crawlOpenPetFoodFacts(crawlState) {
        const result = {
            processed: 0,
            duplicates: 0,
            errors: 0
        };
        
        try {
            // Get products from OPFF
            const products = await this.opffParser.fetchProducts(
                crawlState.opffPage || 1,
                20 // Products per page
            );
            
            console.log(`Fetched ${products.length} products from OPFF page ${crawlState.opffPage || 1}`);
            
            // Process each product
            for (const product of products) {
                try {
                    // Skip if no EAN
                    if (!product.ean) {
                        console.log('Skipping product without EAN');
                        continue;
                    }
                    
                    // Update last crawled EAN
                    crawlState.lastCrawledEan = product.ean;
                    
                    // Check for duplicate
                    const isDuplicate = await this.deduplicationService.isDuplicate(product.ean);
                    if (isDuplicate) {
                        result.duplicates++;
                        console.log(`Duplicate found: ${product.ean}`);
                        continue;
                    }
                    
                    // Validate product data
                    const validationResult = this.validator.validateProduct(product);
                    if (!validationResult.isValid) {
                        console.log(`Validation failed for ${product.ean}:`, validationResult.errors);
                        result.errors++;
                        continue;
                    }
                    
                    // Save to database
                    await this.saveProduct(product);
                    result.processed++;
                    
                    console.log(`Saved product: ${product.ean} - ${product.product}`);
                    
                } catch (error) {
                    console.error(`Error processing product: ${error.message}`);
                    result.errors++;
                }
                
                // Rate limiting between products
                await new Promise(resolve => setTimeout(resolve, 500));
            }
            
            // Update page number if products were found
            if (products.length > 0) {
                crawlState.opffPage = (crawlState.opffPage || 1) + 1;
            }
            
        } catch (error) {
            console.error('OPFF crawler error:', error);
            result.errors++;
        }
        
        return result;
    }
    
    /**
     * Save product to foodSubmissions collection
     */
    async saveProduct(productData) {
        const document = {
            userId: this.config.systemUserId,
            ean: productData.ean,
            brand: productData.brand || 'Unknown',
            product: productData.product || 'Unknown',
            protein: parseFloat(productData.protein) || 0,
            fat: parseFloat(productData.fat) || 0,
            crudeFiber: parseFloat(productData.crudeFiber) || 0,
            rawAsh: parseFloat(productData.rawAsh) || 0,
            moisture: parseFloat(productData.moisture) || 0,
            additives: productData.additives || null,
            imageUrl: productData.imageUrl || null,
            status: 'pending', // All crawled products need review
            submittedAt: new Date().toISOString(),
            reviewedAt: null,
            crawlSessionId: this.sessionId,
            source: productData.source || 'opff'
        };
        
        try {
            const response = await this.databases.createDocument(
                this.config.databaseId,
                this.config.submissionsCollectionId,
                sdk.ID.unique(),
                document
            );
            
            // Add to deduplication cache
            await this.deduplicationService.addToCache(productData.ean);
            
            return response;
        } catch (error) {
            console.error('Error saving product:', error);
            throw error;
        }
    }
}

module.exports = { CrawlerManager };