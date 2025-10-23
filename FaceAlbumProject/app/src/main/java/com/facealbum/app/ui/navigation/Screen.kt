package com.facealbum.app.ui.navigation

/**
 * Navigation destinations for the app
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Suggestions : Screen("suggestions")
    data object PersonDetail : Screen("person/{personId}") {
        fun createRoute(personId: Long) = "person/$personId"
    }
    data object PhotoDetail : Screen("photo/{photoId}") {
        fun createRoute(photoId: Long) = "photo/$photoId"
    }
    data object Settings : Screen("settings")
}
