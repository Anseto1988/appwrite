# SnackTrack Appwrite Collections Documentation

## Overview
This document describes all the Appwrite collections used by the SnackTrack application. Each collection includes its schema, indexes, and relationships with other collections.

## Database Configuration
- **Database ID**: `snacktrack-db` (configurable via `APPWRITE_DATABASE_ID`)
- **Endpoint**: `https://parse.nordburglarp.de/v2`
- **Project ID**: Configured via `APPWRITE_PROJECT_ID` environment variable

## Collections

### 1. Dogs Collection (`dogs`)
Stores information about users' dogs.

**Attributes:**
- `userId` (string, required): Owner's user ID
- `name` (string, required): Dog's name
- `breed` (string, optional): Dog breed
- `dateOfBirth` (datetime, optional): Birth date
- `weight` (double, optional): Current weight in kg (0-200)
- `targetWeight` (double, optional): Target weight in kg (0-200)
- `dailyCalories` (integer, optional): Daily calorie target (0-5000)
- `activityLevel` (string, optional): Activity level (low/medium/high)
- `imageUrl` (string, optional): Profile image URL
- `notes` (string, optional): Additional notes
- `isActive` (boolean, required): Active status

**Indexes:**
- `userId`: For filtering by owner
- `isActive`: For filtering active dogs
- `userActive`: Composite index for user's active dogs

### 2. Foods Collection (`foods`)
Central food database with nutritional information.

**Attributes:**
- `name` (string, required): Food name
- `brand` (string, optional): Brand name
- `barcode` (string, optional): Product barcode
- `calories` (double, required): Calories per 100g (0-1000)
- `protein` (double, optional): Protein percentage (0-100)
- `fat` (double, optional): Fat percentage (0-100)
- `carbohydrates` (double, optional): Carbs percentage (0-100)
- `fiber` (double, optional): Fiber percentage (0-100)
- `moisture` (double, optional): Moisture percentage (0-100)
- `category` (string, optional): Food category
- `imageUrl` (string, optional): Product image
- `isVerified` (boolean, required): Verification status
- `createdBy` (string, optional): Creator user ID

**Indexes:**
- `barcode` (unique): For barcode lookups
- `name` (fulltext): For text search
- `brand`: For filtering by brand
- `isVerified`: For filtering verified foods

### 3. Feedings Collection (`feedings`)
Records of food given to dogs.

**Attributes:**
- `dogId` (string, required): Dog ID
- `userId` (string, required): User who recorded feeding
- `foodId` (string, required): Food ID from foods collection
- `amount` (double, required): Amount fed (0-5000)
- `unit` (string, required): Unit (g/kg/ml/cup)
- `timestamp` (datetime, required): Feeding time
- `mealType` (string, optional): Meal type (breakfast/lunch/dinner/snack)
- `notes` (string, optional): Additional notes
- `calories` (double, optional): Calculated calories

**Indexes:**
- `dogId`: For dog's feeding history
- `userId`: For user's feeding records
- `timestamp`: For chronological ordering
- `dogTimestamp`: Composite for dog's chronological feedings

### 4. Weight Entries Collection (`weightEntries`)
Weight tracking for dogs.

**Attributes:**
- `dogId` (string, required): Dog ID
- `userId` (string, required): User who recorded weight
- `weight` (double, required): Weight in kg (0-200)
- `date` (datetime, required): Measurement date
- `notes` (string, optional): Additional notes

**Indexes:**
- `dogId`: For dog's weight history
- `date`: For chronological ordering
- `dogDate`: Composite for dog's chronological weights

### 5. Communities Collection (`communities`)
User communities for sharing and discussion.

**Attributes:**
- `name` (string, required): Community name
- `description` (string, optional): Community description
- `creatorId` (string, required): Creator user ID
- `isPublic` (boolean, required): Public visibility
- `memberCount` (integer, required): Number of members
- `tags` (string array, optional): Community tags
- `imageUrl` (string, optional): Community image
- `createdAt` (datetime, required): Creation date

**Indexes:**
- `creatorId`: For creator's communities
- `isPublic`: For filtering public communities
- `name` (fulltext): For text search

### 6. Teams Collection (`teams`)
Teams for shared dog management.

**Attributes:**
- `name` (string, required): Team name
- `description` (string, optional): Team description
- `ownerId` (string, required): Team owner user ID
- `memberCount` (integer, required): Number of members
- `createdAt` (datetime, required): Creation date

**Indexes:**
- `ownerId`: For owner's teams

### 7. Team Members Collection (`teamMembers`)
Team membership relationships.

**Attributes:**
- `teamId` (string, required): Team ID
- `userId` (string, required): Member user ID
- `role` (string, required): Member role (owner/admin/member)
- `joinedAt` (datetime, required): Join date

**Indexes:**
- `teamId`: For team's members
- `userId`: For user's teams
- `teamUser` (unique): Prevents duplicate memberships

### 8. Barcodes Collection (`barcodes`)
Barcode to food mappings.

**Attributes:**
- `barcode` (string, required): Product barcode
- `foodId` (string, required): Linked food ID
- `source` (string, optional): Data source
- `verifiedAt` (datetime, optional): Verification date
- `createdBy` (string, optional): Creator user ID

