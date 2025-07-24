/**
 * Product validation service
 * Ensures all required fields are present and valid
 */
class ProductValidator {
    constructor() {
        // Define required fields and their validation rules
        this.requiredFields = {
            ean: {
                type: 'string',
                minLength: 8,
                maxLength: 13,
                pattern: /^[0-9]+$/
            },
            brand: {
                type: 'string',
                minLength: 1,
                maxLength: 100
            },
            product: {
                type: 'string',
                minLength: 1,
                maxLength: 255
            },
            protein: {
                type: 'number',
                min: 0,
                max: 100
            },
            fat: {
                type: 'number',
                min: 0,
                max: 100
            },
            crudeFiber: {
                type: 'number',
                min: 0,
                max: 100
            },
            rawAsh: {
                type: 'number',
                min: 0,
                max: 100
            },
            moisture: {
                type: 'number',
                min: 0,
                max: 100
            }
        };
        
        // Optional fields with validation rules
        this.optionalFields = {
            additives: {
                type: 'string',
                maxLength: 1000
            },
            imageUrl: {
                type: 'string',
                maxLength: 500,
                pattern: /^https?:\/\/.+/
            }
        };
    }
    
    /**
     * Validate a product object
     */
    validateProduct(product) {
        const errors = [];
        
        // Check required fields
        for (const [field, rules] of Object.entries(this.requiredFields)) {
            const value = product[field];
            const fieldErrors = this.validateField(field, value, rules, true);
            errors.push(...fieldErrors);
        }
        
        // Check optional fields if present
        for (const [field, rules] of Object.entries(this.optionalFields)) {
            if (product.hasOwnProperty(field) && product[field] !== null) {
                const value = product[field];
                const fieldErrors = this.validateField(field, value, rules, false);
                errors.push(...fieldErrors);
            }
        }
        
        // Validate nutritional values sum
        const nutritionalSum = (product.protein || 0) + 
                             (product.fat || 0) + 
                             (product.crudeFiber || 0) + 
                             (product.rawAsh || 0) + 
                             (product.moisture || 0);
        
        if (nutritionalSum > 100) {
            errors.push('Nutritional values sum exceeds 100%');
        }
        
        // Validate EAN checksum if 13 digits
        if (product.ean && product.ean.length === 13) {
            if (!this.validateEAN13(product.ean)) {
                errors.push('Invalid EAN-13 checksum');
            }
        }
        
        return {
            isValid: errors.length === 0,
            errors: errors
        };
    }
    
    /**
     * Validate a single field
     */
    validateField(fieldName, value, rules, isRequired) {
        const errors = [];
        
        // Check if required field is missing
        if (isRequired && (value === undefined || value === null || value === '')) {
            errors.push(`${fieldName} is required`);
            return errors;
        }
        
        // Skip validation if optional field is not present
        if (!isRequired && (value === undefined || value === null)) {
            return errors;
        }
        
        // Type validation
        if (rules.type) {
            const actualType = typeof value;
            if (actualType !== rules.type) {
                errors.push(`${fieldName} must be of type ${rules.type}, got ${actualType}`);
                return errors;
            }
        }
        
        // String validations
        if (rules.type === 'string') {
            if (rules.minLength && value.length < rules.minLength) {
                errors.push(`${fieldName} must be at least ${rules.minLength} characters`);
            }
            
            if (rules.maxLength && value.length > rules.maxLength) {
                errors.push(`${fieldName} must not exceed ${rules.maxLength} characters`);
            }
            
            if (rules.pattern && !rules.pattern.test(value)) {
                errors.push(`${fieldName} has invalid format`);
            }
        }
        
        // Number validations
        if (rules.type === 'number') {
            if (rules.min !== undefined && value < rules.min) {
                errors.push(`${fieldName} must be at least ${rules.min}`);
            }
            
            if (rules.max !== undefined && value > rules.max) {
                errors.push(`${fieldName} must not exceed ${rules.max}`);
            }
        }
        
        return errors;
    }
    
    /**
     * Validate EAN-13 checksum
     */
    validateEAN13(ean) {
        if (ean.length !== 13) return false;
        
        let sum = 0;
        for (let i = 0; i < 12; i++) {
            const digit = parseInt(ean[i]);
            if (isNaN(digit)) return false;
            sum += digit * (i % 2 === 0 ? 1 : 3);
        }
        
        const checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit === parseInt(ean[12]);
    }
    
    /**
     * Sanitize product data
     */
    sanitizeProduct(product) {
        const sanitized = {};
        
        // Sanitize all fields
        for (const [key, value] of Object.entries(product)) {
            if (typeof value === 'string') {
                // Remove potential XSS
                sanitized[key] = value
                    .replace(/<script[^>]*>.*?<\/script>/gi, '')
                    .replace(/<[^>]+>/g, '')
                    .trim();
            } else {
                sanitized[key] = value;
            }
        }
        
        // Ensure numbers are properly formatted
        const numericFields = ['protein', 'fat', 'crudeFiber', 'rawAsh', 'moisture'];
        for (const field of numericFields) {
            if (sanitized[field] !== undefined) {
                sanitized[field] = Math.round(parseFloat(sanitized[field]) * 100) / 100;
            }
        }
        
        return sanitized;
    }
}

module.exports = { ProductValidator };