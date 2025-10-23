package com.facealbum.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.facealbum.app.ui.screens.home.HomeScreen
import com.facealbum.app.ui.screens.persondetail.PersonDetailScreen
import com.facealbum.app.ui.screens.photodetail.PhotoDetailScreen
import com.facealbum.app.ui.screens.settings.SettingsScreen
import com.facealbum.app.ui.screens.suggestions.SuggestionsScreen

@Composable
fun FaceAlbumNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPersonClick = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                },
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                },
                onSuggestionsClick = {
                    navController.navigate(Screen.Suggestions.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Suggestions.route) {
            SuggestionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PersonDetail.route,
            arguments = listOf(
                navArgument("personId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: return@composable
            PersonDetailScreen(
                personId = personId,
                onBackClick = { navController.popBackStack() },
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                }
            )
        }
        
        composable(
            route = Screen.PhotoDetail.route,
            arguments = listOf(
                navArgument("photoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: return@composable
            PhotoDetailScreen(
                photoId = photoId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
