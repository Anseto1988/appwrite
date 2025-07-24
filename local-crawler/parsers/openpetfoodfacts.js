const axios = require('axios');

/**
 * Parser for Open Pet Food Facts API
 * Free and open database of pet food products
 */
class OpenPetFoodFactsParser {
    constructor() {
        this.baseUrl = 'https://world.openpetfoodfacts.org';
        this.userAgent = 'SnackTrack-DogFood-Crawler/1.0';
    }
    
    /**
     * Fetch products from a specific category
     */
    async fetchProducts(page = 1, pageSize = 20) {
        const products = [];
        
        // Categories to search
        const categories = [
            'en:dog-food',
            'de:hundefutter',
            'en:dry-dog-food',
            'en:wet-dog-food'
        ];
        
        for (const category of categories) {
            try {
                const url = `${this.baseUrl}/category/${category}.json`;
                
                const response = await axios.get(url, {
                    params: {
                        page: page,
                        page_size: pageSize,
                        fields: 'code,product_name,brands,nutriments,image_url,ingredients_text,ingredients_text_de,ingredients_text_fr,ingredients_text_en,categories_tags'
                    },
                    headers: {
                        'User-Agent': this.userAgent
                    },
                    timeout: 30000 // 30 seconds
                });
                
                if (response.data && response.data.products) {
                    for (const product of response.data.products) {
                        const parsed = this.parseProduct(product);
                        // Only add products with complete nutrient data
                        if (parsed && this.hasCompleteNutrientData(parsed)) {
                            products.push(parsed);
                        }
                    }
                }
                
            } catch (error) {
                console.error(`Error fetching category ${category}:`, error.message);
                // Continue with other categories
            }
            
            // Rate limiting between category requests
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
        
        // Remove duplicates by EAN
        const uniqueProducts = this.removeDuplicates(products);
        
        return uniqueProducts;
    }
    
    /**
     * Parse OPFF product data to our schema
     */
    parseProduct(opffProduct) {
        // Skip if no barcode
        if (!opffProduct.code) {
            return null;
        }
        
        // Extract nutriments (OPFF uses per 100g values)
        const nutriments = opffProduct.nutriments || {};
        
        // Try to extract from nutriments first
        let protein = this.extractNutriment(nutriments, 'proteins');
        let fat = this.extractNutriment(nutriments, 'fat');
        let fiber = this.extractNutriment(nutriments, 'fiber');
        let ash = this.extractNutriment(nutriments, 'ash');
        let moisture = this.extractNutriment(nutriments, 'moisture');
        
        // If not found in nutriments, try to extract from ingredients text in multiple languages
        const ingredientsTexts = [
            opffProduct.ingredients_text,
            opffProduct.ingredients_text_en,
            opffProduct.ingredients_text_de,
            opffProduct.ingredients_text_fr,
            opffProduct.ingredients_text_es,
            opffProduct.ingredients_text_it
        ].filter(text => text);
        
        for (const text of ingredientsTexts) {
            if ((protein === 0 || fat === 0 || fiber === 0 || ash === 0 || moisture === 0) && text) {
                const analyticalValues = this.extractAnalyticalConstituents(text);
                if (analyticalValues) {
                    protein = protein || analyticalValues.protein || 0;
                    fat = fat || analyticalValues.fat || 0;
                    fiber = fiber || analyticalValues.fiber || 0;
                    ash = ash || analyticalValues.ash || 0;
                    moisture = moisture || analyticalValues.moisture || 0;
                }
            }
        }
        
        return {
            ean: opffProduct.code,
            brand: this.cleanText(opffProduct.brands) || 'Unknown',
            product: this.cleanText(opffProduct.product_name) || 'Unknown',
            protein: protein,
            fat: fat,
            crudeFiber: fiber,
            rawAsh: ash,
            moisture: moisture,
            carbohydrates: this.extractNutriment(nutriments, 'carbohydrates'),
            energy: this.extractNutriment(nutriments, 'energy-kcal'),
            additives: this.extractAdditives(opffProduct.ingredients_text),
            imageUrl: this.selectBestImage(opffProduct),
            source: 'opff',
            sourceUrl: `${this.baseUrl}/product/${opffProduct.code}`,
            categories: opffProduct.categories_tags || []
        };
    }
    
    /**
     * Extract nutriment value, trying different field variations
     */
    extractNutriment(nutriments, key) {
        // OPFF may use different field names
        const variations = [
            // For protein
            ...(key === 'proteins' ? ['crude-protein', 'crude-protein_100g', 'protein', 'protein_100g'] : []),
            // For fat
            ...(key === 'fat' ? ['crude-fat', 'crude-fat_100g', 'fats', 'fats_100g', 'total-fat', 'total-fat_100g'] : []),
            // For fiber
            ...(key === 'fiber' ? ['crude-fibre', 'crude-fibre_100g', 'crude-fiber', 'crude-fiber_100g', 'fibers', 'fibers_100g'] : []),
            // For ash
            ...(key === 'ash' ? ['crude-ash', 'crude-ash_100g', 'minerals', 'minerals_100g'] : []),
            // For moisture
            ...(key === 'moisture' ? ['water', 'water_100g', 'humidity', 'humidity_100g'] : []),
            // Standard variations
            `${key}_100g`,
            `${key}_value`,
            `${key}`,
            `${key}_g`
        ];
        
        for (const variation of variations) {
            if (nutriments[variation] !== undefined) {
                const value = parseFloat(nutriments[variation]);
                // If value is from _100g field, it's already a percentage
                if (variation.includes('_100g') || variation.includes('crude-')) {
                    return isNaN(value) ? 0 : value;
                }
                // Otherwise convert to percentage
                return isNaN(value) ? 0 : value;
            }
        }
        
        return 0;
    }
    
    /**
     * Extract analytical constituents from ingredients text
     */
    extractAnalyticalConstituents(text) {
        if (!text) return null;
        
        // Common patterns for analytical constituents in multiple languages
        const patterns = [
            /analytical\s+constituents?:?\s*([^.]+)/i,
            /analytische\s+bestandteile:?\s*([^.]+)/i,
            /composants\s+analytiques?:?\s*([^.]+)/i,
            /analisi\s+garantita:?\s*([^.]+)/i,
            /nutritional\s+analysis:?\s*([^.]+)/i,
            /guaranteed\s+analysis:?\s*([^.]+)/i
        ];
        
        let constituentsText = null;
        for (const pattern of patterns) {
            const match = text.match(pattern);
            if (match) {
                constituentsText = match[1];
                break;
            }
        }
        
        if (!constituentsText) return null;
        
        // Extract individual values
        const result = {};
        
        // Protein patterns
        const proteinPatterns = [
            /(?:crude\s+)?protein[e]?\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /(?:roh)?protein\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /protéines?\s*(?:brutes?)?\s*[:=]?\s*(\d+\.?\d*)\s*%/i
        ];
        
        // Fat patterns
        const fatPatterns = [
            /(?:crude\s+)?fat\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /(?:roh)?fett\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /matières?\s+grasses?\s*(?:brutes?)?\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /oils?\s+(?:and\s+)?fats?\s*[:=]?\s*(\d+\.?\d*)\s*%/i
        ];
        
        // Fiber patterns
        const fiberPatterns = [
            /(?:crude\s+)?fib(?:er|re)\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /(?:roh)?faser\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /fibres?\s*(?:brutes?)?\s*[:=]?\s*(\d+\.?\d*)\s*%/i
        ];
        
        // Ash patterns
        const ashPatterns = [
            /(?:crude\s+)?ash\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /(?:roh)?asche\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /cendres?\s*(?:brutes?)?\s*[:=]?\s*(\d+\.?\d*)\s*%/i
        ];
        
        // Moisture patterns
        const moisturePatterns = [
            /moisture\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /feuchtigkeit\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /humidité\s*[:=]?\s*(\d+\.?\d*)\s*%/i,
            /water\s*[:=]?\s*(\d+\.?\d*)\s*%/i
        ];
        
        // Try to match each nutrient
        for (const pattern of proteinPatterns) {
            const match = constituentsText.match(pattern);
            if (match) {
                result.protein = parseFloat(match[1]);
                break;
            }
        }
        
        for (const pattern of fatPatterns) {
            const match = constituentsText.match(pattern);
            if (match) {
                result.fat = parseFloat(match[1]);
                break;
            }
        }
        
        for (const pattern of fiberPatterns) {
            const match = constituentsText.match(pattern);
            if (match) {
                result.fiber = parseFloat(match[1]);
                break;
            }
        }
        
        for (const pattern of ashPatterns) {
            const match = constituentsText.match(pattern);
            if (match) {
                result.ash = parseFloat(match[1]);
                break;
            }
        }
        
        for (const pattern of moisturePatterns) {
            const match = constituentsText.match(pattern);
            if (match) {
                result.moisture = parseFloat(match[1]);
                break;
            }
        }
        
        return Object.keys(result).length > 0 ? result : null;
    }
    
    /**
     * Extract additives from ingredients text
     */
    extractAdditives(ingredientsText) {
        if (!ingredientsText) return null;
        
        // Look for common additive patterns
        const additivePatterns = [
            /vitamin[e]?\s+[A-E0-9]+/gi,
            /E[0-9]{3}[a-z]?/g,
            /mineral[s]?\s*[:]\s*[^,]+/gi,
            /trace\s+element[s]?/gi
        ];
        
        const additives = [];
        
        for (const pattern of additivePatterns) {
            const matches = ingredientsText.match(pattern);
            if (matches) {
                additives.push(...matches);
            }
        }
        
        return additives.length > 0 ? additives.join(', ') : null;
    }
    
    /**
     * Select the best available image
     */
    selectBestImage(product) {
        if (product.image_url) {
            return product.image_url;
        }
        
        if (product.image_front_url) {
            return product.image_front_url;
        }
        
        if (product.image_small_url) {
            return product.image_small_url;
        }
        
        return null;
    }
    
    /**
     * Clean text by removing extra spaces and line breaks
     */
    cleanText(text) {
        if (!text) return null;
        return text.toString().trim().replace(/\s+/g, ' ');
    }
    
    /**
     * Check if product has complete nutrient data
     * Note: Moisture is optional as many dry foods don't list it
     */
    hasCompleteNutrientData(product) {
        // All main nutrients must be present and greater than 0 (except moisture)
        return product.protein > 0 && 
               product.fat > 0 && 
               product.crudeFiber > 0 && 
               product.rawAsh > 0;
    }
    
    /**
     * Remove duplicate products by EAN
     */
    removeDuplicates(products) {
        const seen = new Set();
        const unique = [];
        
        for (const product of products) {
            if (!seen.has(product.ean)) {
                seen.add(product.ean);
                unique.push(product);
            }
        }
        
        return unique;
    }
    
    /**
     * Search for a specific product by EAN
     */
    async searchByEan(ean) {
        try {
            const url = `${this.baseUrl}/api/v2/product/${ean}.json`;
            
            const response = await axios.get(url, {
                headers: {
                    'User-Agent': this.userAgent
                },
                timeout: 10000
            });
            
            if (response.data && response.data.status === 1 && response.data.product) {
                return this.parseProduct(response.data.product);
            }
            
            return null;
            
        } catch (error) {
            console.error(`Error searching for EAN ${ean}:`, error.message);
            return null;
        }
    }
}

module.exports = { OpenPetFoodFactsParser };