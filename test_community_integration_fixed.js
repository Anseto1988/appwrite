const sdk = require('node-appwrite');

// Fixed test script to validate community feature database integration
const client = new sdk.Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new sdk.Databases(client);
const storage = new sdk.Storage(client);

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

async function testFixedPostCreation() {
    try {
        // Create a test community profile first with correct attribute name
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
                createdAt: new Date().toISOString() // Fixed: using createdAt not created_at
            }
        );

        // Create a test post with correct attribute name
        const testPost = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            sdk.ID.unique(),
            {
                userId: testProfile.$data.userId,
                content: 'QA Test Post - Database Integration Test',
                postType: 'PHOTO',
                imageUrls: JSON.stringify([]),
                hashtags: JSON.stringify(['#qatest', '#integration']),
                likesCount: 0,
                commentsCount: 0,
                created_at: new Date().toISOString() // This matches the setup script
            }
        );

        logTest('Fixed Post Creation', true, `Created post with ID: ${testPost.$id}`);

        // Test post retrieval
        const retrievedPost = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, testPost.$id);
        logTest('Fixed Post Retrieval', true, `Retrieved post: ${retrievedPost.$data.content}`);

        // Test post update (like functionality)
        const updatedPost = await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            testPost.$id,
            { likesCount: 1 }
        );
        logTest('Fixed Post Update', updatedPost.$data.likesCount === 1, `Updated likes count: ${updatedPost.$data.likesCount}`);

        // Clean up test data
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, testPost.$id);
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, testProfile.$id);
        logTest('Fixed Test Data Cleanup', true, 'Test posts and profiles cleaned up');

        return true;
    } catch (error) {
        logTest('Fixed Post Creation Workflow', false, `Post creation failed: ${error.message}`);
        return false;
    }
}

async function testFixedProfileManagement() {
    try {
        const testUserId = 'qa-test-user-' + Date.now();

        // Create profile with correct attribute name
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
                createdAt: new Date().toISOString() // Fixed: using createdAt
            }
        );

        logTest('Fixed Profile Creation', true, `Created profile: ${profile.$data.displayName}`);

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

        logTest('Fixed Profile Update', 
            updatedProfile.$data.displayName === 'Updated QA Profile' && 
            updatedProfile.$data.followersCount === 15,
            `Profile updated successfully`
        );

        // Test profile query by userId
        const profileQuery = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            [sdk.Query.equal('userId', testUserId)]
        );

        logTest('Fixed Profile Query', 
            profileQuery.documents.length === 1 && 
            profileQuery.documents[0].$data.userId === testUserId,
            `Profile query returned correct results`
        );

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        logTest('Fixed Profile Cleanup', true, 'Test profile cleaned up');

        return true;
    } catch (error) {
        logTest('Fixed Profile Management', false, `Profile management failed: ${error.message}`);
        return false;
    }
}

async function testFixedLikeSystem() {
    try {
        const testUserId = 'qa-like-user-' + Date.now();
        const testPostId = 'qa-test-post-' + Date.now();

        // Create a like with correct attribute name
        const like = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_LIKES,
            sdk.ID.unique(),
            {
                userId: testUserId,
                postId: testPostId,
                created_at: new Date().toISOString() // Fixed: using created_at
            }
        );

        logTest('Fixed Like Creation', true, `Created like with ID: ${like.$id}`);

        // Query likes for the post
        const postLikes = await databases.listDocuments(
            DATABASE_ID,
            COLLECTION_COMMUNITY_LIKES,
            [sdk.Query.equal('postId', testPostId)]
        );

        logTest('Fixed Like Query', 
            postLikes.documents.length === 1 && 
            postLikes.documents[0].$data.userId === testUserId,
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
            logTest('Fixed Duplicate Like Prevention', false, 'Duplicate like was allowed');
        } catch (duplicateError) {
            logTest('Fixed Duplicate Like Prevention', true, 'Duplicate like correctly prevented');
        }

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_LIKES, like.$id);
        logTest('Fixed Like Cleanup', true, 'Test like cleaned up');

        return true;
    } catch (error) {
        logTest('Fixed Like System', false, `Like system failed: ${error.message}`);
        return false;
    }
}

