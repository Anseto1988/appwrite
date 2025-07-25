const fs = require('fs');
const path = require('path');

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && file !== 'fix_permissions.js'
);

setupFiles.forEach(file => {
    console.log(`Fixing permissions in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    
    // Replace old permission format with new format
    content = content.replace(
        /\[\s*\{\s*read:\s*\["users"\],\s*write:\s*\["users"\]\s*\}\s*\]/g,
        '[\n                "read(\\"users\\")",\n                "create(\\"users\\")",\n                "update(\\"users\\")",\n                "delete(\\"users\\")"\n            ]'
    );
    
    fs.writeFileSync(file, content);
    console.log(`✓ Fixed permissions in ${file}`);
});

console.log('All permission formats updated!');