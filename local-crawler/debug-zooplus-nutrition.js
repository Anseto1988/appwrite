const axios = require('axios');
const cheerio = require('cheerio');

async function debugZooplusNutrition() {
    console.log('🔍 Debug Zooplus Nutrition Extraction\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    const productUrl = 'https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter/bosch/bosch_senior/567893';
    
    try {
        const response = await axios.get(productUrl, {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 20000
        });
        
        const $ = cheerio.load(response.data);
        
        console.log('📋 Looking for nutrition data:\n');
        
        // Search for various patterns in the page
        const searchTerms = [
            'analytische bestandteile',
            'protein',
            'fett',
            'rohfaser',
            'rohasche',
            'feuchtigkeit'
        ];
        
        searchTerms.forEach(term => {
            if (response.data.toLowerCase().includes(term)) {
                console.log(`✅ Found "${term}" in page`);
                
                // Find the context
                const index = response.data.toLowerCase().indexOf(term);
                const context = response.data.substring(Math.max(0, index - 100), Math.min(response.data.length, index + 200));
                console.log(`   Context: ...${context.replace(/\s+/g, ' ').substring(0, 150)}...\n`);
            }
        });
        
        // Look for specific data structures
        console.log('\n🔍 Checking data structures:');
        
        // Check if data is in JavaScript variables
        const scriptMatches = response.data.match(/window\.__[A-Z_]+__\s*=\s*({[\s\S]+?});/g);
        if (scriptMatches) {
            scriptMatches.forEach((match, i) => {
                if (match.includes('analytische') || match.includes('protein')) {
                    console.log(`\n📊 Found nutrition data in script ${i}:`);
                    console.log(match.substring(0, 200) + '...');
                }
            });
        }
        
        // Check specific selectors
        console.log('\n🔍 Checking specific selectors:');
        
        const selectors = [
            '[class*="ProductAttribute"]',
            '[class*="product-detail"]',
            '[class*="description"]',
            '[class*="nutrition"]',
            '[class*="ingredient"]',
            '.tab-content',
            '[role="tabpanel"]',
            '[data-test*="product"]'
        ];
        
        selectors.forEach(selector => {
            $(selector).each((i, elem) => {
                const text = $(elem).text();
                if (text.includes('%') && (text.includes('Protein') || text.includes('Fett'))) {
                    console.log(`\n✅ Found nutrition in ${selector}:`);
                    console.log(`   ${text.substring(0, 200).replace(/\s+/g, ' ')}...`);
                }
            });
        });
        
        // Look for hidden elements
        console.log('\n🔍 Checking hidden elements:');
        $('[style*="display:none"], [hidden], .hidden').each((i, elem) => {
            const text = $(elem).text();
            if (text.includes('analytische') || (text.includes('%') && text.includes('Protein'))) {
                console.log('  Found nutrition in hidden element!');
                console.log(`  ${text.substring(0, 150).replace(/\s+/g, ' ')}...`);
            }
        });
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

debugZooplusNutrition().catch(console.error);