**Indexes:**
- `barcode` (unique): For barcode lookups
- `foodId`: For food's barcodes

### 9. Health Records Collection (`healthRecords`)
General health records for dogs.

**Attributes:**
- `dogId` (string, required): Dog ID
- `userId` (string, required): User who created record
- `type` (string, required): Record type (checkup/illness/surgery/other)
- `title` (string, required): Record title
- `description` (string, optional): Detailed description
- `date` (datetime, required): Record date
- `veterinarian` (string, optional): Vet name
- `cost` (double, optional): Cost amount
- `attachments` (string array, optional): File attachment IDs

**Indexes:**
- `dogId`: For dog's health history
- `type`: For filtering by record type
- `date`: For chronological ordering

### 10. Vaccinations Collection (`vaccinations`)
Vaccination records for dogs.

**Attributes:**
- `dogId` (string, required): Dog ID
- `userId` (string, required): User who created record
- `vaccine` (string, required): Vaccine name
- `date` (datetime, required): Vaccination date
- `nextDue` (datetime, optional): Next due date
- `veterinarian` (string, optional): Vet name
- `batchNumber` (string, optional): Vaccine batch number
- `notes` (string, optional): Additional notes

**Indexes:**
- `dogId`: For dog's vaccination history
- `nextDue`: For upcoming vaccinations

### 11. Medications Collection (`medications`)
Medication tracking for dogs.

**Attributes:**
- `dogId` (string, required): Dog ID
- `userId` (string, required): User who created record
- `name` (string, required): Medication name
- `dosage` (string, required): Dosage information
- `frequency` (string, required): Frequency (e.g., "2x daily")
- `startDate` (datetime, required): Start date
- `endDate` (datetime, optional): End date
- `reason` (string, optional): Reason for medication
- `prescribedBy` (string, optional): Prescribing vet
- `isActive` (boolean, required): Active status

**Indexes:**
- `dogId`: For dog's medications
- `isActive`: For filtering active medications

### 12. Notifications Collection (`notifications`)
User notifications.

**Attributes:**
- `userId` (string, required): Recipient user ID
- `type` (string, required): Notification type
- `title` (string, required): Notification title
- `message` (string, required): Notification message
- `data` (string, optional): Additional JSON data
- `isRead` (boolean, required): Read status
- `createdAt` (datetime, required): Creation date

**Indexes:**
- `userId`: For user's notifications
- `isRead`: For filtering unread notifications
- `userUnread`: Composite for user's unread notifications

## Storage Buckets

### 1. Dog Images (`dog_images`)
- **Purpose**: Profile images for dogs
- **File types**: JPEG, PNG, WebP
- **Max size**: 5MB per image

### 2. Food Images (`food_images`)
- **Purpose**: Product images for foods
- **File types**: JPEG, PNG, WebP
- **Max size**: 5MB per image

### 3. User Avatars (`avatars`)
- **Purpose**: User profile pictures
- **File types**: JPEG, PNG, WebP
- **Max size**: 2MB per image

## Permissions

### Default Collection Permissions:
- **Read**: Any user
- **Create**: Authenticated users
- **Update**: Authenticated users (own records only)
- **Delete**: Authenticated users (own records only)

### Special Permissions:
- **Foods Collection**: 
  - Create: Authenticated users
  - Update: Only verified contributors or admins
  - Delete: Only admins
  
- **Communities Collection**:
  - Create: Authenticated users
  - Update: Community owner/admins
  - Delete: Community owner only

## Relationships

### Primary Relationships:
1. **Dogs → Users**: Many dogs belong to one user (via `userId`)
2. **Feedings → Dogs**: Many feedings belong to one dog (via `dogId`)
3. **Feedings → Foods**: Each feeding references one food (via `foodId`)
4. **Weight Entries → Dogs**: Many weights belong to one dog (via `dogId`)
5. **Team Members → Teams**: Many members belong to one team (via `teamId`)
6. **Team Members → Users**: Each member is one user (via `userId`)

### Secondary Relationships:
1. **Health Records → Dogs**: Many records per dog
2. **Vaccinations → Dogs**: Many vaccinations per dog
3. **Medications → Dogs**: Many medications per dog
4. **Notifications → Users**: Many notifications per user
5. **Barcodes → Foods**: One-to-one barcode to food mapping

## Data Integrity Rules

1. **Cascading Deletes**: When a dog is deleted, all related records (feedings, weights, health records) should be deleted
2. **User Deletion**: When a user is deleted, their dogs and all related data should be handled (deleted or transferred)
3. **Food Deletion**: Foods with existing feeding records cannot be deleted, only marked as inactive
4. **Team Deletion**: Teams can only be deleted by the owner when no other members exist

## Performance Considerations

1. **Indexing Strategy**: Indexes are created on frequently queried fields
2. **Pagination**: All list queries should use pagination (limit/offset)
3. **Caching**: Frequently accessed data (foods, breeds) should be cached client-side
4. **Batch Operations**: Use batch operations for bulk imports/updates

## Migration Notes

1. **Version Control**: Track schema versions in a migrations collection
2. **Backward Compatibility**: New fields should be optional to maintain compatibility
3. **Data Migration**: Use Appwrite functions for complex data migrations
4. **Testing**: Always test migrations in a development environment first