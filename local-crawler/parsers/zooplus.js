const axios = require('axios');
const cheerio = require('cheerio');

/**
 * Parser for Zooplus online shop
 * European pet supplies retailer
 */
class ZooplusParser {
    constructor() {
        this.baseUrl = 'https://www.zooplus.de';
        this.userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36';
    }
    
    /**
     * Search for dog food products
     */
    async fetchProducts(page = 1, pageSize = 20) {
        const products = [];
        
        try {
            // Use correct URL structure for Zooplus
            const categories = [
                '/shop/hunde/hundefutter_trockenfutter',
                '/shop/hunde/hundefutter_nassfutter'
            ];
            
            for (const category of categories) {
                const searchUrl = `${this.baseUrl}${category}`;
                
                console.log(`Fetching Zooplus category: ${searchUrl}`);
                
                const response = await axios.get(searchUrl, {
                    params: {
                        seite: page // Zooplus uses 'seite' for page parameter
                    },
                    headers: {
                        'User-Agent': this.userAgent,
                        'Accept': 'text/html,application/xhtml+xml',
                        'Accept-Language': 'de-DE,de;q=0.9',
                        'Accept-Encoding': 'gzip, deflate, br'
                    },
                    timeout: 30000
                });
                
                const $ = cheerio.load(response.data);
                
                // Find product links - Updated selectors based on analysis
                const productLinks = [];
                $('a[href*="/shop/"]').each((index, element) => {
                    const link = $(element).attr('href');
                    // Filter for product links (usually end with numbers)
                    if (link && link.match(/\/\d+$/)) {
                        const fullLink = link.startsWith('http') ? link : this.baseUrl + link;
                        if (!productLinks.includes(fullLink)) {
                            productLinks.push(fullLink);
                        }
                    }
                    
                    // Limit to pageSize per category
                    if (productLinks.length >= Math.floor(pageSize / categories.length)) {
                        return false;
                    }
                });
                
                console.log(`Found ${productLinks.length} product links in ${category}`);
                
                // Fetch each product detail
                for (const link of productLinks) {
                    try {
                        const product = await this.fetchProductDetails(link);
                        if (product && this.hasCompleteNutrientData(product)) {
                            products.push(product);
                        }
                        
                        // Rate limiting
                        await new Promise(resolve => setTimeout(resolve, 1500));
                    } catch (error) {
                        console.error(`Error fetching product ${link}:`, error.message);
                    }
                }
                
                // Rate limiting between categories
                await new Promise(resolve => setTimeout(resolve, 2000));
            }
            
        } catch (error) {
            console.error('Error fetching Zooplus products:', error.message);
        }
        
        return products;
    }
    
