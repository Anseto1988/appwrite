const axios = require('axios');
const cheerio = require('cheerio');

async function analyzeZooplusHTML() {
    console.log('🔍 Analyzing Zooplus HTML Structure\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    try {
        // Test the working URL we found
        const response = await axios.get('https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter', {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                'Accept-Language': 'de-DE,de;q=0.9,en;q=0.8'
            },
            timeout: 10000
        });
        
        console.log('✅ Page loaded successfully\n');
        
        const $ = cheerio.load(response.data);
        
        // Look for product containers
        console.log('📦 Product selectors found:');
        const productSelectors = [
            '[data-zta="productListItem"]',
            '[class*="ProductListItem"]',
            '[class*="product-item"]',
            '[class*="ProductTile"]',
            'article[class*="product"]',
            '[data-test*="product"]',
            '.product',
            '[itemtype*="schema.org/Product"]'
        ];
        
        productSelectors.forEach(selector => {
            const count = $(selector).length;
            if (count > 0) {
                console.log(`  ${selector}: ${count} items`);
            }
        });
        
        // Look for product links
        console.log('\n🔗 Sample product links:');
        const productLinks = [];
        
        $('a[href*="/shop/"]').each((i, elem) => {
            const href = $(elem).attr('href');
            const text = $(elem).text().trim();
            
            // Filter for product links (usually have numbers at the end)
            if (href && href.match(/\/\d+$/) && productLinks.length < 5) {
                productLinks.push({
                    href: href,
                    text: text.substring(0, 50) || 'No text'
                });
            }
        });
        
        productLinks.forEach(link => {
            console.log(`  ${link.text}...: ${link.href}`);
        });
        
        // Look for pagination
        console.log('\n📄 Pagination:');
        const pageLinks = [];
        $('a[href*="seite="], a[href*="page="], [class*="pagination"] a').each((i, elem) => {
            const href = $(elem).attr('href');
            if (href && !pageLinks.includes(href)) {
                pageLinks.push(href);
            }
        });
        console.log(`  Found ${pageLinks.length} pagination links`);
        if (pageLinks.length > 0) {
            console.log(`  Sample: ${pageLinks[0]}`);
        }
        
        // Look for structured data
        console.log('\n📊 Structured data:');
        let hasStructuredData = false;
        
        $('script[type="application/ld+json"]').each((i, elem) => {
            hasStructuredData = true;
            try {
                const data = JSON.parse($(elem).html());
                console.log(`  Found JSON-LD data: ${data['@type'] || 'Unknown type'}`);
            } catch (e) {
                console.log('  Found JSON-LD but could not parse');
            }
        });
        
        // Check for React/Next.js data
        $('script').each((i, elem) => {
            const content = $(elem).html();
            if (content && content.includes('window.__INITIAL_STATE__')) {
                console.log('  Found React initial state data');
                hasStructuredData = true;
            }
        });
        
        if (!hasStructuredData) {
            console.log('  No structured data found');
        }
        
        // Test a specific product page
        console.log('\n🔍 Testing product detail page:');
        
        if (productLinks.length > 0) {
            const productUrl = `https://www.zooplus.de${productLinks[0].href}`;
            console.log(`  Fetching: ${productUrl}`);
            
            try {
                const productResponse = await axios.get(productUrl, {
                    headers: { 'User-Agent': userAgent },
                    timeout: 10000
                });
                
                const $product = cheerio.load(productResponse.data);
                
                // Look for EAN
                const pageText = productResponse.data;
                const eanMatch = pageText.match(/(?:EAN|GTIN)[:\s]*(\d{8,14})/i);
                if (eanMatch) {
                    console.log(`  ✅ EAN found: ${eanMatch[1]}`);
                } else {
                    console.log('  ❌ No EAN found');
                }
                
                // Look for nutritional info
                if (pageText.includes('Analytische Bestandteile') || 
                    pageText.includes('Zusammensetzung')) {
                    console.log('  ✅ Nutritional information found');
                } else {
                    console.log('  ❌ No nutritional information found');
                }
                
            } catch (error) {
                console.log(`  Error fetching product: ${error.message}`);
            }
        }
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

analyzeZooplusHTML().catch(console.error);