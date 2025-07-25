const fs = require('fs');

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && !file.includes('fix_') && !file.includes('update_')
);

setupFiles.forEach(file => {
    console.log(`Checking syntax in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    let changed = false;
    
    // Fix broken enum arrays that still have syntax errors
    const fixes = [
        { pattern: /['DAILY'\)/, replacement: '\'DAILY\', \'WEEKLY\', \'MONTHLY\', \'CUSTOM\'], true);' },
        { pattern: /['WEIGHT_MANAGEMENT'\)/, replacement: '\'WEIGHT_MANAGEMENT\', \'ALLERGIES\', \'PERFORMANCE\', \'HEALTH\', \'BEHAVIOR\'], true);' },
        { pattern: /['GENERIC'\)/, replacement: '\'GENERIC\', \'BREED_SPECIFIC\', \'CONDITION_SPECIFIC\', \'ACTIVITY_BASED\'], true);' },
        { pattern: /['ACTIVE'\)/, replacement: '\'ACTIVE\', \'PAUSED\', \'ACCEPTED\', \'DECLINED\', \'EXPIRED\'], true);' },
        { pattern: /['VERY_ACCURATE'\)/, replacement: '\'VERY_ACCURATE\', \'ACCURATE\', \'MODERATE\', \'LOW\', \'EXPERIMENTAL\'], true);' },
        { pattern: /['LEARNING'\)/, replacement: '\'LEARNING\', \'STABLE\', \'IMPROVING\', \'DEGRADING\'], true);' },
        { pattern: /['SYSTEM_GENERATED'\)/, replacement: '\'SYSTEM_GENERATED\', \'USER_REPORTED\', \'MANUAL_REVIEW\'], true);' },
        { pattern: /['REGRESSION'\)/, replacement: '\'REGRESSION\', \'CLASSIFICATION\', \'CLUSTERING\', \'RECOMMENDATION\'], true);' },
        { pattern: /['REAL_TIME'\)/, replacement: '\'REAL_TIME\', \'BATCH\', \'SCHEDULED\'], true);' },
        { pattern: /['RULE_BASED'\)/, replacement: '\'RULE_BASED\', \'ML_BASED\', \'HYBRID\'], true);' },
        { pattern: /['TRAINING'\)/, replacement: '\'TRAINING\', \'READY\', \'DEPLOYED\', \'DEPRECATED\'], true);' }
    ];
    
    fixes.forEach(fix => {
        if (content.includes(fix.pattern.source || fix.pattern.toString().slice(1, -1))) {
            content = content.replace(fix.pattern, fix.replacement);
            changed = true;
        }
    });
    
    if (changed) {
        fs.writeFileSync(file, content);
        console.log(`✓ Fixed syntax errors in ${file}`);
    } else {
        console.log(`  No syntax errors found in ${file}`);
    }
});

console.log('Syntax check complete!');