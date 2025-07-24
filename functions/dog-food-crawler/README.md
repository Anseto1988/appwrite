# Dog Food Crawler Function

Appwrite Cloud Function that automatically crawls dog food product data from various sources.

## Features

- **Multi-source crawling**: Currently supports Open Pet Food Facts API (Phase 1)
- **Deduplication**: Prevents duplicate products using EAN-based checking
- **State persistence**: Can resume crawling after interruption
- **Validation**: Ensures all required nutritional data is present
- **Rate limiting**: Respects server resources with configurable delays
- **Error handling**: Comprehensive error handling with retry logic

## Configuration

### Environment Variables

```env
# Appwrite Configuration
APPWRITE_ENDPOINT=https://parse.nordburglarp.de/v1
APPWRITE_FUNCTION_PROJECT_ID=snackrack2
APPWRITE_API_KEY=your-api-key

# Database Configuration
DATABASE_ID=snacktrack-db
SUBMISSIONS_COLLECTION_ID=foodSubmissions
CRAWL_STATE_COLLECTION_ID=crawlState

# Crawler Configuration
CRAWLER_USER_ID=system_crawler
MAX_PRODUCTS_PER_RUN=500
```

### Function Settings

- **Runtime**: Node.js 18.0
- **Timeout**: 900 seconds (15 minutes)
- **Schedule**: `0 2 * * *` (Daily at 2:00 AM)
- **Memory**: 512 MB (recommended)

## Data Sources

### Phase 1: Open Pet Food Facts (Implemented)
- Free and open database of pet food products
- API-based access with no authentication required
- Categories: dog-food, hundefutter, dry-dog-food, wet-dog-food

### Phase 2: German Pet Stores (Planned)
- Fressnapf.de
- Zooplus.de
- Futterhaus.de

## Database Schema

### foodSubmissions Collection
```javascript
{
    userId: "system_crawler",
    ean: "4000158101234",
    brand: "Happy Dog",
    product: "Supreme Sensible Africa",
    protein: 24.0,
    fat: 12.0,
    crudeFiber: 3.0,
    rawAsh: 6.5,
    moisture: 10.0,
    additives: "Vitamin A, Vitamin D3, ...",
    imageUrl: "https://...",
    status: "pending",
    submittedAt: "2024-01-24T02:00:00Z",
    reviewedAt: null,
    crawlSessionId: "unique-session-id",
    source: "opff"
}
```

### crawlState Collection
```javascript
{
    $id: "crawler_state_v1",
    currentSource: "opff",
    lastCrawledUrl: null,
    lastCrawledEan: "4000158101234",
    opffPage: 15,
    totalProcessed: 300,
    lastRunDate: "2024-01-24T02:55:00Z",
    statistics: {...}
}
```

## Local Development

1. Install dependencies:
```bash
cd functions/dog-food-crawler
npm install
```

2. Create `.env` file with required variables

3. Test locally:
```bash
node test-local.js
```

## Deployment

1. Install Appwrite CLI:
```bash
npm install -g appwrite
```

2. Deploy function:
```bash
appwrite functions createDeployment \
    --functionId=dog-food-crawler \
    --activate=true \
    --entrypoint="src/index.js" \
    --code="."
```

## Monitoring

The function logs detailed information including:
- Session ID for tracking
- Products processed, duplicates found, errors
- Source transitions
- Performance metrics

Check logs via Appwrite Console or CLI:
```bash
appwrite functions listExecutions --functionId=dog-food-crawler
```

## Error Handling

- **Rate limiting**: Automatic exponential backoff
- **Network errors**: Retry with delay
- **Validation errors**: Skip product and continue
- **State persistence**: Save progress for resume

## Future Enhancements

1. **Additional parsers**: Fressnapf, Zooplus scrapers
2. **Image processing**: Download and store product images
3. **Price tracking**: Monitor price changes over time
4. **Nutritional analysis**: Calculate missing values
5. **Duplicate detection**: Fuzzy matching for similar products
6. **Admin interface**: Review and approve submissions

## License

Part of SnackTrack application. All rights reserved.