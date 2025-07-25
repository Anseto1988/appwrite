const fs = require('fs');

const apiKey = 'standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5';

// Get all setup_*.js files
const setupFiles = fs.readdirSync('.').filter(file => 
    file.startsWith('setup_') && file.endsWith('.js') && !file.includes('fix_') && !file.includes('update_')
);

setupFiles.forEach(file => {
    console.log(`Updating API key in ${file}...`);
    
    let content = fs.readFileSync(file, 'utf8');
    
    // Replace environment variable with direct key
    content = content.replace(
        '.setKey(process.env.APPWRITE_API_KEY)',
        `.setKey('${apiKey}')`
    );
    
    fs.writeFileSync(file, content);
    console.log(`✓ Updated API key in ${file}`);
});

console.log('All API keys updated!');