# SnackTrack Backend Scripts

This directory contains scripts and documentation for setting up and managing the Appwrite backend for SnackTrack.

## Quick Start

1. **Setup Environment**
   ```bash
   cp .env.example .env
   # Edit .env with your Appwrite credentials
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Verify Connection**
   ```bash
   npm run verify
   ```

4. **Initialize Database**
   ```bash
   npm run init-db
   ```

5. **Setup Storage**
   ```bash
   node setup-storage-buckets.js
   ```

## Available Scripts

- `npm run verify` - Test connection to Appwrite
- `npm run init-db` - Create all collections with schema
- `npm run setup` - Run all setup scripts in sequence
- `node create-collections.js` - Create collections individually
- `node setup-storage-buckets.js` - Create storage buckets
- `node test-appwrite-operations.js` - Test CRUD operations

## Documentation

- **[APPWRITE_SETUP_GUIDE.md](./APPWRITE_SETUP_GUIDE.md)** - Complete setup guide
- **[APPWRITE_COLLECTIONS.md](./APPWRITE_COLLECTIONS.md)** - Detailed collection schemas

## Environment Variables

Required environment variables (see `.env.example`):
- `APPWRITE_ENDPOINT` - Appwrite server URL
- `APPWRITE_PROJECT_ID` - Your project ID
- `APPWRITE_API_KEY` - API key with admin permissions
- `APPWRITE_DATABASE_ID` - Database ID (default: snacktrack-db)

## Security Notes

⚠️ **Important**: 
- Never commit `.env` files to version control
- API keys should only be used server-side
- Android app should use user authentication, not API keys

## Troubleshooting

If you encounter issues:
1. Check your internet connection
2. Verify your API key has appropriate permissions
3. Ensure the Appwrite endpoint is accessible
4. Check the console for detailed error messages

For more help, see the full setup guide or open an issue on GitHub.