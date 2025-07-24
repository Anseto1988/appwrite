const axios = require('axios');
const cheerio = require('cheerio');

async function debugFressnapfProducts() {
    console.log('🔍 Debug Fressnapf Product Links\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    const baseUrl = 'https://www.fressnapf.de';
    const categoryUrl = '/c/hund/hundefutter/trockenfutter/';
    
    try {
        // Test page 2 specifically
        const response = await axios.get(`${baseUrl}${categoryUrl}`, {
            params: {
                currentPage: 2
            },
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 20000
        });
        
        const $ = cheerio.load(response.data);
        
        console.log('URL:', response.request.res.responseUrl || response.config.url);
        console.log('\n📦 Looking for product links:\n');
        
        // Count different types of links
        const linkTypes = {
            '/p/': $('a[href*="/p/"]').length,
            'product': $('a[href*="product"]').length,
            'artikel': $('a[href*="artikel"]').length
        };
        
        console.log('Link counts:');
        Object.entries(linkTypes).forEach(([type, count]) => {
            console.log(`  Links containing "${type}": ${count}`);
        });
        
        // Show all /p/ links
        console.log('\n🔗 All product links found:');
        const productLinks = new Set();
        
        $('a[href*="/p/"]').each((i, elem) => {
            const href = $(elem).attr('href');
            if (href && !productLinks.has(href)) {
                productLinks.add(href);
                const text = $(elem).text().trim().substring(0, 50);
                console.log(`  ${i + 1}. ${text || 'No text'}`);
                console.log(`     ${href}`);
            }
        });
        
        // Check for product containers
        console.log('\n📦 Product containers:');
        const containerSelectors = [
            '[class*="product-tile"]',
            '[class*="ProductTile"]',
            '[class*="product-card"]',
            '[class*="ProductCard"]',
            '[data-testid*="product"]',
            'article[class*="product"]',
            '.product'
        ];
        
        containerSelectors.forEach(selector => {
            const count = $(selector).length;
            if (count > 0) {
                console.log(`  ${selector}: ${count} items`);
            }
        });
        
        // Check if there's lazy loading
        const scripts = $('script').map((i, elem) => $(elem).html()).get().join(' ');
        if (scripts.includes('lazy') || scripts.includes('infinite')) {
            console.log('\n⚠️  Lazy loading or infinite scroll detected');
        }
        
        // Check for AJAX endpoints
        if (scripts.includes('api/') || scripts.includes('ajax')) {
            console.log('\n🔍 Possible AJAX endpoints found');
            const apiMatches = scripts.match(/['"]([^'"]*api[^'"]*)['"]/g);
            if (apiMatches) {
                apiMatches.slice(0, 3).forEach(match => {
                    console.log(`  ${match}`);
                });
            }
        }
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

debugFressnapfProducts().catch(console.error);