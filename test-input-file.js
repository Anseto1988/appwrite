const sdk = require('node-appwrite');

console.log('SDK object:', Object.keys(sdk));
console.log('\nInputFile:', sdk.InputFile);
console.log('InputFile type:', typeof sdk.InputFile);

if (sdk.InputFile) {
    console.log('\nInputFile methods:');
    console.log(Object.getOwnPropertyNames(sdk.InputFile));
    console.log('\nInputFile prototype:');
    console.log(Object.getOwnPropertyNames(sdk.InputFile.prototype || {}));
}