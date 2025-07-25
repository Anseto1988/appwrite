const { Client, Databases, ID } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client()
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5');

const databases = new Databases(client);
const DATABASE_ID = 'snacktrack-db';

async function createCommunityCollections() {
    console.log('Creating community collections...');
    
    try {
        // 1. Create Forum Categories Collection
        console.log('\nCreating forum_categories collection...');
        const forumCategoriesCollection = await databases.createCollection(
            DATABASE_ID,
            'forum_categories',
            'Forum Categories',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Forum Categories attributes
        await databases.createStringAttribute(DATABASE_ID, 'forum_categories', 'name', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_categories', 'description', 500, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_categories', 'icon', 10, true);
        await databases.createIntegerAttribute(DATABASE_ID, 'forum_categories', 'postCount', true, 0, 999999, 0);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_categories', 'isModerated', true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_categories', 'allowedUserTypes', 255, false); // JSON array
        
        console.log('✓ Forum categories collection created');
        
        // 2. Create Forum Posts Collection
        console.log('\nCreating forum_posts collection...');
        const forumPostsCollection = await databases.createCollection(
            DATABASE_ID,
            'forum_posts',
            'Forum Posts',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Forum Posts attributes
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'authorId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'authorName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'authorAvatar', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'categoryId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'content', 5000, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'tags', 1000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'images', 2000, false); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'forum_posts', 'createdAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'forum_posts', 'updatedAt', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'forum_posts', 'viewCount', true, 0, 999999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'forum_posts', 'likeCount', true, 0, 999999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'forum_posts', 'replyCount', true, 0, 999999, 0);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_posts', 'isPinned', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_posts', 'isLocked', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_posts', 'isExpertVerified', true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_posts', 'breedSpecific', 1000, false); // JSON array
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'forum_posts', 'categoryPosts', 'key', ['categoryId']);
        await databases.createIndex(DATABASE_ID, 'forum_posts', 'recentPosts', 'key', ['createdAt']);
        await databases.createIndex(DATABASE_ID, 'forum_posts', 'popularPosts', 'key', ['viewCount']);
        
        console.log('✓ Forum posts collection created');
        
        // 3. Create Forum Replies Collection
        console.log('\nCreating forum_replies collection...');
        const forumRepliesCollection = await databases.createCollection(
            DATABASE_ID,
            'forum_replies',
            'Forum Replies',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Forum Replies attributes
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'postId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'parentReplyId', 36, false);
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'authorId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'authorName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'authorAvatar', 255, false);
        await databases.createStringAttribute(DATABASE_ID, 'forum_replies', 'content', 2000, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'forum_replies', 'createdAt', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'forum_replies', 'updatedAt', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'forum_replies', 'likeCount', true, 0, 999999, 0);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_replies', 'isExpertAnswer', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'forum_replies', 'isBestAnswer', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'forum_replies', 'postReplies', 'key', ['postId']);
        await databases.createIndex(DATABASE_ID, 'forum_replies', 'threadedReplies', 'key', ['parentReplyId']);
        
        console.log('✓ Forum replies collection created');
        
        // 4. Create Community Events Collection
        console.log('\nCreating community_events collection...');
        const communityEventsCollection = await databases.createCollection(
            DATABASE_ID,
            'community_events',
            'Community Events',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Community Events attributes
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'organizerId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'organizerName', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'community_events', 'eventType', 
            ['MEETUP');
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'description', 2000, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'location', 2000, true); // JSON object
        await databases.createDatetimeAttribute(DATABASE_ID, 'community_events', 'startDateTime', true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'community_events', 'endDateTime', true);
        await databases.createIntegerAttribute(DATABASE_ID, 'community_events', 'maxParticipants', false, 1, 9999);
        await databases.createIntegerAttribute(DATABASE_ID, 'community_events', 'currentParticipants', true, 0, 9999, 0);
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'registeredUserIds', 5000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'tags', 1000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'images', 2000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'community_events', 'requirements', 2000, false); // JSON object
        await databases.createBooleanAttribute(DATABASE_ID, 'community_events', 'isFree', true);
        await databases.createFloatAttribute(DATABASE_ID, 'community_events', 'price', false, 0, 9999);
        await databases.createEnumAttribute(DATABASE_ID, 'community_events', 'status', 
            ['UPCOMING');
        await databases.createDatetimeAttribute(DATABASE_ID, 'community_events', 'createdAt', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'community_events', 'upcomingEvents', 'key', ['startDateTime']);
        await databases.createIndex(DATABASE_ID, 'community_events', 'eventsByCity', 'key', ['location.city']);
        
        console.log('✓ Community events collection created');
        
        // 5. Create Expert Profiles Collection
        console.log('\nCreating expert_profiles collection...');
        const expertProfilesCollection = await databases.createCollection(
            DATABASE_ID,
            'expert_profiles',
            'Expert Profiles',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Expert Profiles attributes
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'userId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'name', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'credentials', 1000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'specializations', 1000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'bio', 2000, true);
        await databases.createIntegerAttribute(DATABASE_ID, 'expert_profiles', 'yearsOfExperience', true, 0, 99);
        await databases.createDatetimeAttribute(DATABASE_ID, 'expert_profiles', 'verifiedAt', false);
        await databases.createFloatAttribute(DATABASE_ID, 'expert_profiles', 'rating', true, 0, 5, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'expert_profiles', 'totalAnswers', true, 0, 99999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'expert_profiles', 'helpfulAnswers', true, 0, 99999, 0);
        await databases.createStringAttribute(DATABASE_ID, 'expert_profiles', 'responseTime', 100, true);
        await databases.createBooleanAttribute(DATABASE_ID, 'expert_profiles', 'isAvailable', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'expert_profiles', 'availableExperts', 'key', ['isAvailable']);
        await databases.createIndex(DATABASE_ID, 'expert_profiles', 'topRatedExperts', 'key', ['rating']);
        
        console.log('✓ Expert profiles collection created');
        
        // 6. Create Expert Questions Collection
        console.log('\nCreating expert_questions collection...');
        const expertQuestionsCollection = await databases.createCollection(
            DATABASE_ID,
            'expert_questions',
            'Expert Questions',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Expert Questions attributes
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'askedByUserId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'askedByUserName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'categoryId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'description', 3000, true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'dogBreed', 100, false);
        await databases.createIntegerAttribute(DATABASE_ID, 'expert_questions', 'dogAge', false, 0, 30);
        await databases.createEnumAttribute(DATABASE_ID, 'expert_questions', 'urgency', 
            ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'images', 2000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'tags', 1000, false); // JSON array
        await databases.createDatetimeAttribute(DATABASE_ID, 'expert_questions', 'askedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'expert_questions', 'status', 
            ['OPEN');
        await databases.createStringAttribute(DATABASE_ID, 'expert_questions', 'assignedExpertId', 36, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'expert_questions', 'answeredAt', false);
        await databases.createIntegerAttribute(DATABASE_ID, 'expert_questions', 'viewCount', true, 0, 999999, 0);
        await databases.createBooleanAttribute(DATABASE_ID, 'expert_questions', 'isPublic', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'expert_questions', 'openQuestions', 'key', ['status']);
        await databases.createIndex(DATABASE_ID, 'expert_questions', 'urgentQuestions', 'key', ['urgency']);
        
        console.log('✓ Expert questions collection created');
        
        // 7. Create User Recipes Collection
        console.log('\nCreating user_recipes collection...');
        const userRecipesCollection = await databases.createCollection(
            DATABASE_ID,
            'user_recipes',
            'User Recipes',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // User Recipes attributes
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'authorId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'authorName', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'description', 1000, true);
        await databases.createEnumAttribute(DATABASE_ID, 'user_recipes', 'category', 
            ['MAIN_MEAL');
        await databases.createIntegerAttribute(DATABASE_ID, 'user_recipes', 'prepTime', true, 0, 999);
        await databases.createIntegerAttribute(DATABASE_ID, 'user_recipes', 'cookTime', true, 0, 999);
        await databases.createIntegerAttribute(DATABASE_ID, 'user_recipes', 'servings', true, 1, 99);
        await databases.createEnumAttribute(DATABASE_ID, 'user_recipes', 'difficulty', 
            ['EASY');
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'ingredients', 3000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'instructions', 3000, true); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'nutritionInfo', 1000, false); // JSON object
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'suitableFor', 1000, false); // JSON object
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'images', 2000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'tags', 1000, false); // JSON array
        await databases.createFloatAttribute(DATABASE_ID, 'user_recipes', 'rating', true, 0, 5, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'user_recipes', 'reviewCount', true, 0, 99999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'user_recipes', 'favoriteCount', true, 0, 99999, 0);
        await databases.createDatetimeAttribute(DATABASE_ID, 'user_recipes', 'createdAt', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'user_recipes', 'isApproved', true);
        await databases.createStringAttribute(DATABASE_ID, 'user_recipes', 'approvedBy', 255, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'user_recipes', 'approvedRecipes', 'key', ['isApproved']);
        await databases.createIndex(DATABASE_ID, 'user_recipes', 'popularRecipes', 'key', ['rating']);
        await databases.createIndex(DATABASE_ID, 'user_recipes', 'recipesByCategory', 'key', ['category']);
        
        console.log('✓ User recipes collection created');
        
        // 8. Create Community Tips Collection
        console.log('\nCreating community_tips collection...');
        const communityTipsCollection = await databases.createCollection(
            DATABASE_ID,
            'community_tips',
            'Community Tips',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Community Tips attributes
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'authorId', 36, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'authorName', 255, true);
        await databases.createEnumAttribute(DATABASE_ID, 'community_tips', 'category', 
            ['GENERAL', 'NUTRITION', 'EXERCISE', 'GROOMING', 'MEDICAL', 'BEHAVIORAL'], true);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'title', 255, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'content', 3000, true);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'breedSpecific', 1000, false); // JSON array
        await databases.createEnumAttribute(DATABASE_ID, 'community_tips', 'ageGroup', 
            ['PUPPY', 'YOUNG', 'ADULT', 'SENIOR'], false);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'images', 2000, false); // JSON array
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'videoUrl', 500, false);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'tags', 1000, false); // JSON array
        await databases.createIntegerAttribute(DATABASE_ID, 'community_tips', 'likeCount', true, 0, 999999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'community_tips', 'saveCount', true, 0, 999999, 0);
        await databases.createIntegerAttribute(DATABASE_ID, 'community_tips', 'shareCount', true, 0, 999999, 0);
        await databases.createDatetimeAttribute(DATABASE_ID, 'community_tips', 'createdAt', true);
        await databases.createBooleanAttribute(DATABASE_ID, 'community_tips', 'isVerified', true);
        await databases.createStringAttribute(DATABASE_ID, 'community_tips', 'verifiedBy', 255, false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'community_tips', 'tipsByCategory', 'key', ['category']);
        await databases.createIndex(DATABASE_ID, 'community_tips', 'popularTips', 'key', ['likeCount']);
        
        console.log('✓ Community tips collection created');
        
        // 9. Create User Interactions Collection
        console.log('\nCreating user_interactions collection...');
        const userInteractionsCollection = await databases.createCollection(
            DATABASE_ID,
            'user_interactions',
            'User Interactions',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // User Interactions attributes
        await databases.createStringAttribute(DATABASE_ID, 'user_interactions', 'userId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'user_interactions', 'contentType', 
            ['POST');
        await databases.createStringAttribute(DATABASE_ID, 'user_interactions', 'contentId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'user_interactions', 'interactionType', 
            ['LIKE');
        await databases.createDatetimeAttribute(DATABASE_ID, 'user_interactions', 'timestamp', true);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'user_interactions', 'userInteractions', 'key', ['userId']);
        await databases.createIndex(DATABASE_ID, 'user_interactions', 'contentInteractions', 'key', ['contentId']);
        
        console.log('✓ User interactions collection created');
        
        // 10. Create Content Reports Collection
        console.log('\nCreating content_reports collection...');
        const contentReportsCollection = await databases.createCollection(
            DATABASE_ID,
            'content_reports',
            'Content Reports',
            [
                "read(\"users\")",
                "create(\"users\")",
                "update(\"users\")",
                "delete(\"users\")"
            ]
        );
        
        // Content Reports attributes
        await databases.createStringAttribute(DATABASE_ID, 'content_reports', 'reporterId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'content_reports', 'contentType', 
            ['POST');
        await databases.createStringAttribute(DATABASE_ID, 'content_reports', 'contentId', 36, true);
        await databases.createEnumAttribute(DATABASE_ID, 'content_reports', 'reason', 
            ['INAPPROPRIATE');
        await databases.createStringAttribute(DATABASE_ID, 'content_reports', 'description', 1000, true);
        await databases.createDatetimeAttribute(DATABASE_ID, 'content_reports', 'reportedAt', true);
        await databases.createEnumAttribute(DATABASE_ID, 'content_reports', 'status', 
            ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'], true);
        await databases.createStringAttribute(DATABASE_ID, 'content_reports', 'reviewedBy', 36, false);
        await databases.createDatetimeAttribute(DATABASE_ID, 'content_reports', 'reviewedAt', false);
        await databases.createEnumAttribute(DATABASE_ID, 'content_reports', 'action', 
            ['WARNING', 'CONTENT_REMOVED', 'USER_SUSPENDED', 'USER_BANNED', 'NO_ACTION'], false);
        
        // Indexes
        await databases.createIndex(DATABASE_ID, 'content_reports', 'pendingReports', 'key', ['status']);
        
        console.log('✓ Content reports collection created');
        
        console.log('\n✅ All community collections created successfully!');
        
    } catch (error) {
        console.error('Error creating collections:', error);
        
        // If error is due to collection already existing, try to just add missing attributes
        if (error.message && error.message.includes('already exists')) {
            console.log('\nCollections may already exist. Attempting to add missing attributes...');
            await addMissingCommunityAttributes();
        }
    }
}

async function addMissingCommunityAttributes() {
    try {
        console.log('\nChecking for missing attributes in existing collections...');
        
        // This would need to be implemented based on what attributes are missing
        // For now, we'll just log that we tried
        console.log('Please manually verify that all required attributes exist in the collections.');
        
    } catch (error) {
        console.error('Error adding missing attributes:', error);
    }
}

// Run the setup
createCommunityCollections()
    .then(() => {
        console.log('\nCommunity database setup complete!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\nSetup failed:', error);
        process.exit(1);
    });