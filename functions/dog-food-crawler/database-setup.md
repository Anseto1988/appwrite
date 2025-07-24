# Database Setup for Dog Food Crawler

## Required Collections

### 1. Update foodSubmissions Collection

The existing `foodSubmissions` collection needs the following additional attributes:

```javascript
// New attributes to add:
{
    crawlSessionId: {
        type: 'string',
        size: 36,
        required: false
    },
    source: {
        type: 'string', 
        size: 20,
        required: false,
        default: 'manual'
    },
    sourceUrl: {
        type: 'string',
        size: 500,
        required: false
    }
}
```

### 2. Create crawlState Collection

Create a new collection for storing crawler state:

```javascript
{
    collectionId: 'crawlState',
    name: 'Crawler State',
    attributes: [
        {
            key: 'currentSource',
            type: 'string',
            size: 20,
            required: true,
            default: 'opff'
        },
        {
            key: 'lastCrawledUrl',
            type: 'string',
            size: 500,
            required: false
        },
        {
            key: 'lastCrawledEan',
            type: 'string',
            size: 13,
            required: false
        },
        {
            key: 'opffPage',
            type: 'integer',
            required: true,
            default: 1,
            min: 1,
            max: 10000
        },
        {
            key: 'fressnapfPage',
            type: 'integer',
            required: true,
            default: 1,
            min: 1,
            max: 10000
        },
        {
            key: 'zooplusPage',
            type: 'integer',
            required: true,
            default: 1,
            min: 1,
            max: 10000
        },
        {
            key: 'totalProcessed',
            type: 'integer',
            required: true,
            default: 0,
            min: 0
        },
        {
            key: 'lastRunDate',
            type: 'datetime',
            required: false
        },
        {
            key: 'lastError',
            type: 'string',
            size: 500,
            required: false
        },
        {
            key: 'lastErrorDate',
            type: 'datetime',
            required: false
        },
        {
            key: 'statistics',
            type: 'string',
            size: 5000,
            required: false
        },
        {
            key: 'updatedAt',
            type: 'datetime',
            required: true
        }
    ],
    permissions: [
        'read("any")',
        'create("users")',
        'update("users")',
        'delete("users")'
    ]
}
```

### 3. Create Indexes

For optimal performance, create these indexes:

#### foodSubmissions Collection
```javascript
// Index on crawlSessionId for grouping
{
    key: 'crawl_session_idx',
    type: 'key',
    attributes: ['crawlSessionId']
}

// Index on source for filtering
{
    key: 'source_idx',
    type: 'key',
    attributes: ['source']
}

// Compound index for efficient duplicate checking
{
    key: 'ean_status_idx',
    type: 'key',
    attributes: ['ean', 'status']
}
```

## Setup Commands

Using Appwrite CLI:

```bash
# Create crawlState collection
appwrite databases createCollection \
    --databaseId="snacktrack-db" \
    --collectionId="crawlState" \
    --name="Crawler State" \
    --permissions='read("any")' 'create("users")' 'update("users")' 'delete("users")'

# Add attributes to crawlState
appwrite databases createStringAttribute \
    --databaseId="snacktrack-db" \
    --collectionId="crawlState" \
    --key="currentSource" \
    --size=20 \
    --required=true \
    --default="opff"

# ... (add all other attributes)

# Add attributes to foodSubmissions
appwrite databases createStringAttribute \
    --databaseId="snacktrack-db" \
    --collectionId="foodSubmissions" \
    --key="crawlSessionId" \
    --size=36 \
    --required=false

appwrite databases createStringAttribute \
    --databaseId="snacktrack-db" \
    --collectionId="foodSubmissions" \
    --key="source" \
    --size=20 \
    --required=false \
    --default="manual"

# Create indexes
appwrite databases createIndex \
    --databaseId="snacktrack-db" \
    --collectionId="foodSubmissions" \
    --key="crawl_session_idx" \
    --type="key" \
    --attributes="crawlSessionId"
```

## Verification

After setup, verify:

1. Collections exist with correct attributes
2. Indexes are created and active
3. Permissions allow the crawler function to read/write
4. Test with a small crawl run

## Maintenance

- Monitor index performance
- Clean up old crawl sessions periodically
- Archive approved products to separate collection