package com.example.ai_music_pro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Create : Screen("create", "Create", Icons.Default.Add)
    object Profile : Screen("profile", "Account", Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AlbumDetail : Screen("album", "Album", Icons.Default.MusicNote) {
        const val ARG_ID = "albumId"
        const val ROUTE_WITH_ARGS = "album/{$ARG_ID}"
        fun createRoute(albumId: String) = "album/$albumId"
    }
    object Login : Screen("login", "Login", Icons.Default.Home)
    object Signup : Screen("signup", "Signup", Icons.Default.Home)
    object Room : Screen("room", "Room", Icons.Default.Groups)
    object RecentList : Screen("recent_list", "Recently Played", Icons.Default.History)
    object LikedList : Screen("liked_list", "Liked Songs", Icons.Default.Favorite)
    object TrendingList : Screen("trending_list", "Trending", Icons.Default.Star)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Profile
)

