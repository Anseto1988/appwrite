# SnackTrack Appwrite Backend Setup Guide

## Overview
This guide provides step-by-step instructions for setting up and configuring the Appwrite backend for the SnackTrack application.

## Prerequisites
- Node.js 16+ installed
- Access to Appwrite instance at https://parse.nordburglarp.de/v2
- API Key with appropriate permissions
- Project ID from Appwrite console

## Environment Configuration

### 1. Create Environment File
Create a `.env` file in the project root:

```bash
# Appwrite Configuration
APPWRITE_ENDPOINT=https://parse.nordburglarp.de/v2
APPWRITE_PROJECT_ID=your_project_id_here
APPWRITE_API_KEY=your_api_key_here
APPWRITE_DATABASE_ID=snacktrack-db
```

### 2. Security Note
⚠️ **IMPORTANT**: Never commit the `.env` file to version control. The `.gitignore` file should include:
```
.env
.env.local
.env.*.local
```

## Installation Steps

### 1. Navigate to Backend Directory
```bash
cd backend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Verify Connection
Test the connection to your Appwrite instance:
```bash
npm run verify
```

Expected output:
```
🔍 Verifying Appwrite Connection...
✅ API Health: pass
✅ Database Health: pass
✅ Storage Health: pass
✅ Connection verification completed successfully!
```

### 4. Initialize Database Schema
Create all required collections and attributes:
```bash
npm run init-db
```

This will create:
- Database: `snacktrack-db`
- Collections: dogs, foods, feedings, weightEntries, etc.
- Attributes and indexes for each collection

### 5. Setup Storage Buckets
Create storage buckets for images:
```bash
node setup-storage-buckets.js
```

This creates:
- `dog_images` - For dog profile pictures
- `food_images` - For food product images
- `avatars` - For user profile pictures

### 6. Test Operations
Run the test script to verify CRUD operations:
```bash
node test-appwrite-operations.js
```

## Manual Setup (Alternative)

If automated scripts fail, you can set up collections manually through the Appwrite console:

### 1. Create Database
1. Go to Databases section in Appwrite console
2. Click "Create Database"
3. Name: "SnackTrack Database"
4. ID: `snacktrack-db`

### 2. Create Collections
For each collection listed in `APPWRITE_COLLECTIONS.md`:
1. Click "Create Collection"
2. Set the collection ID and name
3. Add all required attributes
4. Create indexes
5. Set permissions

### 3. Create Storage Buckets
1. Go to Storage section
2. Create buckets: `dog_images`, `food_images`, `avatars`
3. Set file size limits and allowed extensions

## Android App Configuration

### 1. Update BuildConfig
Ensure your `app/build.gradle.kts` includes:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "APPWRITE_ENDPOINT", "\"${System.getenv("APPWRITE_ENDPOINT") ?: "https://parse.nordburglarp.de/v2"}\"")
        buildConfigField("String", "APPWRITE_PROJECT_ID", "\"${System.getenv("APPWRITE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "APPWRITE_DATABASE_ID", "\"${System.getenv("APPWRITE_DATABASE_ID") ?: "snacktrack-db"}\"")
        // Note: API_KEY should not be included in client apps
    }
}
```

### 2. AppwriteService Configuration
The `AppwriteService.kt` is already configured to use these values:

```kotlin
object AppwriteConfig {
    val ENDPOINT = BuildConfig.APPWRITE_ENDPOINT
    val PROJECT_ID = BuildConfig.APPWRITE_PROJECT_ID
    val DATABASE_ID = BuildConfig.APPWRITE_DATABASE_ID
}
```

## Authentication Setup

### User Authentication Flow
1. **Email/Password**: Built-in support via `account.create()` and `account.createSession()`
2. **OAuth**: Configure providers in Appwrite console (Google, Apple, etc.)
3. **Session Management**: Handled automatically with cookie persistence

### Important Notes:
- Never use API keys in client applications
- Always use user sessions for authentication
- API keys are only for server-side operations

## Troubleshooting

### Connection Issues
1. **401 Unauthorized**: Check API key permissions
2. **404 Not Found**: Verify endpoint URL and project ID
3. **Network Error**: Check internet connection and firewall settings

### Database Issues
1. **Collection not found**: Run `npm run init-db` to create collections
2. **Attribute not found**: Check collection schema matches app models
3. **Index errors**: Ensure indexes are created after attributes

### Common Errors and Solutions

#### "User (role: guests) missing scope (account)"
- This occurs when using API key in client app
- Solution: Remove API key from client, use user authentication

#### "Database not found"
- Database ID mismatch
- Solution: Verify `APPWRITE_DATABASE_ID` matches created database

#### "Network request failed"
- SSL/TLS issues or network connectivity
- Solution: Check endpoint URL uses HTTPS, verify network settings

## Maintenance

### Regular Tasks
1. **Monitor Usage**: Check Appwrite dashboard for API usage
2. **Backup Data**: Regular database exports
3. **Update Indexes**: Optimize based on query patterns
4. **Clean Storage**: Remove orphaned images periodically

### Performance Optimization
1. Use appropriate indexes for frequent queries
2. Implement pagination for large datasets
3. Cache frequently accessed data client-side
4. Use batch operations where possible

## Security Best Practices

1. **API Keys**:
   - Use separate keys for different environments
   - Rotate keys regularly
   - Never expose keys in client code

2. **Permissions**:
   - Follow principle of least privilege
   - Use role-based access control
   - Validate all user inputs

3. **Data Protection**:
   - Enable encryption for sensitive data
   - Use HTTPS for all communications
   - Implement rate limiting

## Support Resources

- **Appwrite Documentation**: https://appwrite.io/docs
- **SnackTrack Issues**: GitHub repository issues
- **Community Support**: Appwrite Discord server

## Next Steps

After successful setup:
1. Run the Android app and test authentication
2. Verify data synchronization works
3. Test offline capabilities
4. Monitor performance and errors

For development workflow, see `DEVELOPER_SETUP.md`.