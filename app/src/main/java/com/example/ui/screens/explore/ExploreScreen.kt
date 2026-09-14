package com.example.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PostCard
import com.example.ui.components.SochTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.screens.onboarding.ALL_INTERESTS
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    repository: SochRepository,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onCommentClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>("AI & Tech") }

    val allPosts by repository.posts.collectAsState()
    val discoverProfiles = remember { repository.getDiscoverProfiles() }
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    // Filter posts and people based on query and topic
    val filteredPosts = remember(searchQuery, selectedCategory, allPosts) {
        allPosts.filter { post ->
            val matchQuery = searchQuery.isBlank() ||
                    post.content.contains(searchQuery, ignoreCase = true) ||
                    (post.author?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                    (post.author?.username?.contains(searchQuery, ignoreCase = true) == true)

            val matchCategory = selectedCategory == null ||
                    post.content.contains(selectedCategory!!, ignoreCase = true) ||
                    (post.author?.interests?.contains(selectedCategory) == true)

            matchQuery && (searchQuery.isNotBlank() || matchCategory)
        }
    }

    val filteredPeople = remember(searchQuery, discoverProfiles) {
        if (searchQuery.isBlank()) emptyList()
        else discoverProfiles.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.interests.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "Explore SOCH",
                subtitle = "Discover ideas, thinkers, and trending topics",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Search Bar Item
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search people, posts, or topics...", color = SochTextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SochTextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = SochTextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SochSurface,
                            unfocusedContainerColor = SochSurface,
                            focusedBorderColor = SochPrimary,
                            unfocusedBorderColor = SochBorder,
                            focusedTextColor = SochTextPrimary,
                            unfocusedTextColor = SochTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Topic Categories Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            Surface(
                                color = if (selectedCategory == null) SochPrimary else SochSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCategory == null) SochPrimary else SochBorder),
                                modifier = Modifier.clickable { selectedCategory = null }
                            ) {
                                Text(
                                    text = "All Topics",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedCategory == null) SochTextPrimary else SochTextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(ALL_INTERESTS.take(10)) { topic ->
                            val isSelected = selectedCategory == topic
                            Surface(
                                color = if (isSelected) SochPrimary else SochSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SochPrimary else SochBorder),
                                modifier = Modifier.clickable {
                                    selectedCategory = if (isSelected) null else topic
                                }
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) SochTextPrimary else SochTextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // People Results if searching
            if (filteredPeople.isNotEmpty()) {
                item {
                    Text(
                        text = "People (${filteredPeople.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(filteredPeople) { person ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SochSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .border(1.dp, SochBorder, RoundedCornerShape(14.dp))
                            .clickable { onUserClick(person.username) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            UserAvatar(name = person.name, avatarUrl = person.avatarUrl, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SochTextPrimary
                                    )
                                    if (person.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        VerifiedBadge()
                                    }
                                }
                                Text(text = "@${person.username}", style = MaterialTheme.typography.bodySmall, color = SochTextMuted)
                            }
                        }
                    }
                }
            }

            // Discussions / Posts Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = SochPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else "Trending Discussions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )
                }
            }

            if (filteredPosts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.Explore,
                        title = "Nothing found",
                        subtitle = "Try adjusting your search keywords or topic category."
                    )
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUser?.id,
                        onLikeClick = { repository.toggleLike(post.id) },
                        onCommentClick = { onCommentClick(post.id) },
                        onAuthorClick = { username -> onUserClick(username) },
                        onDeleteClick = { scope.launch { repository.deletePost(post.id) } },
                        onReportClick = { scope.launch { repository.reportContent("post", post.id, "Report") } },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
