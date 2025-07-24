const { FressnapfParser } = require('./parsers/fressnapf');

async function testFressnapfFixed() {
    console.log('🧪 Testing Fixed Fressnapf Pagination\n');
    
    const parser = new FressnapfParser();
    
    // Test first 3 pages
    for (let page = 1; page <= 3; page++) {
        console.log(`\n📄 Testing Page ${page}`);
        console.log('='.repeat(60));
        
        try {
            const products = await parser.fetchProducts(page, 5); // Only 5 products per page for testing
            
            console.log(`Found ${products.length} products with complete nutrient data\n`);
            
            if (products.length > 0) {
                // Show first 2 products
                products.slice(0, 2).forEach((product, index) => {
                    console.log(`Product ${index + 1}:`);
                    console.log(`  EAN: ${product.ean}`);
                    console.log(`  Brand: ${product.brand}`);
                    console.log(`  Name: ${product.product}`);
                    console.log(`  Nutrients: Protein ${product.protein}%, Fat ${product.fat}%, Fiber ${product.crudeFiber}%, Ash ${product.rawAsh}%`);
                    console.log('');
                });
            }
            
        } catch (error) {
            console.log(`Error on page ${page}: ${error.message}`);
        }
    }
}

testFressnapfFixed().catch(console.error);