const sdk = require('node-appwrite');

// Initialize client
const client = new sdk.Client();
const functions = new sdk.Functions(client);

console.log('Available Functions SDK methods:\n');

// List all methods on the Functions instance
const methods = Object.getOwnPropertyNames(Object.getPrototypeOf(functions))
    .filter(method => method !== 'constructor' && typeof functions[method] === 'function')
    .sort();

methods.forEach(method => {
    console.log(`- ${method}`);
});

console.log('\n\nChecking for deployment-related methods:');
console.log('- updateDeployment:', typeof functions.updateDeployment);
console.log('- activateDeployment:', typeof functions.activateDeployment);
console.log('- setDeployment:', typeof functions.setDeployment);
console.log('- updateFunction:', typeof functions.updateFunction);
console.log('- update:', typeof functions.update);