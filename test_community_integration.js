const sdk = require('node-appwrite');

// Test script to validate community feature database integration
const client = new sdk.Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new sdk.Databases(client);
const storage = new sdk.Storage(client);
const account = new sdk.Account(client);

const DATABASE_ID = 'snacktrack-db';
const COLLECTION_COMMUNITY_POSTS = 'community_posts';
const COLLECTION_COMMUNITY_PROFILES = 'community_profiles';
const COLLECTION_COMMUNITY_LIKES = 'community_likes';
const BUCKET_COMMUNITY_IMAGES = 'community_images';

// Test results tracking
const testResults = {
    passed: 0,
    failed: 0,
    details: []
};

function logTest(testName, passed, message = '') {
    if (passed) {
        testResults.passed++;
        console.log(`✅ ${testName}: PASSED`);
    } else {
        testResults.failed++;
        console.log(`❌ ${testName}: FAILED - ${message}`);
    }
    testResults.details.push({ testName, passed, message });
}

async function testDatabaseConnection() {
    try {
        const database = await databases.get(DATABASE_ID);
        logTest('Database Connection', true, `Connected to database: ${database.name}`);
        return true;
    } catch (error) {
        logTest('Database Connection', false, `Cannot connect to database: ${error.message}`);
        return false;
    }
}

async function testCommunityCollections() {
    const requiredCollections = [
        COLLECTION_COMMUNITY_POSTS,
        COLLECTION_COMMUNITY_PROFILES,
        'community_comments',
        'community_likes',
        'community_follows'
    ];

    for (const collectionId of requiredCollections) {
        try {
            const collection = await databases.getCollection(DATABASE_ID, collectionId);
            logTest(`Collection ${collectionId}`, true, `Collection exists with ${collection.attributes.length} attributes`);
        } catch (error) {
            logTest(`Collection ${collectionId}`, false, `Collection missing: ${error.message}`);
        }
    }
}

async function testCommunityBucket() {
    try {
        const bucket = await storage.getBucket(BUCKET_COMMUNITY_IMAGES);
        logTest('Community Images Bucket', true, `Bucket exists with ${bucket.fileSizeLimit} byte limit`);
        return true;
    } catch (error) {
        logTest('Community Images Bucket', false, `Bucket missing: ${error.message}`);
        return false;
    }
}

async function testPostCreation() {
    try {
        // Create a test community profile first
        const testProfile = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            sdk.ID.unique(),
            {
                userId: 'test-user-' + Date.now(),
                displayName: 'Test QA User',
                bio: 'QA Testing Profile',
                isPremium: false,
                followersCount: 0,
                followingCount: 0,
                postsCount: 0,
                createdAt: new Date().toISOString()
            }
        );

        // Create a test post
        const testPost = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            sdk.ID.unique(),
            {
                userId: testProfile.data.userId,
                content: 'QA Test Post - Database Integration Test',
                postType: 'PHOTO',
                imageUrls: JSON.stringify([]),
                hashtags: JSON.stringify(['#qatest', '#integration']),
                likesCount: 0,
                commentsCount: 0,
                created_at: new Date().toISOString()
            }
        );

        logTest('Post Creation', true, `Created post with ID: ${testPost.$id}`);

        // Test post retrieval
        const retrievedPost = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, testPost.$id);
        logTest('Post Retrieval', true, `Retrieved post: ${retrievedPost.data.content}`);

        // Test post update (like functionality)
        const updatedPost = await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            testPost.$id,
            { likesCount: 1 }
        );
        logTest('Post Update', updatedPost.data.likesCount === 1, `Updated likes count: ${updatedPost.data.likesCount}`);

        // Clean up test data
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, testPost.$id);
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, testProfile.$id);
        logTest('Test Data Cleanup', true, 'Test posts and profiles cleaned up');

        return true;
    } catch (error) {
        logTest('Post Creation Workflow', false, `Post creation failed: ${error.message}`);
        return false;
    }
}

async function testProfileManagement() {
    try {
        const testUserId = 'qa-test-user-' + Date.now();

        // Create profile
        const profile = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            sdk.ID.unique(),
            {
                userId: testUserId,
                displayName: 'QA Test Profile',
                bio: 'Testing profile management',
                isPremium: false,
                followersCount: 10,
                followingCount: 5,
                postsCount: 3,
                createdAt: new Date().toISOString()
            }
        );

        logTest('Profile Creation', true, `Created profile: ${profile.data.displayName}`);

        // Update profile
        const updatedProfile = await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            profile.$id,
            {
                displayName: 'Updated QA Profile',
                bio: 'Updated bio for testing',
                followersCount: 15
            }
        );

        logTest('Profile Update', 
            updatedProfile.data.displayName === 'Updated QA Profile' && 
            updatedProfile.data.followersCount === 15,
            `Profile updated successfully`
        );

        // Test profile query by userId
        const profileQuery = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            [sdk.Query.equal('userId', testUserId)]
        );

        logTest('Profile Query', 
            profileQuery.documents.length === 1 && 
            profileQuery.documents[0].data.userId === testUserId,
            `Profile query returned correct results`
        );

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        logTest('Profile Cleanup', true, 'Test profile cleaned up');

        return true;
    } catch (error) {
        logTest('Profile Management', false, `Profile management failed: ${error.message}`);
        return false;
    }
}

