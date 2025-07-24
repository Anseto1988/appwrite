const sdk = require('node-appwrite');

/**
 * Manages crawler state persistence
 * Allows resuming crawls after interruption
 */
class CrawlStateManager {
    constructor(databases, config) {
        this.databases = databases;
        this.config = config;
        this.stateDocumentId = 'crawler_state_v1'; // Fixed ID for state document
    }
    
    /**
     * Load the current crawl state
     */
    async loadState() {
        try {
            const document = await this.databases.getDocument(
                this.config.databaseId,
                this.config.crawlStateCollectionId,
                this.stateDocumentId
            );
            
            console.log('Loaded existing crawl state');
            
            return {
                currentSource: document.currentSource || 'opff',
                lastCrawledUrl: document.lastCrawledUrl || null,
                lastCrawledEan: document.lastCrawledEan || null,
                opffPage: document.opffPage || 1,
                fressnapfPage: document.fressnapfPage || 1,
                zooplusPage: document.zooplusPage || 1,
                totalProcessed: document.totalProcessed || 0,
                lastRunDate: document.lastRunDate || null,
                lastError: document.lastError || null,
                lastErrorDate: document.lastErrorDate || null,
                statistics: typeof document.statistics === 'string' 
                    ? JSON.parse(document.statistics) 
                    : document.statistics || {}
            };
            
        } catch (error) {
            if (error.code === 404) {
                console.log('No existing crawl state found, creating new');
                return this.createDefaultState();
            }
            
            console.error('Error loading crawl state:', error);
            return this.createDefaultState();
        }
    }
    
    /**
     * Save the current crawl state
     */
    async saveState(state) {
        try {
            // Prepare state document
            const stateDocument = {
                currentSource: state.currentSource || 'opff',
                lastCrawledUrl: state.lastCrawledUrl || null,
                lastCrawledEan: state.lastCrawledEan || null,
                opffPage: state.opffPage || 1,
                fressnapfPage: state.fressnapfPage || 1,
                zooplusPage: state.zooplusPage || 1,
                totalProcessed: state.totalProcessed || 0,
                lastRunDate: state.lastRunDate || new Date().toISOString(),
                lastError: state.lastError || null,
                lastErrorDate: state.lastErrorDate || null,
                statistics: JSON.stringify(state.statistics || {}),
                updatedAt: new Date().toISOString()
            };
            
            try {
                // Try to update existing document
                await this.databases.updateDocument(
                    this.config.databaseId,
                    this.config.crawlStateCollectionId,
                    this.stateDocumentId,
                    stateDocument
                );
                
                console.log('Crawl state updated');
                
            } catch (updateError) {
                if (updateError.code === 404) {
                    // Document doesn't exist, create it
                    await this.databases.createDocument(
                        this.config.databaseId,
                        this.config.crawlStateCollectionId,
                        this.stateDocumentId,
                        stateDocument
                    );
                    
                    console.log('Crawl state created');
                } else {
                    throw updateError;
                }
            }
            
        } catch (error) {
            console.error('Error saving crawl state:', error);
            throw error;
        }
    }
    
    /**
     * Create default initial state
     */
    createDefaultState() {
        return {
            currentSource: 'opff',
            lastCrawledUrl: null,
            lastCrawledEan: null,
            opffPage: 1,
            fressnapfPage: 1,
            zooplusPage: 1,
            totalProcessed: 0,
            lastRunDate: null,
            lastError: null,
            lastErrorDate: null,
            statistics: {
                totalProducts: 0,
                bySource: {
                    opff: 0,
                    fressnapf: 0,
                    zooplus: 0
                },
                byStatus: {
                    pending: 0,
                    approved: 0,
                    rejected: 0
                }
            }
        };
    }
    
    /**
     * Reset state for a specific source
     */
    async resetSource(source) {
        const state = await this.loadState();
        
        switch (source) {
            case 'opff':
                state.opffPage = 1;
                break;
            case 'fressnapf':
                state.fressnapfPage = 1;
                break;
            case 'zooplus':
                state.zooplusPage = 1;
                break;
        }
        
        state.lastCrawledUrl = null;
        state.lastCrawledEan = null;
        
        await this.saveState(state);
    }
    
    /**
     * Update statistics
     */
    async updateStatistics(stats) {
        const state = await this.loadState();
        
        // Merge statistics
        state.statistics = {
            ...state.statistics,
            ...stats,
            lastUpdated: new Date().toISOString()
        };
        
        await this.saveState(state);
    }
    
    /**
     * Get crawl history
     */
    async getCrawlHistory(limit = 10) {
        try {
            const response = await this.databases.listDocuments(
                this.config.databaseId,
                this.config.submissionsCollectionId,
                [
                    sdk.Query.orderDesc('submittedAt'),
                    sdk.Query.limit(limit)
                ]
            );
            
            // Group by crawl session
            const sessionMap = new Map();
            
            for (const doc of response.documents) {
                const sessionId = doc.crawlSessionId || 'unknown';
                
                if (!sessionMap.has(sessionId)) {
                    sessionMap.set(sessionId, {
                        sessionId: sessionId,
                        count: 0,
                        firstProduct: doc.submittedAt,
                        lastProduct: doc.submittedAt,
                        products: []
                    });
                }
                
                const session = sessionMap.get(sessionId);
                session.count++;
                session.products.push({
                    ean: doc.ean,
                    product: doc.product,
                    brand: doc.brand
                });
                
                // Update time range
                if (doc.submittedAt < session.firstProduct) {
                    session.firstProduct = doc.submittedAt;
                }
                if (doc.submittedAt > session.lastProduct) {
                    session.lastProduct = doc.submittedAt;
                }
            }
            
            return Array.from(sessionMap.values());
            
        } catch (error) {
            console.error('Error getting crawl history:', error);
            return [];
        }
    }
}

module.exports = { CrawlStateManager };