const { FressnapfParser } = require('./parsers/fressnapf');
const { ZooplusParser } = require('./parsers/zooplus');

async function testSingleProduct() {
    console.log('🧪 Testing Single Product Extraction\n');
    
    // Test Fressnapf
    console.log('1️⃣ Testing Fressnapf Product');
    console.log('='.repeat(60));
    
    const fressnapfParser = new FressnapfParser();
    const fressnapfUrl = 'https://www.fressnapf.de/p/bosch-high-premium-concept-adult-lamm--reis-15-kg-1232604003/';
    
    try {
        const product = await fressnapfParser.fetchProductDetails(fressnapfUrl);
        
        if (product) {
            console.log('\n✅ Product extracted successfully:');
            console.log(JSON.stringify(product, null, 2));
        } else {
            console.log('\n❌ Failed to extract product');
        }
    } catch (error) {
        console.log('\n❌ Error:', error.message);
    }
    
    // Test Zooplus
    console.log('\n\n2️⃣ Testing Zooplus Product');
    console.log('='.repeat(60));
    
    const zooplusParser = new ZooplusParser();
    const zooplusUrl = 'https://www.zooplus.de/shop/hunde/hundefutter_trockenfutter/bosch/bosch_senior/567893';
    
    try {
        const product = await zooplusParser.fetchProductDetails(zooplusUrl);
        
        if (product) {
            console.log('\n✅ Product extracted successfully:');
            console.log(JSON.stringify(product, null, 2));
        } else {
            console.log('\n❌ Failed to extract product');
        }
    } catch (error) {
        console.log('\n❌ Error:', error.message);
    }
}

testSingleProduct().catch(console.error);