const sdk = require('node-appwrite');
const { OpenPetFoodFactsParser } = require('./parsers/openpetfoodfacts');
const { FressnapfParser } = require('./parsers/fressnapf');
const { ZooplusParser } = require('./parsers/zooplus');
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
        this.fressnapfParser = new FressnapfParser();
        this.zooplusParser = new ZooplusParser();
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
     * Crawl products from Fressnapf
     */
    async crawlFressnapf(crawlState) {
        const result = {
            processed: 0,
            duplicates: 0,
            errors: 0
        };
        
        try {
            // Get products from Fressnapf
            const products = await this.fressnapfParser.fetchProducts(
                crawlState.fressnapfPage || 1,
                10 // Products per page (less because web scraping is slower)
            );
            
            console.log(`Fetched ${products.length} products from Fressnapf page ${crawlState.fressnapfPage || 1}`);
            
            // Process each product
            for (const product of products) {
                try {
                    // Check for duplicate
                    if (await this.deduplicationService.isDuplicate(product.ean)) {
                        console.log(`Duplicate found: ${product.ean}`);
                        result.duplicates++;
                        continue;
                    }
                    
                    // Validate product data
                    if (!this.validator.validateProduct(product)) {
                        console.log(`Invalid product data: ${product.ean}`);
                        result.errors++;
                        continue;
                    }
                    
                    // Save product
                    await this.saveProduct(product);
                    console.log(`Saved product: ${product.ean} - ${product.product}`);
                    result.processed++;
                    
                } catch (error) {
                    console.error(`Error processing product: ${error.message}`);
                    result.errors++;
                }
                
                // Rate limiting between products
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
            
            // Update page number if products were found
            if (products.length > 0) {
                crawlState.fressnapfPage = (crawlState.fressnapfPage || 1) + 1;
            }
            
        } catch (error) {
            console.error('Fressnapf crawler error:', error);
            result.errors++;
        }
        
        return result;
    }
    
    /**
     * Crawl products from Zooplus
     */
    async crawlZooplus(crawlState) {
        const result = {
            processed: 0,
            duplicates: 0,
            errors: 0
        };
        
        try {
            // Get products from Zooplus
            const products = await this.zooplusParser.fetchProducts(
                crawlState.zooplusPage || 1,
                10 // Products per page (less because web scraping is slower)
            );
            
            console.log(`Fetched ${products.length} products from Zooplus page ${crawlState.zooplusPage || 1}`);
            
            // Process each product
            for (const product of products) {
                try {
                    // Check for duplicate
                    if (await this.deduplicationService.isDuplicate(product.ean)) {
                        console.log(`Duplicate found: ${product.ean}`);
                        result.duplicates++;
                        continue;
                    }
                    
                    // Validate product data
                    if (!this.validator.validateProduct(product)) {
                        console.log(`Invalid product data: ${product.ean}`);
                        result.errors++;
                        continue;
                    }
                    
                    // Save product
                    await this.saveProduct(product);
                    console.log(`Saved product: ${product.ean} - ${product.product}`);
                    result.processed++;
                    
                } catch (error) {
                    console.error(`Error processing product: ${error.message}`);
                    result.errors++;
                }
                
                // Rate limiting between products
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
            
            // Update page number if products were found
            if (products.length > 0) {
                crawlState.zooplusPage = (crawlState.zooplusPage || 1) + 1;
            }
            
        } catch (error) {
            console.error('Zooplus crawler error:', error);
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
            status: 'PENDING', // All crawled products need review
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