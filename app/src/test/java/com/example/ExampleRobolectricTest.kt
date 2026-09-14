package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SochRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies SOCH app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SOCH", appName)
    }

    @Test
    fun `official developer profile is Dipak and has 1M followers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SochRepository(context)
        val official = repository.officialDeveloperProfile
        assertEquals("Dipak", official.name)
        assertEquals("dipak.dev", official.username)
        assertEquals(1000000, official.followersCount)
        assertTrue(official.isVerified)

        val discover = repository.getDiscoverProfiles()
        assertEquals("dipak.dev", discover.first().username)
    }

    @Test
    fun `post creation and liking flow works as expected`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SochRepository(context)

        // Sign in locally
        repository.signIn("test@soch.app", "password123")
        assertTrue(repository.isLoggedIn.value)

        // Create post
        val postResult = repository.createPost("Testing SOCH thoughts on engineering and AI")
        assertTrue(postResult.isSuccess)
        val post = postResult.getOrThrow()

        // Like toggle
        assertFalse(post.isLikedByMe)
        repository.toggleLike(post.id)
        val updatedPost = repository.posts.value.first { it.id == post.id }
        assertTrue(updatedPost.isLikedByMe)
        assertEquals(1, updatedPost.likesCount)

        // Comment
        val commentRes = repository.addComment(post.id, "Remarkable perspective on SOCH!")
        assertTrue(commentRes.isSuccess)
        val comments = repository.getCommentsForPost(post.id)
        assertTrue(comments.any { it.content.contains("Remarkable") })
    }
}