    /**
     * Fetch detailed product information
     */
    async fetchProductDetails(productUrl) {
        try {
            const response = await axios.get(productUrl, {
                headers: {
                    'User-Agent': this.userAgent,
                    'Accept': 'text/html,application/xhtml+xml',
                    'Accept-Language': 'de-DE,de;q=0.9'
                },
                timeout: 20000
            });
            
            const $ = cheerio.load(response.data);
            
            // Extract EAN/GTIN - Look in JSON data first
            let ean = null;
            
            // Try different patterns to find EAN
            const eanPatterns = [
                /"ean":"(\d{8,14})"/i,
                /"gtin\d*":"(\d{8,14})"/i,
                /"articleNumber":"(\d{8,14})"/i,
                /(?:EAN|GTIN)[:\s]*(\d{8,14})/i,
                /data-ean="(\d{8,14})"/i,
                /data-gtin="(\d{8,14})"/i,
                /"productID":"(\d{8,14})"/i
            ];
            
            for (const pattern of eanPatterns) {
                const match = response.data.match(pattern);
                if (match) {
                    ean = match[1];
                    break;
                }
            }
            
            // Try structured data if not found
            if (!ean) {
                $('script[type="application/ld+json"]').each((i, elem) => {
                    try {
                        const data = JSON.parse($(elem).html());
                        if (data.gtin13) ean = data.gtin13;
                        else if (data.gtin) ean = data.gtin;
                        else if (data.sku && /^\d{8,14}$/.test(data.sku)) ean = data.sku;
                        else if (data.productID && /^\d{8,14}$/.test(data.productID)) ean = data.productID;
                    } catch (e) {}
                });
            }
            
            if (!ean) {
                console.log('No EAN found for product:', productUrl);
                return null;
            }
            
            // Extract basic info
            let brand = 'Unknown';
            let productName = 'Unknown';
            let imageUrl = null;
            
            // Try structured data first
            $('script[type="application/ld+json"]').each((i, elem) => {
                try {
                    const data = JSON.parse($(elem).html());
                    if (data['@type'] === 'Product') {
                        if (data.brand && data.brand.name) brand = data.brand.name;
                        if (data.name) productName = data.name;
                        if (data.image) imageUrl = data.image;
                    }
                } catch (e) {}
            });
            
            // Fallback to DOM extraction
            if (brand === 'Unknown') {
                brand = $('.z-product__brand, .product__brand, [itemprop="brand"] [itemprop="name"]').text().trim() || 
                       $('[data-zta="product-brand"]').text().trim() ||
                       'Unknown';
            }
            
            if (productName === 'Unknown') {
                productName = $('h1').first().text().trim() || 
                             $('.z-product__name, .product__title, h1[itemprop="name"]').text().trim() || 
                             'Unknown';
            }
            
            if (!imageUrl) {
                imageUrl = $('meta[property="og:image"]').attr('content') ||
                          $('.z-product__image img, .product__image img').first().attr('src') || 
                          $('[itemprop="image"]').attr('content') || 
                          $('img[data-zta="productImage"]').attr('src') ||
                          null;
            }
            
            // Extract brand from product name if still unknown
            if (brand === 'Unknown' && productName !== 'Unknown') {
                const brandPatterns = ['bosch', 'royal canin', 'hills', 'purina', 'eukanuba', 'animonda', 'josera', 'wolf of wilderness'];
                for (const pattern of brandPatterns) {
                    if (productName.toLowerCase().includes(pattern)) {
                        brand = pattern.split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
                        break;
                    }
                }
            }
            
            // Handle image URL (might be array or string)
            if (Array.isArray(imageUrl) && imageUrl.length > 0) {
                imageUrl = imageUrl[0]; // Take first image
            }
            
            // Make sure image URL is absolute
            if (imageUrl && typeof imageUrl === 'string' && !imageUrl.startsWith('http')) {
                imageUrl = this.baseUrl + imageUrl;
            }
            
            // Extract nutritional information
            let analyticalConstituents = null;
            
            // Look for nutritional information in various locations
            const nutritionSelectors = [
                '.z-tabs__content',
                '.product-info__content',
                '.z-accordion__content',
                '[data-zta*="ingredients"]',
                '[data-zta*="nutrition"]',
                '[class*="ProductAttribute"]',
                '[class*="product-info"]',
                '[class*="description"]',
                '.product-description',
                '.tab-panel',
                '[role="tabpanel"]'
            ];
            
            for (const selector of nutritionSelectors) {
                $(selector).each((i, elem) => {
                    const text = $(elem).text();
                    if (text.toLowerCase().includes('analytische bestandteile') || 
                        (text.includes('%') && (text.includes('Protein') || text.includes('Fett')))) {
                        analyticalConstituents = text;
                        return false; // Break out of each()
                    }
                });
                if (analyticalConstituents) break;
            }
            
            // If still not found, search in the entire page
            if (!analyticalConstituents) {
                // Look for table data format (Zooplus uses tables)
                const tableMatch = response.data.match(/analytische\s+bestandteile[\s\S]*?<table[\s\S]*?<\/table>/i);
                if (tableMatch) {
                    analyticalConstituents = tableMatch[0];
                } else {
                    // Fallback to general pattern
                    const nutritionMatch = response.data.match(/analytische\s+bestandteile[\s\S]{0,1000}?(?:protein|fett|faser|asche|feuchte)[\s\S]{0,1000}/i);
                    if (nutritionMatch) {
                        analyticalConstituents = nutritionMatch[0];
                    }
                }
            }
            
            if (!analyticalConstituents) {
                console.log('No analytical constituents found for:', productName);
                return null;
            }
            
            // Extract nutrients
            const nutrients = this.extractNutrientsFromText(analyticalConstituents);
            
            if (!nutrients) {
                return null;
            }
            
            return {
                ean: ean,
                brand: this.cleanText(brand),
                product: this.cleanText(productName),
                protein: nutrients.protein || 0,
                fat: nutrients.fat || 0,
                crudeFiber: nutrients.fiber || 0,
                rawAsh: nutrients.ash || 0,
                moisture: nutrients.moisture || 0,
                carbohydrates: nutrients.carbohydrates || 0,
                energy: nutrients.energy || 0,
                additives: nutrients.additives || null,
                imageUrl: imageUrl,
                source: 'zooplus',
                sourceUrl: productUrl
            };
            
        } catch (error) {
            console.error('Error fetching product details:', error.message);
            return null;
        }
    }
    
