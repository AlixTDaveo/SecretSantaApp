package com.example.secretsanta.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.secretsanta.ui.components.BottomNavBar
import com.example.secretsanta.ui.feature.auth.login.LoginScreen
import com.example.secretsanta.ui.feature.auth.register.RegisterScreen
import com.example.secretsanta.ui.feature.calendar.CalendarScreen
import com.example.secretsanta.ui.feature.messaging.MessagingScreen
import com.example.secretsanta.ui.feature.profile.EditProfileScreen
import com.example.secretsanta.ui.feature.profile.ProfileScreen
import com.example.secretsanta.ui.feature.secretsanta.create.CreateSecretSantaScreen
import com.example.secretsanta.ui.feature.secretsanta.details.SecretSantaDetailsScreen
import com.example.secretsanta.ui.feature.secretsanta.list.SecretSantaListScreen
// import com.example.secretsanta.ui.feature.wishlist.WishlistScreen  // ← SUPPRIMÉ

@Composable
fun NavGraph(startDestination: String = Screen.Login.route) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithBottomNav = listOf(
        Screen.SecretSantaList.route,
        // Screen.Wishlist.route,  // ← SUPPRIMÉ
        Screen.Messaging.route,
        Screen.Calendar.route,
        Screen.Profile.route
    )

    val showBottomNav = currentRoute in routesWithBottomNav

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.SecretSantaList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SecretSantaList.route) {
                SecretSantaListScreen(navController)
            }

            // composable(Screen.Wishlist.route) {  // ← SUPPRIMÉ
            //     WishlistScreen()
            // }

            composable(Screen.Messaging.route) {
                MessagingScreen()
            }

            composable(Screen.Calendar.route) {
                CalendarScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }


            composable(Screen.CreateSecretSanta.route) {
                CreateSecretSantaScreen(navController)
            }

            composable(
                route = Screen.SecretSantaDetails.route,
                arguments = listOf(
                    navArgument("santaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val santaId = backStackEntry.arguments?.getString("santaId") ?: ""
                SecretSantaDetailsScreen(navController, santaId)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}