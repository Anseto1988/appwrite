const axios = require('axios');
const cheerio = require('cheerio');

async function analyzeFressnapfHTML() {
    console.log('🔍 Analyzing Fressnapf HTML Structure\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    try {
        // Get the main dogs category page
        const response = await axios.get('https://www.fressnapf.de/c/hunde/', {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                'Accept-Language': 'de-DE,de;q=0.9,en;q=0.8'
            },
            timeout: 10000
        });
        
        const $ = cheerio.load(response.data);
        
        // Look for all links on the page
        console.log('📋 All category links found:\n');
        
        const categoryLinks = new Map();
        
        $('a[href*="/c/"]').each((i, elem) => {
            const href = $(elem).attr('href');
            const text = $(elem).text().trim();
            
            if (href && text && !categoryLinks.has(href)) {
                categoryLinks.set(href, text);
            }
        });
        
        // Filter and display food-related categories
        console.log('🍖 Food-related categories:');
        categoryLinks.forEach((text, href) => {
            if (text.toLowerCase().includes('futter') || 
                text.toLowerCase().includes('nahrung') ||
                text.toLowerCase().includes('snack')) {
                console.log(`  "${text}": ${href}`);
            }
        });
        
        // Look for product cards/tiles
        console.log('\n📦 Product card selectors found:');
        const productSelectors = [
            '[data-testid*="product"]',
            '[class*="product-tile"]',
            '[class*="ProductTile"]',
            '[class*="product-card"]',
            '[class*="ProductCard"]',
            '.product',
            'article[class*="product"]',
            'div[class*="product-item"]'
        ];
        
        productSelectors.forEach(selector => {
            const count = $(selector).length;
            if (count > 0) {
                console.log(`  ${selector}: ${count} items`);
            }
        });
        
        // Look for actual product links
        console.log('\n🔗 Sample product links:');
        const productLinks = [];
        
        $('a[href*="/p/"]').each((i, elem) => {
            if (productLinks.length < 5) {
                const href = $(elem).attr('href');
                const text = $(elem).find('[class*="product-name"], [class*="title"]').text().trim() ||
                           $(elem).text().trim().substring(0, 50);
                productLinks.push({ href, text });
            }
        });
        
        productLinks.forEach(link => {
            console.log(`  ${link.text}...: ${link.href}`);
        });
        
        // Check for pagination
        console.log('\n📄 Pagination found:');
        const paginationSelectors = [
            '[class*="pagination"]',
            '[data-testid*="pagination"]',
            'nav[aria-label*="pagination"]',
            '.page-numbers',
            'a[href*="page="]'
        ];
        
        paginationSelectors.forEach(selector => {
            const count = $(selector).length;
            if (count > 0) {
                console.log(`  ${selector}: ${count} items`);
            }
        });
        
        // Look for JSON data in scripts
        console.log('\n📊 Looking for structured data:');
        
        $('script').each((i, elem) => {
            const content = $(elem).html();
            if (content && (
                content.includes('window.__INITIAL_STATE__') ||
                content.includes('window.__NEXT_DATA__') ||
                content.includes('productList') ||
                content.includes('categoryProducts')
            )) {
                console.log(`  Found data in script tag ${i}`);
                
                // Try to extract some info
                if (content.includes('window.__')) {
                    const match = content.match(/window\.__[A-Z_]+__\s*=\s*({[\s\S]+?});/);
                    if (match) {
                        try {
                            const data = JSON.parse(match[1]);
                            console.log(`    Data keys: ${Object.keys(data).slice(0, 5).join(', ')}...`);
                        } catch (e) {
                            console.log('    Could not parse JSON data');
                        }
                    }
                }
            }
        });
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

analyzeFressnapfHTML().catch(console.error);