    /**
     * Extract nutrients from German text
     */
    extractNutrientsFromText(text) {
        if (!text) return null;
        
        const result = {};
        
        // German patterns for nutrients (same as Fressnapf)
        const patterns = {
            protein: [
                /(?:roh)?protein\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /(?:rohes?\s+)?eiweiß\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /proteine?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            fat: [
                /(?:roh)?fett\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /fettgehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /öle?\s+und\s+fette?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            fiber: [
                /(?:roh)?faser\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /ballaststoffe?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /rohfasern?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            ash: [
                /(?:roh)?asche\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /mineralstoffe?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /aschgehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            moisture: [
                /feuchtigkeit\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /feuchte\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /wassergehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /feuchtegehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ]
        };
        
        // Extract each nutrient
        for (const [nutrient, nutrientPatterns] of Object.entries(patterns)) {
            for (const pattern of nutrientPatterns) {
                const match = text.match(pattern);
                if (match) {
                    // Convert German decimal comma to dot
                    result[nutrient] = parseFloat(match[1].replace(',', '.'));
                    break;
                }
            }
        }
        
        // Also try table format extraction (Zooplus specific)
        if (text.includes('<td>') || text.includes('</td>')) {
            // Extract from table cells
            const tablePatterns = {
                protein: /<td>(?:Roh)?protein<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                fat: /<td>(?:Roh)?fett<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                fiber: /<td>Rohfaser<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                ash: /<td>Rohasche<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                moisture: /<td>(?:Feuchte|Feuchtigkeit)<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i
            };
            
            for (const [nutrient, pattern] of Object.entries(tablePatterns)) {
                if (!result[nutrient]) {
                    const match = text.match(pattern);
                    if (match) {
                        result[nutrient] = parseFloat(match[1].replace(',', '.'));
                    }
                }
            }
        }
        
        // Extract additives
        const additivesSection = text.match(/zusatzstoffe[:\s]*([^.]+\.)/i);
        if (additivesSection) {
            result.additives = this.cleanText(additivesSection[1]);
        } else {
            // Look for vitamins
            const vitaminMatch = text.match(/vitamin[^.]+\./gi);
            if (vitaminMatch) {
                result.additives = vitaminMatch.join(' ');
            }
        }
        
        return result;
    }
    
    /**
     * Check if product has complete nutrient data
     * Note: Moisture is optional as many dry foods don't list it
     */
    hasCompleteNutrientData(product) {
        return product.protein > 0 && 
               product.fat > 0 && 
               product.crudeFiber > 0 && 
               product.rawAsh > 0;
    }
    
    /**
     * Clean text
     */
    cleanText(text) {
        if (!text) return null;
        return text.toString().trim().replace(/\s+/g, ' ');
    }
}

module.exports = { ZooplusParser };