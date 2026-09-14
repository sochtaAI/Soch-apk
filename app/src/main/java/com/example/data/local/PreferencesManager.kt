package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import org.json.JSONArray

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("soch_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_AUTH_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"

        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_USERNAME = "profile_username"
        private const val KEY_PROFILE_BIO = "profile_bio"
        private const val KEY_PROFILE_AVATAR = "profile_avatar"
        private const val KEY_PROFILE_INTERESTS = "profile_interests"
        private const val KEY_PROFILE_FOLLOWERS = "profile_followers"
        private const val KEY_PROFILE_FOLLOWING = "profile_following"

        private const val KEY_LIKED_POSTS = "liked_posts_ids"
        private const val KEY_FOLLOWED_USERS = "followed_user_ids"

        private const val KEY_CUSTOM_SUPABASE_URL = "custom_supabase_url"
        private const val KEY_CUSTOM_SUPABASE_KEY = "custom_supabase_key"
    }

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var customSupabaseUrl: String?
        get() = prefs.getString(KEY_CUSTOM_SUPABASE_URL, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_SUPABASE_URL, value).apply()

    var customSupabaseKey: String?
        get() = prefs.getString(KEY_CUSTOM_SUPABASE_KEY, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_SUPABASE_KEY, value).apply()

    val isLoggedIn: Boolean
        get() = !userId.isNullOrBlank()

    val hasCompletedProfile: Boolean
        get() = !prefs.getString(KEY_PROFILE_NAME, null).isNullOrBlank() &&
                !prefs.getString(KEY_PROFILE_USERNAME, null).isNullOrBlank()

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_PROFILE_NAME, profile.name)
            .putString(KEY_PROFILE_USERNAME, profile.username)
            .putString(KEY_PROFILE_BIO, profile.bio)
            .putString(KEY_PROFILE_AVATAR, profile.avatarUrl)
            .putString(KEY_PROFILE_INTERESTS, JSONArray(profile.interests).toString())
            .putInt(KEY_PROFILE_FOLLOWERS, profile.followersCount)
            .putInt(KEY_PROFILE_FOLLOWING, profile.followingCount)
            .apply()
    }

    fun getProfile(): UserProfile? {
        val uid = userId ?: return null
        val name = prefs.getString(KEY_PROFILE_NAME, null) ?: return null
        val username = prefs.getString(KEY_PROFILE_USERNAME, "member") ?: "member"
        val bio = prefs.getString(KEY_PROFILE_BIO, "") ?: ""
        val avatar = prefs.getString(KEY_PROFILE_AVATAR, "") ?: ""
        val followers = prefs.getInt(KEY_PROFILE_FOLLOWERS, 0)
        val following = prefs.getInt(KEY_PROFILE_FOLLOWING, 0)

        val interestsStr = prefs.getString(KEY_PROFILE_INTERESTS, "[]")
        val interests = mutableListOf<String>()
        try {
            val arr = JSONArray(interestsStr)
            for (i in 0 until arr.length()) {
                interests.add(arr.getString(i))
            }
        } catch (e: Exception) {
            // ignore
        }

        return UserProfile(
            id = uid,
            name = name,
            username = username,
            bio = bio,
            interests = interests,
            avatarUrl = avatar,
            followersCount = followers,
            followingCount = following,
            isOnline = true
        )
    }

    fun getLikedPostIds(): Set<String> {
        return prefs.getStringSet(KEY_LIKED_POSTS, emptySet()) ?: emptySet()
    }

    fun setPostLiked(postId: String, liked: Boolean) {
        val current = getLikedPostIds().toMutableSet()
        if (liked) current.add(postId) else current.remove(postId)
        prefs.edit().putStringSet(KEY_LIKED_POSTS, current).apply()
    }

    fun getFollowedUserIds(): Set<String> {
        return prefs.getStringSet(KEY_FOLLOWED_USERS, emptySet()) ?: emptySet()
    }

    fun setUserFollowed(targetUserId: String, followed: Boolean) {
        val current = getFollowedUserIds().toMutableSet()
        if (followed) current.add(targetUserId) else current.remove(targetUserId)
        prefs.edit().putStringSet(KEY_FOLLOWED_USERS, current).apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_PROFILE_NAME)
            .remove(KEY_PROFILE_USERNAME)
            .remove(KEY_PROFILE_BIO)
            .remove(KEY_PROFILE_AVATAR)
            .remove(KEY_PROFILE_INTERESTS)
            .remove(KEY_PROFILE_FOLLOWERS)
            .remove(KEY_PROFILE_FOLLOWING)
            .apply()
    }
}
