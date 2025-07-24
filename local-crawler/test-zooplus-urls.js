const axios = require('axios');

async function testZooplusUrls() {
    console.log('🔍 Testing Zooplus URLs and Structure\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    // Test various possible URLs
    const urlsToTest = [
        'https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter',
        'https://www.zooplus.de/shop/hunde/hundefutter_nassfutter', 
        'https://www.zooplus.de/shop/hunde/trockenfutter',
        'https://www.zooplus.de/shop/hunde/nassfutter',
        'https://www.zooplus.de/hunde/hundefutter',
        'https://www.zooplus.de/tiernahrung/hundefutter',
        'https://www.zooplus.de'
    ];
    
    for (const url of urlsToTest) {
        console.log(`\nTesting: ${url}`);
        console.log('-'.repeat(60));
        
        try {
            const response = await axios.get(url, {
                headers: {
                    'User-Agent': userAgent,
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                    'Accept-Language': 'de-DE,de;q=0.9,en;q=0.8',
                    'Accept-Encoding': 'gzip, deflate, br',
                    'DNT': '1',
                    'Connection': 'keep-alive',
                    'Upgrade-Insecure-Requests': '1'
                },
                timeout: 10000,
                maxRedirects: 5
            });
            
            console.log(`✅ Status: ${response.status}`);
            console.log(`   Content-Type: ${response.headers['content-type']}`);
            console.log(`   Content-Length: ${response.data.length} chars`);
            
            // Check if it's a product listing page
            const hasProducts = response.data.includes('product') || 
                               response.data.includes('artikel') ||
                               response.data.includes('data-product');
            console.log(`   Has product data: ${hasProducts ? '✅' : '❌'}`);
            
            // Look for specific patterns
            if (response.data.includes('window.__INITIAL_STATE__')) {
                console.log('   💡 Found React/Next.js app with initial state');
            }
            
            if (response.data.includes('application/ld+json')) {
                console.log('   💡 Found structured data (JSON-LD)');
            }
            
            // Extract some product links if possible
            const productLinkPattern = /href="([^"]*\/shop\/[^"]*produkt[^"]*)"/g;
            const matches = [...response.data.matchAll(productLinkPattern)].slice(0, 3);
            if (matches.length > 0) {
                console.log('   Sample product links:');
                matches.forEach(match => console.log(`     - ${match[1]}`));
            }
            
        } catch (error) {
            console.log(`❌ Error: ${error.message}`);
            if (error.response) {
                console.log(`   Status: ${error.response.status}`);
                console.log(`   Status Text: ${error.response.statusText}`);
            }
        }
        
        // Rate limiting
        await new Promise(resolve => setTimeout(resolve, 1000));
    }
    
    // Test a specific product page
    console.log('\n\n🔍 Testing specific product page');
    console.log('='.repeat(60));
    
    try {
        // This is a common pattern for Zooplus product URLs
        const productUrl = 'https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter/royal_canin/royal_canin_size/669726';
        console.log(`Testing: ${productUrl}`);
        
        const response = await axios.get(productUrl, {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 10000
        });
        
        console.log(`✅ Product page loaded: ${response.status}`);
        
        // Look for EAN
        const eanPattern = /(?:EAN|GTIN)[:\s]*(\d{8,14})/i;
        const eanMatch = response.data.match(eanPattern);
        if (eanMatch) {
            console.log(`   EAN found: ${eanMatch[1]}`);
        }
        
        // Look for price/product data
        if (response.data.includes('itemprop="price"')) {
            console.log('   ✅ Has price microdata');
        }
        
        if (response.data.includes('Analytische Bestandteile')) {
            console.log('   ✅ Has analytical constituents');
        }
        
    } catch (error) {
        console.log(`❌ Product page error: ${error.message}`);
    }
}

testZooplusUrls().catch(console.error);