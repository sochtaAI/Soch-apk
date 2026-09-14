package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.SochRepository
import com.example.ui.screens.ai.AiScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.comments.CommentsScreen
import com.example.ui.screens.connect.ConnectScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.messages.ConversationScreen
import com.example.ui.screens.messages.MessagesScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.onboarding.ProfileSetupScreen
import com.example.ui.screens.post.CreatePostScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.PublicUserProfileScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.*

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Connect : Screen("connect")
    object Ai : Screen("ai")
    object Profile : Screen("profile")
    object CreatePost : Screen("create_post")
    object Comments : Screen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
    object PublicProfile : Screen("user/{username}") {
        fun createRoute(username: String) = "user/$username"
    }
    object Explore : Screen("explore")
    object Messages : Screen("messages")
    object Conversation : Screen("conversation/{convId}") {
        fun createRoute(convId: String) = "conversation/$convId"
    }
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Connect.route, "Connect", Icons.Filled.People, Icons.Outlined.People),
    BottomNavItem(Screen.Ai.route, "AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun SochNavigation(
    repository: SochRepository,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by repository.isLoggedIn.collectAsState()
    val hasCompletedProfile by repository.hasCompletedProfile.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isMainTab = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

    val startDestination = remember {
        if (!isLoggedIn) Screen.Auth.route
        else if (!hasCompletedProfile) Screen.Onboarding.route
        else Screen.Home.route
    }

    Box(modifier = Modifier.fillMaxSize().background(SochBackground)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    repository = repository,
                    onAuthSuccess = { profileComplete ->
                        if (profileComplete) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                ProfileSetupScreen(
                    repository = repository,
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onCreatePostClick = { navController.navigate(Screen.CreatePost.route) },
                    onCommentClick = { postId -> navController.navigate(Screen.Comments.createRoute(postId)) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onUserClick = { username -> navController.navigate(Screen.PublicProfile.createRoute(username)) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(Screen.Connect.route) {
                ConnectScreen(
                    repository = repository,
                    onUserClick = { username -> navController.navigate(Screen.PublicProfile.createRoute(username)) }
                )
            }

            composable(Screen.Ai.route) {
                AiScreen(repository = repository)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    repository = repository,
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onCommentClick = { postId -> navController.navigate(Screen.Comments.createRoute(postId)) }
                )
            }

            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    repository = repository,
                    onDismiss = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Comments.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                CommentsScreen(
                    postId = postId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onUserClick = { username -> navController.navigate(Screen.PublicProfile.createRoute(username)) }
                )
            }

            composable(
                route = Screen.PublicProfile.route,
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                PublicUserProfileScreen(
                    username = username,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onCommentClick = { postId -> navController.navigate(Screen.Comments.createRoute(postId)) },
                    onMessageClick = { convId -> navController.navigate(Screen.Conversation.createRoute(convId)) }
                )
            }

            composable(Screen.Explore.route) {
                ExploreScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onUserClick = { username -> navController.navigate(Screen.PublicProfile.createRoute(username)) },
                    onCommentClick = { postId -> navController.navigate(Screen.Comments.createRoute(postId)) }
                )
            }

            composable(Screen.Messages.route) {
                MessagesScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onConversationClick = { convId -> navController.navigate(Screen.Conversation.createRoute(convId)) }
                )
            }

            composable(
                route = Screen.Conversation.route,
                arguments = listOf(navArgument("convId") { type = NavType.StringType })
            ) { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("convId") ?: ""
                ConversationScreen(
                    conversationId = convId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onUserClick = { username -> navController.navigate(Screen.PublicProfile.createRoute(username)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Floating/Docked Modern Bottom Navigation Bar (Shown on main tabs)
        if (isMainTab) {
            Surface(
                color = SochSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 18.dp)
                    .border(1.dp, SochBorder, RoundedCornerShape(24.dp))
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BOTTOM_NAV_ITEMS.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SochPrimary,
                                unselectedIconColor = SochTextMuted,
                                selectedTextColor = SochPrimary,
                                unselectedTextColor = SochTextMuted,
                                indicatorColor = SochPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    }
}
