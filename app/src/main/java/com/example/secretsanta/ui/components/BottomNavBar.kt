package com.example.secretsanta.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.secretsanta.ui.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object SecretSantas : BottomNavItem(
        Screen.SecretSantaList.route,
        Icons.Default.CardGiftcard,
        "Secret Santas"
    )
    object Messaging : BottomNavItem(
        Screen.Messaging.route,
        Icons.Default.Message,
        "Messagerie"
    )
    object Calendar : BottomNavItem(
        Screen.Calendar.route,
        Icons.Default.CalendarToday,
        "Calendrier"
    )
    object Profile : BottomNavItem(
        Screen.Profile.route,
        Icons.Default.Person,
        "Profil"
    )
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.SecretSantas,
        BottomNavItem.Messaging,
        BottomNavItem.Calendar,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}