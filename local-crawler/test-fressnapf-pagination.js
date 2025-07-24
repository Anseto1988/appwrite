const axios = require('axios');
const cheerio = require('cheerio');

async function testFressnapfPagination() {
    console.log('🔍 Testing Fressnapf Pagination\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    const baseUrl = 'https://www.fressnapf.de';
    const categoryUrl = '/c/hund/hundefutter/trockenfutter/';
    
    // Test different page parameters
    const pageParams = [
        { page: 1 },
        { page: 2 },
        { p: 1 },
        { p: 2 },
        { seite: 1 },
        { seite: 2 },
        { offset: 0 },
        { offset: 24 },
        { start: 0 },
        { start: 24 }
    ];
    
    console.log('Testing different pagination parameters:\n');
    
    for (const params of pageParams) {
        console.log(`Testing params:`, params);
        
        try {
            const response = await axios.get(`${baseUrl}${categoryUrl}`, {
                params: params,
                headers: {
                    'User-Agent': userAgent,
                    'Accept': 'text/html,application/xhtml+xml',
                    'Accept-Language': 'de-DE,de;q=0.9'
                },
                timeout: 10000
            });
            
            const $ = cheerio.load(response.data);
            
            // Get first product link as identifier
            const firstProduct = $('a[href*="/p/"]').first();
            const firstProductHref = firstProduct.attr('href');
            const firstProductText = firstProduct.text().trim().substring(0, 30);
            
            console.log(`  First product: ${firstProductText || 'None found'}`);
            console.log(`  URL: ${response.request.res.responseUrl || response.config.url}`);
            
            // Check for pagination links
            const paginationLinks = [];
            $('a[href*="page="], a[href*="p="], a[href*="seite="], .pagination a').each((i, elem) => {
                const href = $(elem).attr('href');
                if (href && !paginationLinks.includes(href)) {
                    paginationLinks.push(href);
                }
            });
            
            if (paginationLinks.length > 0) {
                console.log(`  Pagination links found: ${paginationLinks.length}`);
                console.log(`  Sample: ${paginationLinks[0]}`);
            }
            
            console.log('');
            
        } catch (error) {
            console.log(`  Error: ${error.message}\n`);
        }
        
        await new Promise(resolve => setTimeout(resolve, 1000));
    }
    
    // Now check the actual pagination structure
    console.log('\n🔍 Analyzing pagination structure:\n');
    
    try {
        const response = await axios.get(`${baseUrl}${categoryUrl}`, {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 10000
        });
        
        const $ = cheerio.load(response.data);
        
        // Look for pagination elements
        console.log('Pagination elements found:');
        
        const paginationSelectors = [
            '.pagination',
            '[class*="pagination"]',
            'nav[aria-label*="pagination"]',
            '[data-testid*="pagination"]',
            '.page-numbers',
            '[class*="page"]'
        ];
        
        paginationSelectors.forEach(selector => {
            const elements = $(selector);
            if (elements.length > 0) {
                console.log(`\n${selector}: ${elements.length} elements`);
                
                // Check links within pagination
                elements.find('a').each((i, elem) => {
                    const href = $(elem).attr('href');
                    const text = $(elem).text().trim();
                    if (i < 3) { // Show first 3 links
                        console.log(`  Link ${i + 1}: "${text}" -> ${href}`);
                    }
                });
            }
        });
        
        // Check if it's infinite scroll
        const scripts = $('script').map((i, elem) => $(elem).html()).get().join(' ');
        if (scripts.includes('infinite') || scripts.includes('loadMore') || scripts.includes('lazy')) {
            console.log('\n⚠️  Possible infinite scroll or lazy loading detected');
        }
        
    } catch (error) {
        console.error('Error analyzing pagination:', error.message);
    }
}

testFressnapfPagination().catch(console.error);