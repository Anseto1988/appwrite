const fs = require('fs');

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && !file.includes('fix_')
);

setupFiles.forEach(file => {
    console.log(`Fixing syntax errors in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    
    // Fix missing closing parentheses for enum attributes
    content = content.replace(/], true;/g, '], true);');
    content = content.replace(/], false;/g, '], false);');
    
    // Fix the incorrect medication food interaction enum
    content = content.replace(
        "['NONE', 'WIFI', 'MOBILE_2G', 'MOBILE_3G', 'MOBILE_4G', 'MOBILE_5G', 'ETHERNET']",
        "['NONE', 'WITH_FOOD', 'WITHOUT_FOOD', 'AVOID_DAIRY']"
    );
    
    fs.writeFileSync(file, content);
    console.log(`✓ Fixed syntax errors in ${file}`);
});

console.log('All syntax errors fixed!');