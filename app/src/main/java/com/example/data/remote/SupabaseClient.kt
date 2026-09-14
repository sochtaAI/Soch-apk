package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseClient(
    private var customUrl: String? = null,
    private var customKey: String? = null
) {
    private val tag = "SupabaseClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val supabaseUrl: String
        get() {
            if (!customUrl.isNullOrBlank()) return customUrl!!.trim().removeSuffix("/")
            val buildUrl = try {
                BuildConfig::class.java.getField("SUPABASE_URL").get(null) as? String
            } catch (e: Exception) {
                null
            }
            return if (!buildUrl.isNullOrBlank() && !buildUrl.contains("your-project")) {
                buildUrl.trim().removeSuffix("/")
            } else {
                "https://dummy.supabase.co"
            }
        }

    val supabaseKey: String
        get() {
            if (!customKey.isNullOrBlank()) return customKey!!.trim()
            val buildKey = try {
                BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String
            } catch (e: Exception) {
                null
            }
            return if (!buildKey.isNullOrBlank() && !buildKey.contains("your-anon-key")) {
                buildKey.trim()
            } else {
                ""
            }
        }

    val isConfigured: Boolean
        get() = supabaseKey.isNotBlank() && !supabaseUrl.contains("dummy.supabase.co")

    fun updateCredentials(url: String, key: String) {
        customUrl = url.trim()
        customKey = key.trim()
    }

    private fun addHeaders(builder: Request.Builder, authToken: String? = null): Request.Builder {
        builder.addHeader("apikey", supabaseKey)
        val token = if (!authToken.isNullOrBlank()) authToken else supabaseKey
        builder.addHeader("Authorization", "Bearer $token")
        builder.addHeader("Content-Type", "application/json")
        return builder
    }

    // --- AUTHENTICATION ---
    suspend fun signUp(email: String, pass: String, name: String, username: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Supabase credentials not configured"))
        }
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("data", JSONObject().apply {
                    put("name", name)
                    put("username", username)
                })
            }
            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request)

            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val jsonObj = JSONObject(body)
                    val userObj = jsonObj.optJSONObject("user") ?: jsonObj
                    val userId = userObj.optString("id")
                    val accessToken = jsonObj.optString("access_token")
                    val refreshToken = jsonObj.optString("refresh_token")
                    Result.success(AuthResponse(userId, email, accessToken, refreshToken))
                } else {
                    val err = parseError(body, "Signup failed (${response.code})")
                    Result.failure(Exception(err))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "signUp error", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Supabase credentials not configured"))
        }
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=password")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request)

            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val jsonObj = JSONObject(body)
                    val userObj = jsonObj.optJSONObject("user")
                    val userId = userObj?.optString("id") ?: jsonObj.optString("id")
                    val accessToken = jsonObj.optString("access_token")
                    val refreshToken = jsonObj.optString("refresh_token")
                    Result.success(AuthResponse(userId, email, accessToken, refreshToken))
                } else {
                    val err = parseError(body, "Sign in failed (${response.code})")
                    Result.failure(Exception(err))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "signIn error", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.success(Unit)
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/logout")
                .post("{}".toRequestBody(jsonMediaType))
            addHeaders(request, token)
            client.newCall(request.build()).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit) // safe logout
        }
    }

    // --- PROFILES ---
    suspend fun fetchProfile(userId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?id=eq.$userId&select=*")
                .get()
            addHeaders(request)
            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val arr = JSONArray(body)
                    if (arr.length() > 0) {
                        Result.success(parseProfileJson(arr.getJSONObject(0)))
                    } else {
                        Result.success(null)
                    }
                } else {
                    Result.failure(Exception("Fetch profile error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertProfile(profile: UserProfile, token: String?): Result<UserProfile> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val json = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("username", profile.username)
                put("bio", profile.bio)
                put("interests", JSONArray(profile.interests))
                put("avatar_url", profile.avatarUrl)
                put("followers_count", profile.followersCount)
                put("following_count", profile.followingCount)
                put("is_online", profile.isOnline)
                put("is_verified", profile.isVerified)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request, token)

            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception(parseError(body, "Profile save error (${response.code})")))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- POSTS ---
    suspend fun fetchPosts(): Result<List<Post>> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts?select=*,profiles(*)&order=created_at.desc")
                .get()
            addHeaders(request)

            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val arr = JSONArray(body)
                    val posts = mutableListOf<Post>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val authorObj = obj.optJSONObject("profiles")
                        val author = authorObj?.let { parseProfileJson(it) }
                        posts.add(
                            Post(
                                id = obj.getString("id"),
                                userId = obj.getString("user_id"),
                                content = obj.getString("content"),
                                likesCount = obj.optInt("likes_count", 0),
                                commentsCount = obj.optInt("comments_count", 0),
                                createdAt = parseTimestamp(obj.optString("created_at")),
                                author = author
                            )
                        )
                    }
                    Result.success(posts)
                } else {
                    Result.failure(Exception("Fetch posts error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(content: String, userId: String, token: String?): Result<Post> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val json = JSONObject().apply {
                put("user_id", userId)
                put("content", content)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts")
                .addHeader("Prefer", "return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request, token)

            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val arr = JSONArray(body)
                    val obj = arr.getJSONObject(0)
                    Result.success(
                        Post(
                            id = obj.getString("id"),
                            userId = obj.getString("user_id"),
                            content = obj.getString("content"),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    Result.failure(Exception(parseError(body, "Create post error")))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String, token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts?id=eq.$postId")
                .delete()
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete failed (${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LIKES ---
    suspend fun likePost(postId: String, userId: String, token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val json = JSONObject().apply {
                put("post_id", postId)
                put("user_id", userId)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/likes")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Like failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: String, userId: String, token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/likes?post_id=eq.$postId&user_id=eq.$userId")
                .delete()
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Unlike failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- COMMENTS ---
    suspend fun fetchComments(postId: String): Result<List<Comment>> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/comments?post_id=eq.$postId&select=*,profiles(*)&order=created_at.asc")
                .get()
            addHeaders(request)
            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val arr = JSONArray(body)
                    val comments = mutableListOf<Comment>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val authorObj = obj.optJSONObject("profiles")
                        comments.add(
                            Comment(
                                id = obj.getString("id"),
                                postId = obj.getString("post_id"),
                                userId = obj.getString("user_id"),
                                content = obj.getString("content"),
                                createdAt = parseTimestamp(obj.optString("created_at")),
                                author = authorObj?.let { parseProfileJson(it) }
                            )
                        )
                    }
                    Result.success(comments)
                } else {
                    Result.failure(Exception("Fetch comments failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, userId: String, content: String, token: String?): Result<Comment> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val json = JSONObject().apply {
                put("post_id", postId)
                put("user_id", userId)
                put("content", content)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/comments")
                .addHeader("Prefer", "return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val arr = JSONArray(body)
                    val obj = arr.getJSONObject(0)
                    Result.success(
                        Comment(
                            id = obj.getString("id"),
                            postId = obj.getString("post_id"),
                            userId = obj.getString("user_id"),
                            content = obj.getString("content"),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    Result.failure(Exception("Add comment failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: String, token: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/comments?id=eq.$commentId")
                .delete()
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete comment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- FOLLOWS ---
    suspend fun followUser(followerId: String, followingId: String, token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val json = JSONObject().apply {
                put("follower_id", followerId)
                put("following_id", followingId)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/follows")
                .post(json.toString().toRequestBody(jsonMediaType))
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Follow failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String, token: String?): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext Result.failure(Exception("Not configured"))
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/follows?follower_id=eq.$followerId&following_id=eq.$followingId")
                .delete()
            addHeaders(request, token)
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Unfollow failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseProfileJson(obj: JSONObject): UserProfile {
        val interestsArr = obj.optJSONArray("interests")
        val interestsList = mutableListOf<String>()
        if (interestsArr != null) {
            for (i in 0 until interestsArr.length()) {
                interestsList.add(interestsArr.optString(i))
            }
        }
        return UserProfile(
            id = obj.getString("id"),
            name = obj.optString("name", "SOCH Member"),
            username = obj.optString("username", "member"),
            bio = obj.optString("bio", ""),
            interests = interestsList,
            avatarUrl = obj.optString("avatar_url", ""),
            followersCount = obj.optInt("followers_count", 0),
            followingCount = obj.optInt("following_count", 0),
            isOnline = obj.optBoolean("is_online", false),
            isVerified = obj.optBoolean("is_verified", false),
            createdAt = parseTimestamp(obj.optString("created_at"))
        )
    }

    private fun parseTimestamp(ts: String?): Long {
        if (ts.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            java.time.Instant.parse(ts).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseError(body: String, fallback: String): String {
        return try {
            val obj = JSONObject(body)
            obj.optString("msg", obj.optString("message", obj.optString("error_description", fallback)))
        } catch (e: Exception) {
            fallback
        }
    }
}

data class AuthResponse(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)
