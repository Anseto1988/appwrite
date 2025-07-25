# Appwrite Backend Configuration Guide

This comprehensive guide covers the complete Appwrite backend setup for SnackTrack, including database collections, authentication, storage, cloud functions, and security configurations.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Project Configuration](#project-configuration)
3. [Database Schema](#database-schema)
4. [Authentication Setup](#authentication-setup)
5. [Storage Configuration](#storage-configuration)
6. [Cloud Functions](#cloud-functions)
7. [Security & Permissions](#security--permissions)
8. [API Endpoints](#api-endpoints)
9. [Maintenance & Monitoring](#maintenance--monitoring)

## Overview

SnackTrack uses Appwrite as its Backend-as-a-Service (BaaS) solution, providing:
- Document database for flexible data storage
- User authentication with OAuth support
- File storage for images
- Serverless functions for automation
- Real-time capabilities for live updates

### Server Details

- **Endpoint**: `https://parse.nordburglarp.de/v2`
- **Project ID**: `snackrack2`
- **Database ID**: `snacktrack-db`
- **Region**: Self-hosted
- **Version**: Latest stable

## Project Configuration

### Initial Setup

1. **Create Project**
   ```bash
   # Using Appwrite CLI
   appwrite init project
   # Project ID: snackrack2
   # Project Name: SnackTrack
   ```

2. **Configure API Keys**
   ```bash
   # Create API key with required scopes
   appwrite projects createKey \
     --projectId=snackrack2 \
     --name="SnackTrack API" \
     --scopes="databases.read" "databases.write" "storage.read" "functions.read"
   ```

3. **Set Environment Variables**
   ```env
   APPWRITE_ENDPOINT=https://parse.nordburglarp.de/v2
   APPWRITE_PROJECT_ID=snackrack2
   APPWRITE_API_KEY=your-api-key-here
   DATABASE_ID=snacktrack-db
   ```

## Database Schema

### Collections Overview

The database uses the following collections with their specific attributes:

### 1. **dogs** Collection

Stores dog profiles and basic information.

```javascript
{
  "$id": "unique-dog-id",
  "userId": "owner-user-id",
  "name": "Max",
  "breed": "Golden Retriever",
  "birthDate": "2020-01-15",
  "weight": 30.5,
  "gender": "male",
  "neutered": true,
  "profileImageUrl": "https://...",
  "activityLevel": "active",
  "targetWeight": 28.0,
  "notes": "Friendly and energetic",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-20T15:30:00Z"
}
```

**Indexes:**
- `userId` (key) - For fetching user's dogs
- `createdAt` (key, desc) - For sorting

### 2. **foods** Collection

Master database of food products.

```javascript
{
  "$id": "unique-food-id",
  "brand": "Royal Canin",
  "product": "Medium Adult",
  "ean": "3182550402286",
  "protein": 25.0,
  "fat": 14.0,
  "crudeFiber": 2.8,
  "rawAsh": 6.1,
  "moisture": 10.0,
  "calcium": 1.2,
  "phosphorus": 0.9,
  "omega3": 0.5,
  "omega6": 2.8,
  "calories": 380,
  "ingredients": "Chicken, rice, corn...",
  "additives": "Vitamin A, D3, E...",
  "imageUrl": "https://...",
  "approved": true,
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedBy": "admin-user-id"
}
```

**Indexes:**
- `ean` (unique) - Barcode lookup
- `brand,product` (fulltext) - Search functionality
- `approved` (key) - Filter approved products

### 3. **foodIntakes** Collection

Daily food consumption records.

```javascript
{
  "$id": "unique-intake-id",
  "dogId": "dog-id",
  "userId": "user-id",
  "foodId": "food-id",
  "amount": 150.0,
  "date": "2024-01-25",
  "time": "08:30",
  "mealType": "breakfast",
  "notes": "Ate everything",
  "createdAt": "2024-01-25T08:30:00Z"
}
```

**Indexes:**
- `dogId,date` (compound) - Daily intake queries
- `userId` (key) - User's all intakes
- `createdAt` (key, desc) - Recent intakes

### 4. **weights** Collection

Weight tracking history.

```javascript
{
  "$id": "unique-weight-id",
  "dogId": "dog-id",
  "userId": "user-id",
  "weight": 30.2,
  "date": "2024-01-25",
  "time": "09:00",
  "notes": "After breakfast",
  "createdAt": "2024-01-25T09:00:00Z"
}
```

**Indexes:**
- `dogId,date` (compound, desc) - Weight history
- `createdAt` (key, desc) - Latest weights

### 5. **dogMedications** Collection

Medication tracking.

```javascript
{
  "$id": "unique-medication-id",
  "dogId": "dog-id",
  "userId": "user-id",
  "name": "Heartworm Prevention",
  "dosage": "1 tablet",
  "frequency": "monthly",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "lastGiven": "2024-01-15",
  "nextDue": "2024-02-15",
  "reminders": true,
  "notes": "Give with food",
  "active": true,
  "createdAt": "2024-01-01T10:00:00Z"
}
```

### 6. **dogAllergies** Collection

Allergy information.

```javascript
{
  "$id": "unique-allergy-id",
  "dogId": "dog-id",
  "userId": "user-id",
  "allergen": "Chicken",
  "severity": "moderate",
  "symptoms": "Skin irritation, digestive issues",
  "diagnosedDate": "2023-06-15",
  "diagnosedBy": "Dr. Smith Veterinary Clinic",
  "avoidIngredients": ["chicken", "poultry", "chicken meal"],
  "notes": "Switch to lamb-based food",
  "createdAt": "2023-06-15T14:00:00Z"
}
```

### 7. **teams** Collection

Multi-user collaboration.

```javascript
{
  "$id": "unique-team-id",
  "name": "Smith Family",
  "description": "Family members caring for Max",
  "ownerId": "team-owner-id",
  "members": [
    {
      "userId": "member-1-id",
      "role": "admin",
      "joinedAt": "2024-01-15T10:00:00Z"
    },
    {
      "userId": "member-2-id",
      "role": "member",
      "joinedAt": "2024-01-16T12:00:00Z"
    }
  ],
  "dogIds": ["dog-1-id", "dog-2-id"],
  "settings": {
    "allowMemberInvites": true,
    "requireApproval": false
  },
  "createdAt": "2024-01-15T10:00:00Z"
}
```

### 8. **communityPosts** Collection

Community forum posts.

```javascript
{
  "$id": "unique-post-id",
  "userId": "author-id",
  "title": "Best food for senior dogs?",
  "content": "Looking for recommendations...",
  "category": "nutrition",
  "tags": ["senior", "food", "advice"],
  "imageUrls": ["https://..."],
  "likes": 15,
  "likedBy": ["user-1", "user-2"],
  "commentCount": 8,
  "views": 125,
  "featured": false,
  "status": "published",
  "createdAt": "2024-01-20T16:00:00Z",
  "updatedAt": "2024-01-21T10:00:00Z"
}
```

### 9. **foodSubmissions** Collection

User-submitted food products pending approval.

```javascript
{
  "$id": "unique-submission-id",
  "userId": "submitter-id",
  "brand": "New Brand",
  "product": "Premium Dog Food",
  "ean": "1234567890123",
  "nutritionData": {
    "protein": 26.0,
    "fat": 15.0,
    // ... other nutrients
  },
  "imageUrl": "https://...",
  "status": "pending", // pending, approved, rejected
  "reviewedBy": null,
  "reviewNotes": null,
  "submittedAt": "2024-01-22T14:00:00Z",
  "reviewedAt": null,
  "source": "manual" // manual, crawler
}
```

### 10. **aiRecommendations** Collection

AI-generated recommendations.

```javascript
{
  "$id": "unique-recommendation-id",
  "dogId": "dog-id",
  "userId": "user-id",
  "type": "feeding",
  "recommendation": "Reduce daily calories by 10%",
  "reasoning": "Weight trend shows 5% increase over target",
  "confidence": 0.85,
  "priority": "medium",
  "actionItems": [
    "Reduce portion size to 140g per meal",
    "Increase exercise by 15 minutes daily"
  ],
  "validUntil": "2024-02-01T00:00:00Z",
  "acknowledged": false,
  "createdAt": "2024-01-25T10:00:00Z"
}
```

## Authentication Setup

### Email/Password Authentication

1. **Enable in Console**
   - Navigate to Authentication → Settings
   - Enable Email/Password provider
   - Configure password requirements

2. **Email Templates**
   - Customize verification emails
   - Set up password reset templates
   - Configure welcome emails

### Google OAuth Configuration

1. **Google Cloud Setup**
   ```
   1. Create project in Google Cloud Console
   2. Enable Google+ API
   3. Create OAuth 2.0 credentials
   4. Add redirect URI: appwrite-callback-snackrack2://
   ```

2. **Appwrite Configuration**
   ```
   1. Add Google provider in Authentication
   2. Enter Client ID and Secret
   3. Configure scopes: email, profile
   ```

3. **Android Manifest**
   ```xml
   <activity 
       android:name="io.appwrite.views.CallbackActivity" 
       android:exported="true">
       <intent-filter android:label="android_web_auth">
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />
           <data android:scheme="appwrite-callback-snackrack2" />
       </intent-filter>
   </activity>
   ```

## Storage Configuration

### Buckets Setup

1. **Profile Images Bucket**
   ```javascript
   {
     bucketId: "profile-images",
     name: "Profile Images",
     permissions: ["read(any)", "write(users)"],
     maximumFileSize: 5242880, // 5MB
     allowedFileExtensions: ["jpg", "jpeg", "png", "webp"],
     compression: "gzip",
     encryption: true,
     antivirus: true
   }
   ```

2. **Food Images Bucket**
   ```javascript
   {
     bucketId: "food-images",
     name: "Food Product Images",
     permissions: ["read(any)", "write(users)"],
     maximumFileSize: 10485760, // 10MB
     allowedFileExtensions: ["jpg", "jpeg", "png", "webp"],
     compression: "gzip",
     encryption: true,
     antivirus: true
   }
   ```

3. **Community Images Bucket**
   ```javascript
   {
     bucketId: "community-images",
     name: "Community Post Images",
     permissions: ["read(any)", "write(users)"],
     maximumFileSize: 20971520, // 20MB
     allowedFileExtensions: ["jpg", "jpeg", "png", "webp", "gif"],
     compression: "gzip",
     encryption: true,
     antivirus: true
   }
   ```

## Cloud Functions

### Dog Food Crawler Function

Automated crawler for food product data.

**Configuration:**
```javascript
{
  functionId: "dog-food-crawler",
  name: "Dog Food Crawler",
  runtime: "node-16.0",
  entrypoint: "src/index.js",
  schedule: "0 2 * * *", // Daily at 2 AM
  timeout: 900, // 15 minutes
  variables: {
    "DATABASE_ID": "snacktrack-db",
    "SUBMISSIONS_COLLECTION_ID": "foodSubmissions",
    "CRAWL_STATE_COLLECTION_ID": "crawlState",
    "MAX_PRODUCTS_PER_RUN": "500"
  }
}
```

**Deployment:**
```bash
cd functions/dog-food-crawler
zip -r ../../dog-food-crawler.zip .
# Upload via console or CLI
```

### Notification Scheduler Function

Handles medication reminders and notifications.

**Configuration:**
```javascript
{
  functionId: "notification-scheduler",
  name: "Notification Scheduler",
  runtime: "node-16.0",
  schedule: "0 */6 * * *", // Every 6 hours
  timeout: 300, // 5 minutes
}
```

## Security & Permissions

### Collection Permissions

1. **Personal Data (dogs, weights, etc.)**
   ```
   Read: User who created the document
   Write: User who created the document
   ```

2. **Shared Data (teams)**
   ```
   Read: Team members
   Write: Team admins and owner
   ```

3. **Public Data (foods, community posts)**
   ```
   Read: Any user
   Write: Authenticated users (with moderation)
   ```

### API Security

1. **Rate Limiting**
   - 100 requests per minute per IP
   - 1000 requests per hour per user

2. **Input Validation**
   - All inputs sanitized
   - File type restrictions
   - Size limitations

3. **Data Encryption**
   - TLS for all connections
   - Encrypted at rest
   - Secure session management

## API Endpoints

### Base URL
```
https://parse.nordburglarp.de/v2
```

### Authentication Endpoints
```
POST   /account/sessions/email     - Email login
POST   /account                    - Create account
DELETE /account/sessions/current   - Logout
GET    /account                    - Get current user
```

### Database Endpoints
```
GET    /databases/{databaseId}/collections/{collectionId}/documents
POST   /databases/{databaseId}/collections/{collectionId}/documents
PATCH  /databases/{databaseId}/collections/{collectionId}/documents/{documentId}
DELETE /databases/{databaseId}/collections/{collectionId}/documents/{documentId}
```

### Storage Endpoints
```
POST   /storage/buckets/{bucketId}/files
GET    /storage/buckets/{bucketId}/files/{fileId}/view
DELETE /storage/buckets/{bucketId}/files/{fileId}
```

## Maintenance & Monitoring

### Regular Tasks

1. **Daily**
   - Check crawler execution logs
   - Monitor error rates
   - Review new submissions

2. **Weekly**
   - Database optimization
   - Clear old sessions
   - Review security logs

3. **Monthly**
   - Backup all data
   - Update dependencies
   - Performance analysis

### Monitoring Queries

```javascript
// Check database size
appwrite databases list

// Monitor function executions
appwrite functions listExecutions --functionId=dog-food-crawler

// Review recent errors
appwrite logs list --limit=100 --levels=error

// Check storage usage
appwrite storage listBuckets
```

### Backup Strategy

1. **Automated Backups**
   - Daily database snapshots
   - Weekly full backups
   - Monthly archives

2. **Manual Backups**
   ```bash
   # Export collections
   appwrite databases listDocuments \
     --databaseId=snacktrack-db \
     --collectionId=dogs \
     --limit=1000 > dogs-backup.json
   ```

### Performance Optimization

1. **Indexes**
   - Create indexes for frequently queried fields
   - Compound indexes for complex queries
   - Monitor slow queries

2. **Caching**
   - Enable CDN for static assets
   - Cache frequently accessed data
   - Use appropriate TTL values

3. **Query Optimization**
   - Limit result sets
   - Use pagination
   - Avoid N+1 queries

---

**Version**: 1.0.0  
**Last Updated**: January 2025  
**Maintained by**: SnackTrack Development Team

For additional support, consult the [Appwrite Documentation](https://appwrite.io/docs) or contact the development team.