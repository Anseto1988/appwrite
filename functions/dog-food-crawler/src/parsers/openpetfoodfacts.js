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
                        fields: 'code,product_name,brands,nutriments,image_url,ingredients_text,categories_tags'
                    },
                    headers: {
                        'User-Agent': this.userAgent
                    },
                    timeout: 30000 // 30 seconds
                });
                
                if (response.data && response.data.products) {
                    for (const product of response.data.products) {
                        const parsed = this.parseProduct(product);
                        if (parsed) {
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
        
        return {
            ean: opffProduct.code,
            brand: this.cleanText(opffProduct.brands) || 'Unknown',
            product: this.cleanText(opffProduct.product_name) || 'Unknown',
            protein: this.extractNutriment(nutriments, 'proteins'),
            fat: this.extractNutriment(nutriments, 'fat'),
            crudeFiber: this.extractNutriment(nutriments, 'fiber'),
            rawAsh: this.extractNutriment(nutriments, 'ash'),
            moisture: this.extractNutriment(nutriments, 'moisture'),
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
            `${key}_100g`,
            `${key}_value`,
            `${key}`,
            `${key}_g`
        ];
        
        for (const variation of variations) {
            if (nutriments[variation] !== undefined) {
                const value = parseFloat(nutriments[variation]);
                return isNaN(value) ? 0 : value;
            }
        }
        
        return 0;
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