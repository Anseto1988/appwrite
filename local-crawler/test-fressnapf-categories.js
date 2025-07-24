const axios = require('axios');
const cheerio = require('cheerio');

async function testFressnapfCategories() {
    console.log('🔍 Testing Fressnapf Category Structure\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    // First, let's check the dogs main page to find food categories
    try {
        const response = await axios.get('https://www.fressnapf.de/c/hunde/', {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                'Accept-Language': 'de-DE,de;q=0.9,en;q=0.8',
                'Accept-Encoding': 'gzip, deflate, br',
                'DNT': '1',
                'Connection': 'keep-alive',
                'Upgrade-Insecure-Requests': '1'
            },
            timeout: 10000
        });
        
        console.log('✅ Main dogs page loaded successfully\n');
        
        const $ = cheerio.load(response.data);
        
        // Find links that might be food categories
        console.log('🔍 Looking for food category links:\n');
        
        const foodLinks = new Set();
        
        // Look for links containing food-related keywords
        $('a[href*="/c/hunde/"]').each((i, elem) => {
            const href = $(elem).attr('href');
            const text = $(elem).text().trim();
            
            if (href && (
                href.includes('futter') || 
                href.includes('nahrung') || 
                href.includes('snack') ||
                text.toLowerCase().includes('futter') ||
                text.toLowerCase().includes('nahrung')
            )) {
                foodLinks.add({
                    url: href,
                    text: text
                });
            }
        });
        
        console.log('Found food-related links:');
        foodLinks.forEach(link => {
            console.log(`  - ${link.text || 'No text'}: ${link.url}`);
        });
        
        // Also look for navigation menu items
        console.log('\n🔍 Looking in navigation menus:\n');
        
        $('nav a, .navigation a, .menu a, [class*="nav"] a').each((i, elem) => {
            const href = $(elem).attr('href');
            const text = $(elem).text().trim();
            
            if (href && href.includes('/c/hunde/') && 
                (text.toLowerCase().includes('futter') || 
                 text.toLowerCase().includes('nahrung'))) {
                console.log(`  - ${text}: ${href}`);
            }
        });
        
        // Test specific food category patterns
        console.log('\n🔍 Testing specific category URLs:\n');
        
        const testUrls = [
            '/c/hunde/hundefutter-trocken/',
            '/c/hunde/hundefutter-nass/',
            '/c/hunde/trockenfutter/',
            '/c/hunde/nassfutter/',
            '/c/hunde/hundefutter/',
            '/c/hunde/hundenahrung/'
        ];
        
        for (const testUrl of testUrls) {
            const fullUrl = `https://www.fressnapf.de${testUrl}`;
            console.log(`\nTesting: ${fullUrl}`);
            
            try {
                const testResponse = await axios.get(fullUrl, {
                    headers: {
                        'User-Agent': userAgent
                    },
                    timeout: 5000
                });
                
                console.log(`  ✅ Status: ${testResponse.status}`);
                
                const $test = cheerio.load(testResponse.data);
                const productCount = $test('[class*="product"], [data-product]').length;
                console.log(`  Products found: ${productCount}`);
                
            } catch (error) {
                console.log(`  ❌ Error: ${error.response?.status || error.message}`);
            }
            
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
        
    } catch (error) {
        console.error('Error loading main page:', error.message);
    }
}

testFressnapfCategories().catch(console.error);