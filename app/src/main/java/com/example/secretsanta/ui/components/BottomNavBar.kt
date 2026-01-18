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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.example.secretsanta.R

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    object SecretSantas : BottomNavItem(
        Screen.SecretSantaList.route,
        Icons.Default.CardGiftcard,
        R.string.menu_home
    )

    object Wishlist : BottomNavItem(
        route = Screen.Wishlist.route,
        icon = Icons.Default.List,
        labelRes = R.string.menu_wishlist
    )

    object Messaging : BottomNavItem(
        Screen.Messaging.route,
        Icons.Default.Message,
        R.string.menu_messages
    )

    object Calendar : BottomNavItem(
        Screen.Calendar.route,
        Icons.Default.CalendarToday,
        R.string.menu_calendar
    )

    object Profile : BottomNavItem(
        Screen.Profile.route,
        Icons.Default.Person,
        R.string.menu_profile
    )

}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.SecretSantas,
        BottomNavItem.Wishlist,
        BottomNavItem.Messaging,
        BottomNavItem.Calendar,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(stringResource(item.labelRes))
                },


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