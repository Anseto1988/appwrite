const axios = require('axios');
const cheerio = require('cheerio');

async function debugProductExtraction() {
    console.log('🔍 Debug Product Data Extraction\n');
    
    const userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';
    
    // Test a Fressnapf product
    console.log('1️⃣ Testing Fressnapf Product Page');
    console.log('='.repeat(60));
    
    try {
        const fressnapfUrl = 'https://www.fressnapf.de/p/bosch-high-premium-concept-adult-lamm--reis-15-kg-1232604003/';
        const response = await axios.get(fressnapfUrl, {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 20000
        });
        
        const $ = cheerio.load(response.data);
        
        console.log('\n📋 Page structure analysis:');
        
        // Look for EAN patterns in the entire page
        console.log('\n🔍 Searching for EAN...');
        const pageText = response.data;
        
        // Various EAN patterns
        const eanPatterns = [
            /"ean":"(\d{8,14})"/i,
            /"gtin\d*":"(\d{8,14})"/i,
            /EAN[:\s]*(\d{8,14})/i,
            /GTIN[:\s]*(\d{8,14})/i,
            /"sku":"(\d{8,14})"/i,
            /data-ean="(\d{8,14})"/i,
            /data-gtin="(\d{8,14})"/i
        ];
        
        let eanFound = false;
        for (const pattern of eanPatterns) {
            const match = pageText.match(pattern);
            if (match) {
                console.log(`  ✅ EAN found with pattern ${pattern}: ${match[1]}`);
                eanFound = true;
                break;
            }
        }
        
        if (!eanFound) {
            console.log('  ❌ No EAN found with standard patterns');
            
            // Look for product details section
            console.log('\n  Looking in product details sections...');
            $('.product-details, .product-info, [class*="detail"], [class*="info"]').each((i, elem) => {
                const text = $(elem).text();
                if (text.match(/\d{8,14}/)) {
                    console.log(`    Found number in ${$(elem).attr('class')}: ${text.match(/\d{8,14}/)[0]}`);
                }
            });
        }
        
        // Look for nutritional info
        console.log('\n🍖 Searching for nutritional information...');
        
        const nutritionPatterns = [
            'analytische bestandteile',
            'zusammensetzung',
            'inhaltsstoffe',
            'nährwerte',
            'protein',
            'fett'
        ];
        
        let nutritionFound = false;
        nutritionPatterns.forEach(pattern => {
            if (pageText.toLowerCase().includes(pattern)) {
                console.log(`  ✅ Found "${pattern}" in page`);
                nutritionFound = true;
            }
        });
        
        if (nutritionFound) {
            // Try to find the actual nutrition section
            $('[class*="tab"], [class*="accordion"], [class*="detail"], [class*="description"]').each((i, elem) => {
                const text = $(elem).text();
                if (text.toLowerCase().includes('analytische bestandteile')) {
                    console.log('\n  📊 Found nutrition section:');
                    console.log(`    ${text.substring(0, 200)}...`);
                }
            });
        }
        
    } catch (error) {
        console.error('Fressnapf error:', error.message);
    }
    
    // Test a Zooplus product
    console.log('\n\n2️⃣ Testing Zooplus Product Page');
    console.log('='.repeat(60));
    
    try {
        const zooplusUrl = 'https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter/bosch/bosch_senior/567893';
        const response = await axios.get(zooplusUrl, {
            headers: {
                'User-Agent': userAgent,
                'Accept': 'text/html,application/xhtml+xml',
                'Accept-Language': 'de-DE,de;q=0.9'
            },
            timeout: 20000
        });
        
        const $ = cheerio.load(response.data);
        const pageText = response.data;
        
        console.log('\n🔍 Searching for EAN...');
        
        // Look for EAN
        const eanPatterns = [
            /"ean":"(\d{8,14})"/i,
            /"gtin\d*":"(\d{8,14})"/i,
            /EAN[:\s]*(\d{8,14})/i,
            /GTIN[:\s]*(\d{8,14})/i,
            /"articleNumber":"(\d{8,14})"/i
        ];
        
        let eanFound = false;
        for (const pattern of eanPatterns) {
            const match = pageText.match(pattern);
            if (match) {
                console.log(`  ✅ EAN found: ${match[1]}`);
                eanFound = true;
                break;
            }
        }
        
        if (!eanFound) {
            console.log('  ❌ No EAN found');
            
            // Check JSON-LD
            $('script[type="application/ld+json"]').each((i, elem) => {
                try {
                    const data = JSON.parse($(elem).html());
                    console.log(`    JSON-LD type: ${data['@type']}`);
                    if (data.gtin || data.sku || data.productID) {
                        console.log(`    Found ID: ${data.gtin || data.sku || data.productID}`);
                    }
                } catch (e) {}
            });
        }
        
        // Look for nutritional info
        console.log('\n🍖 Searching for nutritional information...');
        
        if (pageText.toLowerCase().includes('analytische bestandteile')) {
            console.log('  ✅ Found "analytische bestandteile"');
            
            // Find the section
            const nutritionMatch = pageText.match(/analytische bestandteile[^<]*(<[^>]*>)?([^<]+)/i);
            if (nutritionMatch) {
                console.log(`    Content: ${nutritionMatch[2].substring(0, 200)}...`);
            }
        }
        
        // Look for tabs or sections that might contain nutrition
        console.log('\n  Checking product information sections...');
        $('[class*="ProductAttribute"], [class*="product-info"], [class*="description"]').each((i, elem) => {
            const text = $(elem).text();
            if (text.includes('%') && (text.includes('Protein') || text.includes('Fett'))) {
                console.log(`    Found nutrition in ${$(elem).attr('class')}: ${text.substring(0, 100)}...`);
            }
        });
        
    } catch (error) {
        console.error('Zooplus error:', error.message);
    }
}

debugProductExtraction().catch(console.error);