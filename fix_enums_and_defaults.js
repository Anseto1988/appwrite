const fs = require('fs');

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && !file.includes('fix_')
);

setupFiles.forEach(file => {
    console.log(`Fixing enums and defaults in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    
    // Fix broken enum arrays by restoring complete enum definitions
    const enumFixes = {
        "['FOOD')": "['FOOD', 'ENVIRONMENTAL', 'MEDICATION', 'CONTACT', 'OTHER'], true",
        "['MILD')": "['MILD', 'MODERATE', 'SEVERE'], true",
        "['ORAL')": "['ORAL', 'TOPICAL', 'INJECTION', 'INHALED', 'OTHER'], true",
        "['ONCE')": "['ONCE_DAILY', 'TWICE_DAILY', 'THREE_TIMES_DAILY', 'AS_NEEDED', 'WEEKLY', 'MONTHLY'], true",
        "['NONE')": "['NONE', 'WITH_FOOD', 'WITHOUT_FOOD', 'AVOID_DAIRY'], true",
        "['OBSERVATION')": "['OBSERVATION', 'SYMPTOM', 'BEHAVIOR', 'EMERGENCY', 'ROUTINE_CHECK'], true",
        "['NO_APPETITE')": "['NO_APPETITE', 'REDUCED', 'NORMAL', 'INCREASED'], false",
        "['VERY_LOW')": "['VERY_LOW', 'LOW', 'NORMAL', 'HIGH', 'VERY_HIGH'], false",
        "['DRY_FOOD')": "['DRY_FOOD', 'WET_FOOD', 'TREATS', 'SUPPLEMENTS', 'TOYS', 'ACCESSORIES', 'MEDICATION'], true",
        "['OFFICIAL_DATABASE')": "['OFFICIAL_DATABASE', 'USER_SUBMISSION', 'OCR_EXTRACTION', 'MANUAL_ENTRY'], true",
        "['UNVERIFIED')": "['UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED'], true",
        "['VIEW')": "['VIEW', 'PURCHASE', 'COMPARE', 'SAVE', 'SHARE'], true",
        "['INFO')": "['INFO', 'WARNING', 'DANGER', 'CRITICAL'], true",
        "['DISABLED')": "['DISABLED', 'BASIC', 'FULL', 'ADVANCED'], true",
        "['PET_STORE')": "['PET_STORE', 'SUPERMARKET', 'ONLINE', 'VETERINARY', 'SPECIALTY'], true",
        "['BUDGET')": "['BUDGET', 'MODERATE', 'PREMIUM', 'LUXURY'], true",
        "['DOG')": "['DOG', 'FEEDING', 'HEALTH_ENTRY', 'MEDICATION', 'ALLERGY', 'WEIGHT_GOAL', 'PRODUCT', 'SYNC_QUEUE'], true",
        "['LOCAL_WINS')": "['LOCAL_WINS', 'SERVER_WINS', 'MERGE', 'MANUAL_REVIEW', 'PENDING'], false",
        "['OFFLINE_START')": "['OFFLINE_START', 'OFFLINE_END', 'SYNC_START', 'SYNC_COMPLETE', 'SYNC_FAILED', 'CONFLICT_DETECTED', 'DATA_CORRUPTION'], true",
        "['NONE')": "['NONE', 'WIFI', 'MOBILE_2G', 'MOBILE_3G', 'MOBILE_4G', 'MOBILE_5G', 'ETHERNET'], true",
        "['UNKNOWN')": "['UNKNOWN', 'POOR', 'FAIR', 'GOOD', 'EXCELLENT'], true",
        "['QUICK')": "['QUICK', 'FULL', 'DEEP', 'REPAIR'], true",
        "['PENDING')": "['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true",
        "['LOSE_WEIGHT')": "['LOSE_WEIGHT', 'GAIN_WEIGHT', 'MAINTAIN_WEIGHT'], true",
        "['DRAFT')": "['DRAFT', 'ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED'], true",
        "['ROUTINE')": "['ROUTINE', 'DIAGNOSTIC', 'FOLLOW_UP', 'EMERGENCY', 'PREVENTION'], true",
        "['SCHEDULED')": "['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'OVERDUE'], true",
        "['SPRING')": "['SPRING', 'SUMMER', 'AUTUMN', 'WINTER'], true",
        "['GENERAL')": "['GENERAL', 'NUTRITION', 'EXERCISE', 'GROOMING', 'MEDICAL', 'BEHAVIORAL'], true",
        "['LOW')": "['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], true",
        "['ANECDOTAL')": "['ANECDOTAL', 'OBSERVATIONAL', 'CLINICAL_STUDY', 'RANDOMIZED_TRIAL', 'META_ANALYSIS'], true"
    };
    
    // Apply enum fixes
    Object.entries(enumFixes).forEach(([broken, fixed]) => {
        content = content.replace(new RegExp(broken.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), fixed);
    });
    
    // Remove default values from required boolean attributes only if they have defaults
    content = content.replace(
        /createBooleanAttribute\(([^,]+),\s*([^,]+),\s*([^,]+),\s*true,\s*(true|false)\)/g,
        'createBooleanAttribute($1, $2, $3, true)'
    );
    
    fs.writeFileSync(file, content);
    console.log(`✓ Fixed enums and defaults in ${file}`);
});

console.log('All enum arrays and required defaults fixed!');