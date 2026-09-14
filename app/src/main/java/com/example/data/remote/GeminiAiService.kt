package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val tag = "GeminiAiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val apiKey: String
        get() {
            val key = try {
                BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            } catch (e: Exception) {
                null
            } ?: try {
                BuildConfig::class.java.getField("AI_API_KEY").get(null) as? String
            } catch (e: Exception) {
                null
            }
            return if (!key.isNullOrBlank() && !key.contains("MY_GEMINI_API_KEY")) key.trim() else ""
        }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    suspend fun generateResponse(
        prompt: String,
        history: List<Pair<String, Boolean>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel.")
            )
        }

        try {
            // Using gemini-3.5-flash per Gemini API guidelines for thinking and text Q&A
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // System persona context for SOCH
            val systemPrompt = "You are SOCH AI, the thoughtful, calm, and intellectually rigorous companion inside SOCH (Think. Connect. Grow.). Help users study, brainstorm, dissect concepts, and elevate their thinking. Respond with clarity, elegance, and structured markdown. Keep explanations concise, insightful, and human."

            // Past conversation context
            for ((text, isUser) in history.takeLast(6)) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                })
            }

            // Current message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            })

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1200)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        val text = parts.getJSONObject(0).getString("text")
                        Result.success(text.trim())
                    } else {
                        Result.failure(Exception("No response generated from SOCH AI"))
                    }
                } else {
                    Log.e(tag, "Gemini API error: ${response.code} $responseBody")
                    val errorMsg = try {
                        JSONObject(responseBody).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "AI request failed with code ${response.code}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to call Gemini API", e)
            Result.failure(e)
        }
    }
}
