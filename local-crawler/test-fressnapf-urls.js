const axios = require('axios');

async function testFressnapfUrls() {
    console.log('🔍 Testing Fressnapf URLs and Structure\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    // Test various possible URLs
    const urlsToTest = [
        'https://www.fressnapf.de/hunde/futter/',
        'https://www.fressnapf.de/hunde/hundefutter/',
        'https://www.fressnapf.de/c/hunde/futter/',
        'https://www.fressnapf.de/c/hunde/hundefutter/',
        'https://www.fressnapf.de/c/hunde/',
        'https://www.fressnapf.de/hunde/',
        'https://www.fressnapf.de'
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
                               response.data.includes('data-product') ||
                               response.data.includes('productGrid');
            console.log(`   Has product data: ${hasProducts ? '✅' : '❌'}`);
            
            // Look for specific Fressnapf patterns
            if (response.data.includes('__NEXT_DATA__')) {
                console.log('   💡 Found Next.js app data');
            }
            
            if (response.data.includes('productList')) {
                console.log('   💡 Found product list');
            }
            
            // Extract product links
            const productLinkPattern = /href="([^"]*\/p\/[^"]*)"/g;
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
    
    // Test search/category API
    console.log('\n\n🔍 Testing API endpoints');
    console.log('='.repeat(60));
    
    const apiEndpoints = [
        'https://www.fressnapf.de/api/v1/products?category=hunde-futter',
        'https://www.fressnapf.de/api/products/search?q=hundefutter',
        'https://www.fressnapf.de/_next/data/buildId/hunde/futter.json'
    ];
    
    for (const endpoint of apiEndpoints) {
        console.log(`\nTesting: ${endpoint}`);
        try {
            const response = await axios.get(endpoint, {
                headers: {
                    'User-Agent': userAgent,
                    'Accept': 'application/json',
                    'Accept-Language': 'de-DE,de;q=0.9'
                },
                timeout: 5000
            });
            
            console.log(`✅ API Response: ${response.status}`);
            console.log(`   Content-Type: ${response.headers['content-type']}`);
            
        } catch (error) {
            console.log(`❌ API Error: ${error.message}`);
        }
    }
}

testFressnapfUrls().catch(console.error);