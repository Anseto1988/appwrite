require('dotenv').config();
const sdk = require('node-appwrite');

async function checkNutrients() {
    console.log('🔍 Checking Saved Product Nutrients\n');
    
    const client = new sdk.Client();
    client
        .setEndpoint(process.env.APPWRITE_ENDPOINT)
        .setProject(process.env.APPWRITE_PROJECT_ID)
        .setKey(process.env.APPWRITE_API_KEY);
    
    const databases = new sdk.Databases(client);
    
    try {
        // Get recent food submissions
        const submissions = await databases.listDocuments(
            process.env.DATABASE_ID,
            process.env.SUBMISSIONS_COLLECTION_ID,
            [
                sdk.Query.orderDesc('submittedAt'),
                sdk.Query.limit(10)
            ]
        );
        
        console.log(`Found ${submissions.documents.length} recent submissions\n`);
        
        // Analyze nutrient data
        let withProtein = 0;
        let withFat = 0;
        let withFiber = 0;
        let withAsh = 0;
        let withMoisture = 0;
        let withAnyNutrient = 0;
        
        submissions.documents.forEach((doc, index) => {
            console.log(`${index + 1}. ${doc.product} (${doc.brand})`);
            console.log(`   EAN: ${doc.ean}`);
            console.log(`   Nutrients:`);
            console.log(`     Protein: ${doc.protein}%`);
            console.log(`     Fat: ${doc.fat}%`);
            console.log(`     Crude Fiber: ${doc.crudeFiber}%`);
            console.log(`     Raw Ash: ${doc.rawAsh}%`);
            console.log(`     Moisture: ${doc.moisture}%`);
            
            if (doc.protein > 0) withProtein++;
            if (doc.fat > 0) withFat++;
            if (doc.crudeFiber > 0) withFiber++;
            if (doc.rawAsh > 0) withAsh++;
            if (doc.moisture > 0) withMoisture++;
            
            if (doc.protein > 0 || doc.fat > 0 || doc.crudeFiber > 0 || doc.rawAsh > 0 || doc.moisture > 0) {
                withAnyNutrient++;
                console.log(`   ✅ Has nutrient data`);
            } else {
                console.log(`   ❌ No nutrient data`);
            }
            
            console.log('');
        });
        
        // Summary
        console.log('📊 NUTRIENT DATA SUMMARY');
        console.log('=' . repeat(40));
        console.log(`Total products analyzed: ${submissions.documents.length}`);
        console.log(`Products with any nutrients: ${withAnyNutrient} (${Math.round(withAnyNutrient/submissions.documents.length*100)}%)`);
        console.log(`Products with protein: ${withProtein} (${Math.round(withProtein/submissions.documents.length*100)}%)`);
        console.log(`Products with fat: ${withFat} (${Math.round(withFat/submissions.documents.length*100)}%)`);
        console.log(`Products with fiber: ${withFiber} (${Math.round(withFiber/submissions.documents.length*100)}%)`);
        console.log(`Products with ash: ${withAsh} (${Math.round(withAsh/submissions.documents.length*100)}%)`);
        console.log(`Products with moisture: ${withMoisture} (${Math.round(withMoisture/submissions.documents.length*100)}%)`);
        
    } catch (error) {
        console.error('Error:', error.message);
    }
}

checkNutrients();