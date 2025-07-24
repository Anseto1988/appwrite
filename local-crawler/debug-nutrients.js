require('dotenv').config();
const axios = require('axios');

async function debugNutrients() {
    console.log('🔍 Debugging Open Pet Food Facts Nutrient Data\n');
    
    const baseUrl = 'https://world.openpetfoodfacts.org';
    
    try {
        // Fetch some dog food products
        console.log('Fetching dog food products...\n');
        const response = await axios.get(`${baseUrl}/category/en:dog-food.json`, {
            params: {
                page: 1,
                page_size: 5,
                fields: 'code,product_name,brands,nutriments,ingredients_text_en,ingredients_text_de,ingredients_text'
            },
            headers: {
                'User-Agent': 'SnackTrack-Debug/1.0'
            }
        });
        
        if (!response.data || !response.data.products) {
            console.log('No products found');
            return;
        }
        
        console.log(`Found ${response.data.products.length} products\n`);
        
        // Analyze each product
        response.data.products.forEach((product, index) => {
            console.log('='.repeat(60));
            console.log(`Product ${index + 1}: ${product.product_name || 'Unknown'}`);
            console.log(`Brand: ${product.brands || 'Unknown'}`);
            console.log(`EAN: ${product.code}`);
            console.log('\nNutriments object:');
            console.log(JSON.stringify(product.nutriments, null, 2));
            
            // Check specific nutrient fields
            console.log('\nExtracted values:');
            const nutriments = product.nutriments || {};
            
            // Check all possible protein variations
            console.log('\nProtein variations:');
            ['proteins', 'protein', 'proteins_100g', 'protein_100g', 'proteins_value', 'protein_value'].forEach(key => {
                if (nutriments[key] !== undefined) {
                    console.log(`  ${key}: ${nutriments[key]}`);
                }
            });
            
            // Check all possible fat variations
            console.log('\nFat variations:');
            ['fat', 'fats', 'fat_100g', 'fats_100g', 'fat_value', 'total-fat', 'total-fat_100g'].forEach(key => {
                if (nutriments[key] !== undefined) {
                    console.log(`  ${key}: ${nutriments[key]}`);
                }
            });
            
            // Check fiber variations
            console.log('\nFiber variations:');
            ['fiber', 'fibers', 'fiber_100g', 'fibers_100g', 'crude-fiber', 'crude-fiber_100g'].forEach(key => {
                if (nutriments[key] !== undefined) {
                    console.log(`  ${key}: ${nutriments[key]}`);
                }
            });
            
            // Check ash variations
            console.log('\nAsh variations:');
            ['ash', 'ash_100g', 'ash_value', 'crude-ash', 'crude-ash_100g', 'minerals', 'minerals_100g'].forEach(key => {
                if (nutriments[key] !== undefined) {
                    console.log(`  ${key}: ${nutriments[key]}`);
                }
            });
            
            // Check moisture variations
            console.log('\nMoisture variations:');
            ['moisture', 'moisture_100g', 'water', 'water_100g', 'humidity', 'humidity_100g'].forEach(key => {
                if (nutriments[key] !== undefined) {
                    console.log(`  ${key}: ${nutriments[key]}`);
                }
            });
            
            // Check ingredients for analytical constituents
            console.log('\nIngredients text:');
            if (product.ingredients_text) {
                console.log(product.ingredients_text.substring(0, 200) + '...');
                
                // Look for analytical constituents pattern
                const analyticalPattern = /analytical\s+constituents?:?\s*([^.]+)/i;
                const match = product.ingredients_text.match(analyticalPattern);
                if (match) {
                    console.log('\nFound analytical constituents:');
                    console.log(match[1]);
                    
                    // Try to extract values
                    const proteinMatch = match[1].match(/protein[:\s]+(\d+\.?\d*)\s*%/i);
                    const fatMatch = match[1].match(/fat[:\s]+(\d+\.?\d*)\s*%/i);
                    const fiberMatch = match[1].match(/fib(?:er|re)[:\s]+(\d+\.?\d*)\s*%/i);
                    const ashMatch = match[1].match(/ash[:\s]+(\d+\.?\d*)\s*%/i);
                    const moistureMatch = match[1].match(/moisture[:\s]+(\d+\.?\d*)\s*%/i);
                    
                    if (proteinMatch) console.log(`  Protein: ${proteinMatch[1]}%`);
                    if (fatMatch) console.log(`  Fat: ${fatMatch[1]}%`);
                    if (fiberMatch) console.log(`  Fiber: ${fiberMatch[1]}%`);
                    if (ashMatch) console.log(`  Ash: ${ashMatch[1]}%`);
                    if (moistureMatch) console.log(`  Moisture: ${moistureMatch[1]}%`);
                }
            }
            
            console.log('\n');
        });
        
        // Now fetch a specific product with known nutrients
        console.log('\n' + '='.repeat(60));
        console.log('Fetching specific product with full details...\n');
        
        const specificResponse = await axios.get(`${baseUrl}/api/v2/product/3182550708180.json`, {
            headers: {
                'User-Agent': 'SnackTrack-Debug/1.0'
            }
        });
        
        if (specificResponse.data && specificResponse.data.product) {
            const product = specificResponse.data.product;
            console.log(`Product: ${product.product_name}`);
            console.log(`All available fields:`);
            console.log(JSON.stringify(Object.keys(product), null, 2));
            
            console.log('\nFull nutriments object:');
            console.log(JSON.stringify(product.nutriments, null, 2));
        }
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

debugNutrients();