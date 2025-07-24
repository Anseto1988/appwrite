require('dotenv').config();
const { FressnapfParser } = require('./parsers/fressnapf');
const { ZooplusParser } = require('./parsers/zooplus');

async function testUpdatedCrawlers() {
    console.log('🧪 Testing Updated Web Crawlers\n');
    
    // Test Fressnapf
    console.log('1️⃣ Testing Fressnapf Parser');
    console.log('='.repeat(60));
    
    const fressnapfParser = new FressnapfParser();
    try {
        const fressnapfProducts = await fressnapfParser.fetchProducts(1, 2); // Just 2 products for testing
        
        console.log(`\n✅ Fressnapf Results:`);
        console.log(`   Products found: ${fressnapfProducts.length}`);
        
        if (fressnapfProducts.length > 0) {
            console.log('\n   Sample product:');
            const product = fressnapfProducts[0];
            console.log(`   - EAN: ${product.ean}`);
            console.log(`   - Brand: ${product.brand}`);
            console.log(`   - Product: ${product.product}`);
            console.log(`   - Protein: ${product.protein}%`);
            console.log(`   - Fat: ${product.fat}%`);
            console.log(`   - Fiber: ${product.crudeFiber}%`);
            console.log(`   - Ash: ${product.rawAsh}%`);
            console.log(`   - Moisture: ${product.moisture}%`);
        }
    } catch (error) {
        console.log(`\n❌ Fressnapf Error: ${error.message}`);
    }
    
    // Test Zooplus
    console.log('\n\n2️⃣ Testing Zooplus Parser');
    console.log('='.repeat(60));
    
    const zooplusParser = new ZooplusParser();
    try {
        const zooplusProducts = await zooplusParser.fetchProducts(1, 2); // Just 2 products for testing
        
        console.log(`\n✅ Zooplus Results:`);
        console.log(`   Products found: ${zooplusProducts.length}`);
        
        if (zooplusProducts.length > 0) {
            console.log('\n   Sample product:');
            const product = zooplusProducts[0];
            console.log(`   - EAN: ${product.ean}`);
            console.log(`   - Brand: ${product.brand}`);
            console.log(`   - Product: ${product.product}`);
            console.log(`   - Protein: ${product.protein}%`);
            console.log(`   - Fat: ${product.fat}%`);
            console.log(`   - Fiber: ${product.crudeFiber}%`);
            console.log(`   - Ash: ${product.rawAsh}%`);
            console.log(`   - Moisture: ${product.moisture}%`);
        }
    } catch (error) {
        console.log(`\n❌ Zooplus Error: ${error.message}`);
    }
    
    console.log('\n\n✅ Test completed!');
}

testUpdatedCrawlers().catch(console.error);