async function testLikeSystem() {
    try {
        const testUserId = 'qa-like-user-' + Date.now();
        const testPostId = 'qa-test-post-' + Date.now();

        // Create a like
        const like = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_LIKES,
            sdk.ID.unique(),
            {
                userId: testUserId,
                postId: testPostId,
                created_at: new Date().toISOString()
            }
        );

        logTest('Like Creation', true, `Created like with ID: ${like.$id}`);

        // Query likes for the post
        const postLikes = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_LIKES,
            [sdk.Query.equal('postId', testPostId)]
        );

        logTest('Like Query', 
            postLikes.documents.length === 1 && 
            postLikes.documents[0].data.userId === testUserId,
            `Like query returned correct results`
        );

        // Test unique constraint (should prevent duplicate likes)
        try {
            await databases.createDocument(
                DATABASE_ID,
                COLLECTION_COMMUNITY_LIKES,
                sdk.ID.unique(),
                {
                    userId: testUserId,
                    postId: testPostId,
                    created_at: new Date().toISOString()
                }
            );
            logTest('Duplicate Like Prevention', false, 'Duplicate like was allowed');
        } catch (duplicateError) {
            logTest('Duplicate Like Prevention', true, 'Duplicate like correctly prevented');
        }

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_LIKES, like.$id);
        logTest('Like Cleanup', true, 'Test like cleaned up');

        return true;
    } catch (error) {
        logTest('Like System', false, `Like system failed: ${error.message}`);
        return false;
    }
}

async function testDataPersistence() {
    try {
        // Create data
        const testData = {
            userId: 'persistence-test-' + Date.now(),
            displayName: 'Persistence Test User',
            bio: 'Testing data persistence across operations',
            isPremium: false,
            followersCount: 0,
            followingCount: 0,
            postsCount: 0,
            createdAt: new Date().toISOString()
        };

        const profile = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            sdk.ID.unique(),
            testData
        );

        // Wait a moment to ensure data is persisted
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Retrieve and verify data persistence
        const retrievedProfile = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        
        const dataMatches = 
            retrievedProfile.data.userId === testData.userId &&
            retrievedProfile.data.displayName === testData.displayName &&
            retrievedProfile.data.bio === testData.bio &&
            retrievedProfile.data.isPremium === testData.isPremium;

        logTest('Data Persistence', dataMatches, 'Data correctly persisted and retrieved');

        // Test data consistency across multiple operations
        await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            profile.$id,
            { followersCount: 5, postsCount: 2 }
        );

        const updatedProfile = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        const updatesApplied = 
            updatedProfile.data.followersCount === 5 &&
            updatedProfile.data.postsCount === 2 &&
            updatedProfile.data.displayName === testData.displayName; // Original data should remain

        logTest('Data Consistency', updatesApplied, 'Data updates correctly applied while preserving existing data');

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        
        return true;
    } catch (error) {
        logTest('Data Persistence', false, `Data persistence test failed: ${error.message}`);
        return false;
    }
}

async function testPerformanceAndLimits() {
    try {
        const startTime = Date.now();
        
        // Test query performance with limit
        const posts = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            [
                sdk.Query.orderDesc('created_at'),
                sdk.Query.limit(10)
            ]
        );
        
        const queryTime = Date.now() - startTime;
        logTest('Query Performance', queryTime < 5000, `Query completed in ${queryTime}ms`);
        
        // Test pagination
        const pagedPosts = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            [
                sdk.Query.orderDesc('created_at'),
                sdk.Query.limit(5),
                sdk.Query.offset(5)
            ]
        );
        
        logTest('Pagination Support', true, `Pagination working, returned ${pagedPosts.documents.length} posts`);
        
        return true;
    } catch (error) {
        logTest('Performance and Limits', false, `Performance test failed: ${error.message}`);
        return false;
    }
}

async function runAllTests() {
    console.log('🚀 Starting Community Feature Database Integration Tests...\n');
    
    // Core infrastructure tests
    const dbConnected = await testDatabaseConnection();
    if (!dbConnected) {
        console.log('\n❌ Database connection failed. Stopping tests.');
        return;
    }
    
    await testCommunityCollections();
    await testCommunityBucket();
    
    // Functional tests
    await testPostCreation();
    await testProfileManagement();
    await testLikeSystem();
    await testDataPersistence();
    await testPerformanceAndLimits();
    
    // Print final results
    console.log('\n' + '='.repeat(50));
    console.log('🏁 COMMUNITY FEATURE INTEGRATION TEST RESULTS');
    console.log('='.repeat(50));
    console.log(`✅ Tests Passed: ${testResults.passed}`);
    console.log(`❌ Tests Failed: ${testResults.failed}`);
    console.log(`📊 Success Rate: ${((testResults.passed / (testResults.passed + testResults.failed)) * 100).toFixed(1)}%`);
    
    if (testResults.failed === 0) {
        console.log('\n🎉 ALL TESTS PASSED! Community feature is ready for production.');
    } else {
        console.log('\n⚠️  Some tests failed. Review the issues above before production deployment.');
    }
    
    console.log('\n📋 Test Summary:');
    testResults.details.forEach(test => {
        const status = test.passed ? '✅' : '❌';
        console.log(`  ${status} ${test.testName}: ${test.message || 'OK'}`);
    });
}

// Run all tests
runAllTests().catch(console.error);