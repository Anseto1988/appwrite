const axios = require('axios');

async function debugZooplusMoisture() {
    console.log('🔍 Debug Zooplus Moisture Extraction\n');
    
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
        
        console.log('Looking for moisture patterns in the page:\n');
        
        // Look for table section with nutrients
        const tableMatch = response.data.match(/analytische\s+bestandteile[\s\S]*?<table[\s\S]*?<\/table>/i);
        if (tableMatch) {
            console.log('Found analytical constituents table:\n');
            console.log(tableMatch[0].replace(/<!--.*?-->/g, '').replace(/\s+/g, ' '));
            console.log('\n');
            
            // Look specifically for moisture rows
            const moisturePatterns = [
                /<td>Feuchte<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                /<td>Feuchtigkeit<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                /<td>Feuchtegehalt<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                /<td>Wassergehalt<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i,
                /<td>Wasser<\/td>\s*<td>([\d.,]+)(?:<!-- -->)?(?:\s*<!-- -->)?\s*%/i
            ];
            
            console.log('Testing moisture patterns:');
            moisturePatterns.forEach((pattern, i) => {
                const match = tableMatch[0].match(pattern);
                if (match) {
                    console.log(`  ✅ Pattern ${i} matched: ${match[1]}%`);
                } else {
                    console.log(`  ❌ Pattern ${i} did not match`);
                }
            });
            
            // Show all table rows
            console.log('\nAll table rows:');
            const rows = tableMatch[0].match(/<tr>[\s\S]*?<\/tr>/g);
            if (rows) {
                rows.forEach(row => {
                    const cleanRow = row.replace(/<!--.*?-->/g, '').replace(/\s+/g, ' ');
                    console.log(`  ${cleanRow}`);
                });
            }
        }
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

debugZooplusMoisture().catch(console.error);