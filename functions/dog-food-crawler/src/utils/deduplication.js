const sdk = require('node-appwrite');

/**
 * Deduplication service to prevent duplicate products
 * Uses EAN as the primary key for deduplication
 */
class DeduplicationService {
    constructor(databases, config) {
        this.databases = databases;
        this.config = config;
        
        // In-memory cache for current session
        this.sessionCache = new Set();
        
        // Cache configuration
        this.cacheSize = 1000;
        this.cacheTTL = 60 * 60 * 1000; // 1 hour
    }
    
    /**
     * Check if a product with given EAN already exists
     */
    async isDuplicate(ean) {
        if (!ean) return false;
        
        // Check session cache first
        if (this.sessionCache.has(ean)) {
            return true;
        }
        
        try {
            // Check database
            const response = await this.databases.listDocuments(
                this.config.databaseId,
                this.config.submissionsCollectionId,
                [
                    sdk.Query.equal('ean', ean),
                    sdk.Query.limit(1)
                ]
            );
            
            const exists = response.documents.length > 0;
            
            // Add to session cache if exists
            if (exists) {
                this.addToCache(ean);
            }
            
            return exists;
            
        } catch (error) {
            console.error(`Error checking duplicate for EAN ${ean}:`, error.message);
            // In case of error, assume not duplicate to allow retry
            return false;
        }
    }
    
    /**
     * Batch check for duplicates
     * More efficient for checking multiple EANs
     */
    async batchCheckDuplicates(eanList) {
        if (!eanList || eanList.length === 0) {
            return new Map();
        }
        
        const duplicates = new Map();
        
        // First check session cache
        for (const ean of eanList) {
            if (this.sessionCache.has(ean)) {
                duplicates.set(ean, true);
            }
        }
        
        // Get uncached EANs
        const uncachedEans = eanList.filter(ean => !duplicates.has(ean));
        
        if (uncachedEans.length === 0) {
            return duplicates;
        }
        
        try {
            // Process in batches of 25 (Appwrite limit for Query.equal array)
            const batchSize = 25;
            
            for (let i = 0; i < uncachedEans.length; i += batchSize) {
                const batch = uncachedEans.slice(i, i + batchSize);
                
                const response = await this.databases.listDocuments(
                    this.config.databaseId,
                    this.config.submissionsCollectionId,
                    [
                        sdk.Query.equal('ean', batch),
                        sdk.Query.limit(100)
                    ]
                );
                
                // Mark found EANs as duplicates
                for (const doc of response.documents) {
                    duplicates.set(doc.ean, true);
                    this.addToCache(doc.ean);
                }
            }
            
        } catch (error) {
            console.error('Error in batch duplicate check:', error.message);
        }
        
        return duplicates;
    }
    
    /**
     * Add EAN to session cache
     */
    addToCache(ean) {
        if (!ean) return;
        
        // Implement simple LRU by clearing cache if too large
        if (this.sessionCache.size >= this.cacheSize) {
            // Clear oldest entries (simple approach)
            const entriesToRemove = this.sessionCache.size - this.cacheSize + 100;
            const iterator = this.sessionCache.values();
            
            for (let i = 0; i < entriesToRemove; i++) {
                const value = iterator.next().value;
                if (value) {
                    this.sessionCache.delete(value);
                }
            }
        }
        
        this.sessionCache.add(ean);
    }
    
    /**
     * Clear session cache
     */
    clearCache() {
        this.sessionCache.clear();
    }
    
    /**
     * Get cache statistics
     */
    getCacheStats() {
        return {
            size: this.sessionCache.size,
            maxSize: this.cacheSize
        };
    }
    
    /**
     * Find similar products by name
     * Useful for detecting potential duplicates with different EANs
     */
    async findSimilarProducts(productName, brand) {
        if (!productName) return [];
        
        try {
            // Search for products with similar names
            const queries = [
                sdk.Query.limit(10)
            ];
            
            // Add brand filter if available
            if (brand) {
                queries.push(sdk.Query.equal('brand', brand));
            }
            
            // Search query would need full-text search support
            // For now, we'll just get products from the same brand
            const response = await this.databases.listDocuments(
                this.config.databaseId,
                this.config.submissionsCollectionId,
                queries
            );
            
            // Simple similarity check
            const similar = response.documents.filter(doc => {
                const similarity = this.calculateSimilarity(
                    productName.toLowerCase(),
                    doc.product.toLowerCase()
                );
                return similarity > 0.8; // 80% similarity threshold
            });
            
            return similar;
            
        } catch (error) {
            console.error('Error finding similar products:', error.message);
            return [];
        }
    }
    
    /**
     * Calculate simple string similarity (Dice coefficient)
     */
    calculateSimilarity(str1, str2) {
        if (str1 === str2) return 1;
        if (str1.length < 2 || str2.length < 2) return 0;
        
        // Create bigrams
        const getBigrams = (str) => {
            const bigrams = new Set();
            for (let i = 0; i < str.length - 1; i++) {
                bigrams.add(str.substr(i, 2));
            }
            return bigrams;
        };
        
        const bigrams1 = getBigrams(str1);
        const bigrams2 = getBigrams(str2);
        
        // Calculate intersection
        let intersection = 0;
        for (const bigram of bigrams1) {
            if (bigrams2.has(bigram)) {
                intersection++;
            }
        }
        
        // Dice coefficient
        return (2 * intersection) / (bigrams1.size + bigrams2.size);
    }
}

module.exports = { DeduplicationService };