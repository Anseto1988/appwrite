const axios = require('axios');
const cheerio = require('cheerio');

/**
 * Parser for Fressnapf online shop
 * German pet food retailer with detailed product information
 */
class FressnapfParser {
    constructor() {
        this.baseUrl = 'https://www.fressnapf.de';
        this.userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36';
    }
    
    /**
     * Search for dog food products
     */
    async fetchProducts(page = 1, pageSize = 20) {
        const products = [];
        
        try {
            // Use correct URL structure for Fressnapf
            const categories = [
                '/c/hund/hundefutter/trockenfutter/',
                '/c/hund/hundefutter/nassfutter/',
                '/c/hund/hundefutter/snacks/'
            ];
            
            for (const category of categories) {
                const searchUrl = `${this.baseUrl}${category}`;
                
                console.log(`Fetching Fressnapf category: ${searchUrl}`);
                
                const response = await axios.get(searchUrl, {
                    params: {
                        currentPage: page
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
                const categoryProductLinks = new Set();
                
                $('a[href*="/p/"]').each((index, element) => {
                    const link = $(element).attr('href');
                    if (link && !link.startsWith('http')) {
                        const fullLink = this.baseUrl + link;
                        categoryProductLinks.add(fullLink);
                    }
                });
                
                // Convert Set to Array and take only what we need
                const uniqueLinks = Array.from(categoryProductLinks);
                const linksToProcess = uniqueLinks.slice(0, Math.ceil(pageSize / categories.length));
                productLinks.push(...linksToProcess);
                
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
            console.error('Error fetching Fressnapf products:', error.message);
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
            
            // Extract EAN - Look for it in JSON data first
            let ean = null;
            
            // Try to find EAN in page data
            const eanPatterns = [
                /"gtin\d*":"(\d{8,14})"/i,
                /"ean":"(\d{8,14})"/i,
                /(?:EAN|GTIN)[:\s]*(\d{8,14})/i,
                /"sku":"(\d{8,14})"/i,
                /data-ean="(\d{8,14})"/i
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
                    } catch (e) {}
                });
            }
            
            if (!ean) {
                console.log('No EAN found for product:', productUrl);
                return null;
            }
            
            // Extract basic info - parse brand from title or structured data
            let brand = 'Unknown';
            
            // Try structured data first
            $('script[type="application/ld+json"]').each((i, elem) => {
                try {
                    const data = JSON.parse($(elem).html());
                    if (data.brand && data.brand.name) {
                        brand = data.brand.name;
                    }
                } catch (e) {}
            });
            
            // If still unknown, try to extract from title
            const productName = $('h1').first().text().trim() || 
                               $('.product-stage__title').text().trim() || 
                               $('[itemprop="name"]').text().trim() ||
                               'Unknown';
            
            // Extract brand from product name if not found
            if (brand === 'Unknown' && productName !== 'Unknown') {
                // Common pet food brands
                const brandPatterns = ['bosch', 'royal canin', 'hills', 'purina', 'eukanuba', 'animonda', 'terra canis'];
                for (const pattern of brandPatterns) {
                    if (productName.toLowerCase().includes(pattern)) {
                        brand = pattern.split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
                        break;
                    }
                }
            }
            
            // Extract image URL
            let imageUrl = $('img[itemprop="image"]').attr('src') ||
                          $('.product-stage__image img').attr('src') || 
                          $('[class*="product-image"] img').first().attr('src') ||
                          $('meta[property="og:image"]').attr('content') ||
                          null;
            
            // Make sure image URL is absolute
            if (imageUrl && !imageUrl.startsWith('http')) {
                imageUrl = this.baseUrl + imageUrl;
            }
            
            // Extract nutritional information
            let analyticalConstituents = null;
            
            // Look for analytical constituents in various sections
            const nutritionSelectors = [
                '.product-description__content',
                '.product-info__content', 
                '.tab-content',
                '.accordion__content',
                '.tab-pane',
                '[class*="detail"]',
                '[class*="ingredient"]',
                '[class*="nutrition"]',
                '[data-testid*="ingredients"]',
                '[data-testid*="nutrition"]'
            ];
            
            for (const selector of nutritionSelectors) {
                $(selector).each((i, elem) => {
                    const text = $(elem).text();
                    if (text.toLowerCase().includes('analytische bestandteile') || 
                        (text.includes('%') && text.includes('Protein'))) {
                        analyticalConstituents = text;
                        return false; // Break out of each()
                    }
                });
                if (analyticalConstituents) break;
            }
            
            // If still not found, search in the entire page for the section
            if (!analyticalConstituents) {
                const nutritionMatch = response.data.match(/analytische\s+bestandteile[^<]*?([^}]+?(?:protein|fett|faser|asche|feuchte)[^}]+)/i);
                if (nutritionMatch) {
                    analyticalConstituents = nutritionMatch[0];
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
                source: 'fressnapf',
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
        
        // German patterns for nutrients
        const patterns = {
            protein: [
                /(?:roh)?protein\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /(?:rohes?\s+)?eiweiß\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            fat: [
                /(?:roh)?fett\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /fettgehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            fiber: [
                /(?:roh)?faser\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /ballaststoffe?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            ash: [
                /(?:roh)?asche\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /mineralstoffe?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
            ],
            moisture: [
                /feuchtigkeit\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /feuchte(?:gehalt)?\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /wassergehalt\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i,
                /wasser\s*[:=]?\s*(\d+[,.]?\d*)\s*%/i
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
        
        // Extract additives
        const additivesMatch = text.match(/zusatzstoffe[:\s]+([^.]+\.)|(vitamin[^.]+\.)/i);
        if (additivesMatch) {
            result.additives = this.cleanText(additivesMatch[1] || additivesMatch[2]);
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

module.exports = { FressnapfParser };