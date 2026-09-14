package com.example.data.repository

import android.content.Context
import com.example.data.local.PreferencesManager
import com.example.data.model.*
import com.example.data.remote.GeminiAiService
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SochRepository(context: Context) {
    private val prefs = PreferencesManager(context)
    val supabase = SupabaseClient(prefs.customSupabaseUrl, prefs.customSupabaseKey)
    val aiService = GeminiAiService()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current Auth State
    private val _currentUser = MutableStateFlow<UserProfile?>(prefs.getProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _hasCompletedProfile = MutableStateFlow(prefs.hasCompletedProfile)
    val hasCompletedProfile: StateFlow<Boolean> = _hasCompletedProfile.asStateFlow()

    // Official seeded profile Dipak (@dipak.dev) - MUST be pinned permanently first
    val officialDeveloperProfile = UserProfile(
        id = "00000000-0000-0000-0000-000000000001",
        name = "Dipak",
        username = "dipak.dev",
        bio = "Building SOCH — a space for ideas, learning, technology and meaningful connections. Exploring AI, building useful products and helping people think, learn and grow.",
        interests = listOf("Developer", "AI", "Technology"),
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        followersCount = 1000000,
        followingCount = 84,
        isOnline = true,
        isVerified = true
    )

    // Seed Recommended Profiles
    private val seedProfiles = listOf(
        officialDeveloperProfile,
        UserProfile(
            id = "00000000-0000-0000-0000-000000000002",
            name = "Aarav",
            username = "aarav.tech",
            bio = "Engineering next-gen software systems, open source contributor & building AI apps.",
            interests = listOf("AI & Tech", "Coding"),
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
            followersCount = 4200,
            followingCount = 150,
            isOnline = true,
            isVerified = false
        ),
        UserProfile(
            id = "00000000-0000-0000-0000-000000000003",
            name = "Riya",
            username = "riya.learns",
            bio = "Cognitive neuroscience student. Passionate about how our minds perceive knowledge and creativity.",
            interests = listOf("Study", "Science"),
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
            followersCount = 8900,
            followingCount = 210,
            isOnline = false,
            isVerified = false
        ),
        UserProfile(
            id = "00000000-0000-0000-0000-000000000004",
            name = "Kabir",
            username = "kabir.creator",
            bio = "Product designer & writer. Exploring minimal interfaces and human-centered thinking.",
            interests = listOf("Creativity", "Design"),
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
            followersCount = 12500,
            followingCount = 312,
            isOnline = true,
            isVerified = false
        )
    )

    // Feed Posts State
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Followed User IDs Set
    private val _followedUserIds = MutableStateFlow<Set<String>>(prefs.getFollowedUserIds())
    val followedUserIds: StateFlow<Set<String>> = _followedUserIds.asStateFlow()

    // Comments State mapped by postId
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    // Conversations & Messages State
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // AI Messages
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                text = "Welcome to SOCH AI. Think better, learn faster. Ask any question, explore complex concepts, or brainstorm ideas.",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    init {
        initializeInitialData()
        refreshFeed()
    }

    private fun initializeInitialData() {
        val likedSet = prefs.getLikedPostIds()
        val defaultPosts = listOf(
            Post(
                id = "10000000-0000-0000-0000-000000000001",
                userId = officialDeveloperProfile.id,
                content = "Welcome to SOCH. A calm space where technology, ideas, and intellect converge. Here we focus on depth, curiosity, and building together. What thought is shaping your day?",
                likesCount = 1420,
                commentsCount = 2,
                createdAt = System.currentTimeMillis() - 3600000,
                author = officialDeveloperProfile,
                isLikedByMe = likedSet.contains("10000000-0000-0000-0000-000000000001")
            ),
            Post(
                id = "10000000-0000-0000-0000-000000000002",
                userId = officialDeveloperProfile.id,
                content = "The future of software is not more noise—it is elevated signal. With AI as a thinking partner, our creativity is multiplied, not replaced.",
                likesCount = 980,
                commentsCount = 1,
                createdAt = System.currentTimeMillis() - 14400000,
                author = officialDeveloperProfile,
                isLikedByMe = likedSet.contains("10000000-0000-0000-0000-000000000002")
            ),
            Post(
                id = "10000000-0000-0000-0000-000000000003",
                userId = "00000000-0000-0000-0000-000000000002",
                content = "Building systems that prioritize clarity over complexity is one of the hardest disciplines in modern engineering. How do you simplify your architecture?",
                likesCount = 312,
                commentsCount = 0,
                createdAt = System.currentTimeMillis() - 28800000,
                author = seedProfiles[1],
                isLikedByMe = likedSet.contains("10000000-0000-0000-0000-000000000003")
            )
        )
        _posts.value = defaultPosts

        // Seed Comments
        _comments.value = mapOf(
            "10000000-0000-0000-0000-000000000001" to listOf(
                Comment(
                    id = "c1",
                    postId = "10000000-0000-0000-0000-000000000001",
                    userId = "00000000-0000-0000-0000-000000000002",
                    content = "Incredible to see this vision take form! The calm aesthetic sets a whole new benchmark.",
                    createdAt = System.currentTimeMillis() - 2400000,
                    author = seedProfiles[1]
                ),
                Comment(
                    id = "c2",
                    postId = "10000000-0000-0000-0000-000000000001",
                    userId = "00000000-0000-0000-0000-000000000003",
                    content = "A breath of fresh air compared to typical feeds. Looking forward to deep discussions here.",
                    createdAt = System.currentTimeMillis() - 1800000,
                    author = seedProfiles[2]
                )
            ),
            "10000000-0000-0000-0000-000000000002" to listOf(
                Comment(
                    id = "c3",
                    postId = "10000000-0000-0000-0000-000000000002",
                    userId = "00000000-0000-0000-0000-000000000004",
                    content = "Amplifying human intellect rather than cognitive overload. Exactly what's needed.",
                    createdAt = System.currentTimeMillis() - 10800000,
                    author = seedProfiles[3]
                )
            )
        )

        // Seed conversations
        _conversations.value = listOf(
            Conversation(
                id = "conv_1",
                otherUser = officialDeveloperProfile,
                lastMessage = "Welcome to SOCH! Let me know what you'd like to see next.",
                lastMessageTime = System.currentTimeMillis() - 3600000,
                unreadCount = 1
            ),
            Conversation(
                id = "conv_2",
                otherUser = seedProfiles[1],
                lastMessage = "Checking out the new AI models, love the clarity.",
                lastMessageTime = System.currentTimeMillis() - 86400000,
                unreadCount = 0
            )
        )

        _messages.value = mapOf(
            "conv_1" to listOf(
                ChatMessage(
                    id = "m1",
                    conversationId = "conv_1",
                    senderId = officialDeveloperProfile.id,
                    content = "Hi there! Welcome to SOCH. Feel free to explore, connect, and share your ideas.",
                    createdAt = System.currentTimeMillis() - 4000000
                ),
                ChatMessage(
                    id = "m2",
                    conversationId = "conv_1",
                    senderId = officialDeveloperProfile.id,
                    content = "Welcome to SOCH! Let me know what you'd like to see next.",
                    createdAt = System.currentTimeMillis() - 3600000
                )
            )
        )

        // Seed notifications
        _notifications.value = listOf(
            AppNotification(
                id = "notif_1",
                userId = prefs.userId ?: "me",
                actor = officialDeveloperProfile,
                type = NotificationType.FOLLOW,
                text = "Dipak followed you on SOCH",
                read = false,
                createdAt = System.currentTimeMillis() - 7200000
            ),
            AppNotification(
                id = "notif_2",
                userId = prefs.userId ?: "me",
                actor = seedProfiles[1],
                type = NotificationType.LIKE,
                text = "Aarav liked your response in AI discussions",
                read = true,
                createdAt = System.currentTimeMillis() - 14400000
            )
        )
    }

    // --- AUTH METHODS ---
    suspend fun signUp(email: String, pass: String, name: String, username: String): Result<Unit> {
        return try {
            if (supabase.isConfigured) {
                val res = supabase.signUp(email, pass, name, username)
                if (res.isSuccess) {
                    val auth = res.getOrThrow()
                    prefs.userId = auth.userId.ifBlank { UUID.randomUUID().toString() }
                    prefs.userEmail = email
                    prefs.authToken = auth.accessToken
                    prefs.refreshToken = auth.refreshToken
                    val profile = UserProfile(
                        id = prefs.userId!!,
                        name = name,
                        username = username,
                        isOnline = true
                    )
                    prefs.saveProfile(profile)
                    _currentUser.value = profile
                    _isLoggedIn.value = true
                    _hasCompletedProfile.value = false // proceed to onboarding
                    Result.success(Unit)
                } else {
                    Result.failure(res.exceptionOrNull() ?: Exception("Sign up error"))
                }
            } else {
                // Seamless local auth for smooth setup
                val uid = UUID.randomUUID().toString()
                prefs.userId = uid
                prefs.userEmail = email
                prefs.authToken = "local_token_$uid"
                val profile = UserProfile(
                    id = uid,
                    name = name,
                    username = username,
                    isOnline = true
                )
                prefs.saveProfile(profile)
                _currentUser.value = profile
                _isLoggedIn.value = true
                _hasCompletedProfile.value = false
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            if (supabase.isConfigured) {
                val res = supabase.signIn(email, pass)
                if (res.isSuccess) {
                    val auth = res.getOrThrow()
                    prefs.userId = auth.userId
                    prefs.userEmail = email
                    prefs.authToken = auth.accessToken
                    prefs.refreshToken = auth.refreshToken

                    // Fetch profile
                    val profRes = supabase.fetchProfile(auth.userId)
                    val profile = profRes.getOrNull() ?: UserProfile(
                        id = auth.userId,
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        username = email.substringBefore("@").lowercase(),
                        isOnline = true
                    )
                    prefs.saveProfile(profile)
                    _currentUser.value = profile
                    _isLoggedIn.value = true
                    _hasCompletedProfile.value = true
                    Result.success(Unit)
                } else {
                    Result.failure(res.exceptionOrNull() ?: Exception("Sign in error"))
                }
            } else {
                // Local sign in fallback
                val uid = prefs.userId ?: UUID.randomUUID().toString()
                prefs.userId = uid
                prefs.userEmail = email
                val profile = prefs.getProfile() ?: UserProfile(
                    id = uid,
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    username = email.substringBefore("@").lowercase(),
                    isOnline = true
                )
                prefs.saveProfile(profile)
                _currentUser.value = profile
                _isLoggedIn.value = true
                _hasCompletedProfile.value = true
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        if (supabase.isConfigured) {
            supabase.signOut(prefs.authToken)
        }
        prefs.clearSession()
        _currentUser.value = null
        _isLoggedIn.value = false
        _hasCompletedProfile.value = false
    }

    // --- PROFILE ONBOARDING & UPDATES ---
    suspend fun completeProfile(name: String, username: String, interests: List<String>, bio: String = ""): Result<Unit> {
        val uid = prefs.userId ?: UUID.randomUUID().toString()
        val current = _currentUser.value
        val updated = UserProfile(
            id = uid,
            name = name.trim(),
            username = username.trim().removePrefix("@"),
            bio = bio.ifBlank { current?.bio.orEmpty() },
            interests = interests,
            avatarUrl = current?.avatarUrl.orEmpty(),
            followersCount = current?.followersCount ?: 0,
            followingCount = current?.followingCount ?: 0,
            isOnline = true
        )
        prefs.saveProfile(updated)
        _currentUser.value = updated
        _hasCompletedProfile.value = true

        if (supabase.isConfigured) {
            scope.launch {
                supabase.upsertProfile(updated, prefs.authToken)
            }
        }
        return Result.success(Unit)
    }

    suspend fun updateProfile(name: String, username: String, bio: String, avatarUrl: String, interests: List<String>): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        val updated = current.copy(
            name = name.trim(),
            username = username.trim().removePrefix("@"),
            bio = bio.trim(),
            avatarUrl = avatarUrl.trim(),
            interests = interests
        )
        prefs.saveProfile(updated)
        _currentUser.value = updated

        if (supabase.isConfigured) {
            scope.launch {
                supabase.upsertProfile(updated, prefs.authToken)
            }
        }
        return Result.success(Unit)
    }

    // --- FEED & POSTS ---
    fun refreshFeed() {
        scope.launch {
            if (supabase.isConfigured) {
                val res = supabase.fetchPosts()
                if (res.isSuccess) {
                    val remotePosts = res.getOrThrow()
                    val likedSet = prefs.getLikedPostIds()
                    _posts.value = remotePosts.map { it.copy(isLikedByMe = likedSet.contains(it.id)) }
                }
            }
        }
    }

    suspend fun createPost(content: String): Result<Post> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Post cannot be empty"))
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Must be logged in"))

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            content = content.trim(),
            likesCount = 0,
            commentsCount = 0,
            createdAt = System.currentTimeMillis(),
            author = user,
            isLikedByMe = false
        )

        // Optimistic feed update
        _posts.value = listOf(newPost) + _posts.value

        if (supabase.isConfigured) {
            scope.launch {
                val res = supabase.createPost(content.trim(), user.id, prefs.authToken)
                if (res.isSuccess) {
                    refreshFeed()
                }
            }
        }
        return Result.success(newPost)
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        val currentPosts = _posts.value.toMutableList()
        val post = currentPosts.find { it.id == postId }
        if (post != null && post.userId == prefs.userId) {
            currentPosts.remove(post)
            _posts.value = currentPosts

            if (supabase.isConfigured) {
                scope.launch {
                    supabase.deletePost(postId, prefs.authToken)
                }
            }
            return Result.success(Unit)
        }
        return Result.failure(Exception("Cannot delete post"))
    }

    // --- LIKES ---
    fun toggleLike(postId: String) {
        val user = _currentUser.value ?: return
        val currentPosts = _posts.value.toMutableList()
        val index = currentPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = currentPosts[index]
            val newLikedState = !post.isLikedByMe
            val newLikesCount = if (newLikedState) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)

            currentPosts[index] = post.copy(
                isLikedByMe = newLikedState,
                likesCount = newLikesCount
            )
            _posts.value = currentPosts
            prefs.setPostLiked(postId, newLikedState)

            if (supabase.isConfigured) {
                scope.launch {
                    if (newLikedState) {
                        supabase.likePost(postId, user.id, prefs.authToken)
                    } else {
                        supabase.unlikePost(postId, user.id, prefs.authToken)
                    }
                }
            }
        }
    }

    // --- COMMENTS ---
    fun getCommentsForPost(postId: String): List<Comment> {
        return _comments.value[postId] ?: emptyList()
    }

    suspend fun addComment(postId: String, content: String): Result<Comment> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Comment cannot be empty"))
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Must be logged in"))

        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = user.id,
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            author = user
        )

        // Optimistic update
        val map = _comments.value.toMutableMap()
        val list = (map[postId] ?: emptyList()).toMutableList()
        list.add(newComment)
        map[postId] = list
        _comments.value = map

        // Update post comments count
        val postList = _posts.value.toMutableList()
        val idx = postList.indexOfFirst { it.id == postId }
        if (idx != -1) {
            postList[idx] = postList[idx].copy(commentsCount = postList[idx].commentsCount + 1)
            _posts.value = postList
        }

        if (supabase.isConfigured) {
            scope.launch {
                supabase.addComment(postId, user.id, content.trim(), prefs.authToken)
            }
        }
        return Result.success(newComment)
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        val map = _comments.value.toMutableMap()
        val list = (map[postId] ?: emptyList()).toMutableList()
        val comment = list.find { it.id == commentId }
        if (comment != null && comment.userId == prefs.userId) {
            list.remove(comment)
            map[postId] = list
            _comments.value = map

            val postList = _posts.value.toMutableList()
            val idx = postList.indexOfFirst { it.id == postId }
            if (idx != -1) {
                postList[idx] = postList[idx].copy(commentsCount = (postList[idx].commentsCount - 1).coerceAtLeast(0))
                _posts.value = postList
            }

            if (supabase.isConfigured) {
                scope.launch {
                    supabase.deleteComment(commentId)
                }
            }
            return Result.success(Unit)
        }
        return Result.failure(Exception("Cannot delete comment"))
    }

    // --- CONNECT & FOLLOWS ---
    fun getDiscoverProfiles(): List<UserProfile> {
        val currentUserId = prefs.userId
        // Official Developer profile MUST appear first and remain pinned per section 9 & 10 & 40
        val list = mutableListOf<UserProfile>()
        list.add(officialDeveloperProfile)
        seedProfiles.filter { it.id != officialDeveloperProfile.id && it.id != currentUserId }
            .forEach { list.add(it) }
        return list
    }

    fun isFollowing(userId: String): Boolean {
        return _followedUserIds.value.contains(userId)
    }

    fun toggleFollow(targetUserId: String) {
        val currentUserId = prefs.userId ?: return
        if (targetUserId == currentUserId) return // cannot follow self per rule

        val currentFollowed = _followedUserIds.value.toMutableSet()
        val willFollow = !currentFollowed.contains(targetUserId)

        if (willFollow) {
            currentFollowed.add(targetUserId)
        } else {
            currentFollowed.remove(targetUserId)
        }
        _followedUserIds.value = currentFollowed
        prefs.setUserFollowed(targetUserId, willFollow)

        // Update following count on current user
        _currentUser.value?.let { me ->
            val newCount = if (willFollow) me.followingCount + 1 else (me.followingCount - 1).coerceAtLeast(0)
            val updatedMe = me.copy(followingCount = newCount)
            _currentUser.value = updatedMe
            prefs.saveProfile(updatedMe)
        }

        if (supabase.isConfigured) {
            scope.launch {
                if (willFollow) {
                    supabase.followUser(currentUserId, targetUserId, prefs.authToken)
                } else {
                    supabase.unfollowUser(currentUserId, targetUserId, prefs.authToken)
                }
            }
        }
    }

    fun getProfileByUsername(username: String): UserProfile? {
        val clean = username.removePrefix("@").lowercase()
        if (clean == officialDeveloperProfile.username.lowercase()) return officialDeveloperProfile
        val seed = seedProfiles.find { it.username.lowercase() == clean }
        if (seed != null) return seed
        val me = _currentUser.value
        if (me != null && me.username.lowercase() == clean) return me
        return null
    }

    fun getPostsByUserId(userId: String): List<Post> {
        return _posts.value.filter { it.userId == userId }
    }

    // --- MESSAGING ---
    fun getMessagesForConversation(convId: String): List<ChatMessage> {
        return _messages.value[convId] ?: emptyList()
    }

    suspend fun sendMessage(convId: String, content: String): Result<ChatMessage> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be empty"))
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            senderId = user.id,
            content = content.trim(),
            createdAt = System.currentTimeMillis()
        )

        val map = _messages.value.toMutableMap()
        val list = (map[convId] ?: emptyList()).toMutableList()
        list.add(msg)
        map[convId] = list
        _messages.value = map

        // Update conversation last message
        val convList = _conversations.value.toMutableList()
        val idx = convList.indexOfFirst { it.id == convId }
        if (idx != -1) {
            convList[idx] = convList[idx].copy(
                lastMessage = content.trim(),
                lastMessageTime = System.currentTimeMillis()
            )
            _conversations.value = convList
        }
        return Result.success(msg)
    }

    fun getOrCreateConversationWith(user: UserProfile): Conversation {
        val existing = _conversations.value.find { it.otherUser.id == user.id }
        if (existing != null) return existing

        val newConv = Conversation(
            id = "conv_${UUID.randomUUID()}",
            otherUser = user,
            lastMessage = "Started a conversation",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0
        )
        _conversations.value = listOf(newConv) + _conversations.value
        return newConv
    }

    // --- REPORTS ---
    suspend fun reportContent(targetType: String, targetId: String, reason: String): Result<Unit> {
        val reporterId = prefs.userId ?: "anonymous"
        if (supabase.isConfigured) {
            scope.launch {
                // Call supabase report endpoint
            }
        }
        return Result.success(Unit)
    }

    // --- AI ASSISTANT ---
    suspend fun sendAiQuery(prompt: String): Result<String> {
        val currentMsgs = _aiMessages.value.toMutableList()
        val userMsg = AiChatMessage(text = prompt, isUser = true)
        currentMsgs.add(userMsg)

        val generatingMsg = AiChatMessage(text = "", isUser = false, isGenerating = true)
        currentMsgs.add(generatingMsg)
        _aiMessages.value = currentMsgs

        val history = _aiMessages.value
            .filter { !it.isGenerating && it != userMsg }
            .takeLast(6)
            .map { it.text to it.isUser }

        val result = aiService.generateResponse(prompt, history)

        val updatedMsgs = _aiMessages.value.toMutableList()
        updatedMsgs.remove(generatingMsg)

        if (result.isSuccess) {
            val replyText = result.getOrThrow()
            updatedMsgs.add(AiChatMessage(text = replyText, isUser = false))
            _aiMessages.value = updatedMsgs
            return Result.success(replyText)
        } else {
            val errorText = result.exceptionOrNull()?.message ?: "Unable to contact SOCH AI"
            updatedMsgs.add(AiChatMessage(text = "⚠️ $errorText", isUser = false))
            _aiMessages.value = updatedMsgs
            return result
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            AiChatMessage(
                text = "Welcome to SOCH AI. Think better, learn faster. Ask any question, explore complex concepts, or brainstorm ideas.",
                isUser = false
            )
        )
    }
}
