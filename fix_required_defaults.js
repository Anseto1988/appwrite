const fs = require('fs');
const path = require('path');

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && file !== 'fix_permissions.js' && file !== 'fix_required_defaults.js'
);

setupFiles.forEach(file => {
    console.log(`Fixing required defaults in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    
    // Remove default values for required boolean attributes
    content = content.replace(
        /createBooleanAttribute\([^,]+,\s*[^,]+,\s*[^,]+,\s*true,\s*true\)/g,
        (match) => match.replace(', true)', ')')
    );
    
    content = content.replace(
        /createBooleanAttribute\([^,]+,\s*[^,]+,\s*[^,]+,\s*true,\s*false\)/g,
        (match) => match.replace(', false)', ')')
    );
    
    // Remove default values for required enum attributes that might have defaults
    content = content.replace(
        /createEnumAttribute\([^,]+,\s*[^,]+,\s*[^,]+,\s*\[[^\]]+\],\s*true,?\s*[^)]*\)/g,
        (match) => {
            // Keep only the first 4 parameters for required enum attributes
            const parts = match.split(',');
            if (parts.length > 4) {
                return parts.slice(0, 4).join(',') + ')';
            }
            return match;
        }
    );
    
    fs.writeFileSync(file, content);
    console.log(`✓ Fixed required defaults in ${file}`);
});

console.log('All required default values removed!');