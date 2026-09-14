package com.example.data.model

import java.util.UUID

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val username: String,
    val bio: String = "",
    val interests: List<String> = emptyList(),
    val avatarUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val author: UserProfile? = null,
    val isLikedByMe: Boolean = false
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val author: UserProfile? = null
)

data class FollowRelation(
    val id: String = UUID.randomUUID().toString(),
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val otherUser: UserProfile,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val actor: UserProfile,
    val type: NotificationType,
    val text: String,
    val postId: String? = null,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    FOLLOW, LIKE, COMMENT, MESSAGE, MENTION
}

data class ContentReport(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String,
    val targetType: String, // "post", "comment", "profile"
    val targetId: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isGenerating: Boolean = false
)
