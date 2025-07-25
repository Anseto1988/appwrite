const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createBarcodeCollections() {
    console.log('Creating barcode feature collections...');
    
    try {
        // 1. Create Products Collection
        console.log('\nCreating products collection...');
        const productsCollection = await databases.createCollection(
            DATABASE_ID,
            'products',
            'Products',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Products attributes
        await databases.createStringAttribute(DATABASE_ID, 'products', 'barcode', 50, true);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'name', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'brand', 100, true);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'manufacturer', 100, false);
        await databases.createEnumAttribute(DATABASE_ID, 'products', 'category', 
            ['DRY_FOOD', 'WET_FOOD', 'TREATS', 'SUPPLEMENTS', 'TOYS', 'ACCESSORIES', 'MEDICATION'], true);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'subCategory', 50, false);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'description', 1000, true);
        await databases.createStringAttribute(DATABASE_ID, 'products', 'images', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'products', 'nutritionalInfo', 5000, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'products', 'ingredients', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'products', 'allergens', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'products', 'certifications', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'products', 'variants', 3000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'products', 'metadata', 3000, true); // JSON
        await databases.createEnumAttribute(DATABASE_ID, 'products', 'source', 
            ['OFFICIAL_DATABASE', 'USER_SUBMISSION', 'OCR_EXTRACTION', 'MANUAL_ENTRY'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'products', 'verificationStatus', 
            ['UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED'], true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'products', 'lastUpdated', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'products', 'barcodeLookup', 'key', ['barcode']);
        await databases.createIndex(DATABASE_ID, 'products', 'productSearch', 'fulltext', ['name', 'brand']);
        await databases.createIndex(DATABASE_ID, 'products', 'categoryFilter', 'key', ['category']);
        
        console.log('✓ Products collection created');
        
        // 2. Create Barcode History Collection
        console.log('\nCreating barcode_history collection...');
        const barcodeHistoryCollection = await databases.createCollection(
            DATABASE_ID,
            'barcode_history',
            'Barcode History',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Barcode History attributes
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'barcode', 50, true);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'productId', 36, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'barcode_history', 'scanTimestamp', true);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'scanLocation', 500, false); // JSON
        await databases.createEnumAttribute(DATABASE_ID, 'barcode_history', 'action', 
            ['VIEW', 'PURCHASE', 'COMPARE', 'SAVE', 'SHARE'], true);
        await databases.createFloatAttribute(DATABASE_ID, 'barcode_history', 'quantity', false);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_history', 'notes', 500, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'barcode_history', 'userHistory', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'barcode_history', 'dogHistory', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'barcode_history', 'historyByTime', 'key', ['scanTimestamp']);
        
        console.log('✓ Barcode history collection created');
        
        // 3. Create Product Comparisons Collection
        console.log('\nCreating product_comparisons collection...');
        const productComparisonsCollection = await databases.createCollection(
            DATABASE_ID,
            'product_comparisons',
            'Product Comparisons',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Product Comparisons attributes
        await databases.createStringAttribute(DATABASE_ID, 'product_comparisons', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'product_comparisons', 'productIds', 500, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'product_comparisons', 'comparisonCriteria', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'product_comparisons', 'results', 5000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'product_comparisons', 'recommendation', 2000, false); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'product_comparisons', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'product_comparisons', 'userComparisons', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'product_comparisons', 'comparisonsByTime', 'key', ['createdAt']);
        
        console.log('✓ Product comparisons collection created');
        
        // 4. Create Product Inventory Collection
        console.log('\nCreating product_inventory collection...');
        const productInventoryCollection = await databases.createCollection(
            DATABASE_ID,
            'product_inventory',
            'Product Inventory',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Product Inventory attributes
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'productId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'currentStock', 500, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'consumptionRate', 500, false); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'reorderSettings', 1000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'storageLocation', 100, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'product_inventory', 'expirationDate', false);
        await databases.createStringAttribute(DATABASE_ID, 'product_inventory', 'purchaseHistory', 5000, true); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'product_inventory', 'userInventory', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'product_inventory', 'inventoryByProduct', 'key', ['productId']);
        await databases.createIndex(DATABASE_ID, 'product_inventory', 'expirationTracking', 'key', ['expirationDate']);
        
        console.log('✓ Product inventory collection created');
        
        // 5. Create Barcode Analytics Collection
        console.log('\nCreating barcode_analytics collection...');
        const barcodeAnalyticsCollection = await databases.createCollection(
            DATABASE_ID,
            'barcode_analytics',
            'Barcode Analytics',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Barcode Analytics attributes
        await databases.createStringAttribute(DATABASE_ID, 'barcode_analytics', 'userId', 36, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'barcode_analytics', 'period', true);
        await databases.createStringAttribute(DATABASE_ID, 'barcode_analytics', 'scanStatistics', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'barcode_analytics', 'productStatistics', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'barcode_analytics', 'shoppingStatistics', 3000, true); // JSON
        await databases.createStringAttribute(DATABASE_ID, 'barcode_analytics', 'trendAnalysis', 3000, true); // JSON
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'barcode_analytics', 'userAnalytics', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'barcode_analytics', 'analyticsByPeriod', 'key', ['period']);
        
        console.log('✓ Barcode analytics collection created');
        
        // 6. Create Product Recommendations Collection
        console.log('\nCreating product_recommendations collection...');
        const productRecommendationsCollection = await databases.createCollection(
            DATABASE_ID,
            'product_recommendations',
            'Product Recommendations',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Product Recommendations attributes
        await databases.createStringAttribute(DATABASE_ID, 'product_recommendations', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'product_recommendations', 'recommendedProductId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'product_recommendations', 'reason', 500, true);
        await databases.createFloatAttribute(DATABASE_ID, 'product_recommendations', 'score', true, 0.0, 1.0);
        await databases.createStringAttribute(DATABASE_ID, 'product_recommendations', 'factors', 2000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'product_recommendations', 'alternativeProductIds', 500, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'product_recommendations', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'product_recommendations', 'dogRecommendations', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'product_recommendations', 'recommendationsByScore', 'key', ['score']);
        
        console.log('✓ Product recommendations collection created');
        
        // 7. Create Allergen Alerts Collection
        console.log('\nCreating allergen_alerts collection...');
        const allergenAlertsCollection = await databases.createCollection(
            DATABASE_ID,
            'allergen_alerts',
            'Allergen Alerts',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Allergen Alerts attributes
        await databases.createStringAttribute(DATABASE_ID, 'allergen_alerts', 'dogId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'allergen_alerts', 'productId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'allergen_alerts', 'detectedAllergens', 2000, true); // JSON array
        await databases.createEnumAttribute(DATABASE_ID, 'allergen_alerts', 'severity', 
            ['INFO', 'WARNING', 'DANGER', 'CRITICAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'allergen_alerts', 'recommendation', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'allergen_alerts', 'alternativeProductIds', 500, true); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'allergen_alerts', 'detectedAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'allergen_alerts', 'dogAlerts', 'key', ['dogId']);
        await databases.createIndex(DATABASE_ID, 'allergen_alerts', 'alertsBySeverity', 'key', ['severity']);
        
        console.log('✓ Allergen alerts collection created');
        
        // 8. Create OCR Results Collection
        console.log('\nCreating ocr_results collection...');
        const ocrResultsCollection = await databases.createCollection(
            DATABASE_ID,
            'ocr_results',
            'OCR Results',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // OCR Results attributes
        await databases.createStringAttribute(DATABASE_ID, 'ocr_results', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'ocr_results', 'barcode', 50, false);
        await databases.createStringAttribute(DATABASE_ID, 'ocr_results', 'extractedText', 5000, true);
        await databases.createFloatAttribute(DATABASE_ID, 'ocr_results', 'confidence', true, 0.0, 1.0);
        await databases.createStringAttribute(DATABASE_ID, 'ocr_results', 'language', 5, true, 'de');
        await databases.createStringAttribute(DATABASE_ID, 'ocr_results', 'extractedData', 5000, true); // JSON
        await databases.createIntegerAttribute(DATABASE_ID, 'ocr_results', 'processingTime', true, 0, 999999);
        await databases.createDatetimeAttribute(DATABASE_ID, 'ocr_results', 'processedAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'ocr_results', 'userOCR', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'ocr_results', 'ocrByBarcode', 'key', ['barcode']);
        
        console.log('✓ OCR results collection created');
        
        // 9. Create Shopping Lists Collection
        console.log('\nCreating shopping_lists collection...');
        const shoppingListsCollection = await databases.createCollection(
            DATABASE_ID,
            'shopping_lists',
            'Shopping Lists',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Shopping Lists attributes
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'name', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'items', 10000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'stores', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'shopping_lists', 'totalEstimate', 500, false); // JSON
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_lists', 'createdAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'shopping_lists', 'completedAt', false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'shopping_lists', 'userLists', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'shopping_lists', 'listsByDate', 'key', ['createdAt']);
        
        console.log('✓ Shopping lists collection created');
        
        // 10. Create Stores Collection
        console.log('\nCreating stores collection...');
        const storesCollection = await databases.createCollection(
            DATABASE_ID,
            'stores',
            'Stores',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Stores attributes
        await databases.createStringAttribute(DATABASE_ID, 'stores', 'name', 200, true);
        await databases.createStringAttribute(DATABASE_ID, 'stores', 'address', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'stores', 'location', 500, false); // JSON
        await databases.createEnumAttribute(DATABASE_ID, 'stores', 'type', 
            ['PET_STORE', 'SUPERMARKET', 'ONLINE', 'VETERINARY', 'SPECIALTY'], true);
        await databases.createEnumAttribute(DATABASE_ID, 'stores', 'priceLevel', 
            ['BUDGET', 'MODERATE', 'PREMIUM', 'LUXURY'], true);
        await databases.createStringAttribute(DATABASE_ID, 'stores', 'availableProducts', 5000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'stores', 'openingHours', 2000, true); // JSON map
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'stores', 'storesByType', 'key', ['type']);
        await databases.createIndex(DATABASE_ID, 'stores', 'storeSearch', 'fulltext', ['name', 'address']);
        
        console.log('✓ Stores collection created');
        
        console.log('\n✅ All barcode feature collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingBarcodeAttributes();
        }
    }
}

async function addMissingBarcodeAttributes() {
    try {
        console.log('\nChecking for missing attributes in existing collections...');
        
        // This would need to be implemented based on what attributes are missing
        // For now, we'll just log that we tried
        console.log('Please manually verify that all required attributes exist in the collections.');
        
    } catch (error) {
        console.error('Error adding missing attributes:', error);
    }
}

// Run the setup
createBarcodeCollections()
    .then(() => {
        console.log('\nBarcode features database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });