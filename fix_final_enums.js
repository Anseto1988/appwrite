const fs = require('fs');

// Fix the corrupted network connection type enum
const offlineFile = 'setup_offline_collections.js';
let content = fs.readFileSync(offlineFile, 'utf8');

// Fix the connectionType enum that got corrupted with food interaction values
content = content.replace(
    "['NONE', 'WITH_FOOD', 'WITHOUT_FOOD', 'AVOID_DAIRY']",
    "['NONE', 'WIFI', 'MOBILE_2G', 'MOBILE_3G', 'MOBILE_4G', 'MOBILE_5G', 'ETHERNET']"
);

fs.writeFileSync(offlineFile, content);
console.log('✓ Fixed network connection type enum');