async function testFixedDataPersistence() {
    try {
        // Create data with correct attribute names
        const testData = {
            userId: 'persistence-test-' + Date.now(),
            displayName: 'Persistence Test User',
            bio: 'Testing data persistence across operations',
            isPremium: false,
            followersCount: 0,
            followingCount: 0,
            postsCount: 0,
            createdAt: new Date().toISOString() // Fixed: using createdAt
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
            retrievedProfile.$data.userId === testData.userId &&
            retrievedProfile.$data.displayName === testData.displayName &&
            retrievedProfile.$data.bio === testData.bio &&
            retrievedProfile.$data.isPremium === testData.isPremium;

        logTest('Fixed Data Persistence', dataMatches, 'Data correctly persisted and retrieved');

        // Test data consistency across multiple operations
        await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            profile.$id,
            { followersCount: 5, postsCount: 2 }
        );

        const updatedProfile = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        const updatesApplied = 
            updatedProfile.$data.followersCount === 5 &&
            updatedProfile.$data.postsCount === 2 &&
            updatedProfile.$data.displayName === testData.displayName; // Original data should remain

        logTest('Fixed Data Consistency', updatesApplied, 'Data updates correctly applied while preserving existing data');

        // Clean up
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        
        return true;
    } catch (error) {
        logTest('Fixed Data Persistence', false, `Data persistence test failed: ${error.message}`);
        return false;
    }
}

async function testCompleteWorkflow() {
    try {
        console.log('\n🔄 Testing Complete Community Workflow...');
        
        // Step 1: Create user profile
        const userId = 'workflow-user-' + Date.now();
        const profile = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            sdk.ID.unique(),
            {
                userId: userId,
                displayName: 'Workflow Test User',
                bio: 'Complete workflow test',
                isPremium: false,
                followersCount: 0,
                followingCount: 0,
                postsCount: 0,
                createdAt: new Date().toISOString()
            }
        );
        
        // Step 2: Create a post
        const post = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            sdk.ID.unique(),
            {
                userId: userId,
                content: 'Complete workflow test post',
                postType: 'PHOTO',
                imageUrls: JSON.stringify([]),
                hashtags: JSON.stringify(['#workflow', '#test']),
                likesCount: 0,
                commentsCount: 0,
                created_at: new Date().toISOString()
            }
        );
        
        // Step 3: Update profile post count
        await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_PROFILES,
            profile.$id,
            { postsCount: 1 }
        );
        
        // Step 4: Like the post
        const like = await databases.createDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_LIKES,
            sdk.ID.unique(),
            {
                userId: 'another-user-' + Date.now(),
                postId: post.$id,
                created_at: new Date().toISOString()
            }
        );
        
        // Step 5: Update post like count
        await databases.updateDocument(
            DATABASE_ID,
            COLLECTION_COMMUNITY_POSTS,
            post.$id,
            { likesCount: 1 }
        );
        
        // Step 6: Verify complete workflow
        const finalProfile = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        const finalPost = await databases.getDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, post.$id);
        
        const workflowSuccess = 
            finalProfile.$data.postsCount === 1 &&
            finalPost.$data.likesCount === 1 &&
            finalPost.$data.content === 'Complete workflow test post';
            
        logTest('Complete Workflow', workflowSuccess, 'Full user workflow completed successfully');
        
        // Cleanup
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_LIKES, like.$id);
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_POSTS, post.$id);
        await databases.deleteDocument(DATABASE_ID, COLLECTION_COMMUNITY_PROFILES, profile.$id);
        
        return true;
    } catch (error) {
        logTest('Complete Workflow', false, `Complete workflow failed: ${error.message}`);
        return false;
    }
}

async function runFixedTests() {
    console.log('🚀 Starting FIXED Community Feature Database Integration Tests...\n');
    
    // Run the fixed tests
    await testFixedPostCreation();
    await testFixedProfileManagement();
    await testFixedLikeSystem();
    await testFixedDataPersistence();
    await testCompleteWorkflow();
    
    // Print final results
    console.log('\n' + '='.repeat(60));
    console.log('🏁 FIXED COMMUNITY FEATURE INTEGRATION TEST RESULTS');
    console.log('='.repeat(60));
    console.log(`✅ Tests Passed: ${testResults.passed}`);
    console.log(`❌ Tests Failed: ${testResults.failed}`);
    console.log(`📊 Success Rate: ${((testResults.passed / (testResults.passed + testResults.failed)) * 100).toFixed(1)}%`);
    
    if (testResults.failed === 0) {
        console.log('\n🎉 ALL TESTS PASSED! Community feature is ready for production.');
        console.log('✅ Database connectivity: WORKING');
        console.log('✅ Post creation workflow: WORKING');
        console.log('✅ Profile management: WORKING');
        console.log('✅ Like system: WORKING');
        console.log('✅ Data persistence: WORKING');
        console.log('✅ Complete user workflow: WORKING');
    } else {
        console.log('\n⚠️  Some tests failed. Review the issues above before production deployment.');
    }
    
    console.log('\n📋 Detailed Test Results:');
    testResults.details.forEach(test => {
        const status = test.passed ? '✅' : '❌';
        console.log(`  ${status} ${test.testName}: ${test.message || 'OK'}`);
    });
}

// Run all fixed tests
runFixedTests().catch(console.error);