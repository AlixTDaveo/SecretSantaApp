package com.example.secretsanta.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Main Navigation (avec Bottom Nav)
    object SecretSantaList : Screen("secret_santa_list")
    object Wishlist : Screen("wishlist")
    object Messaging : Screen("messaging")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")

    // Secondary Screens (sans Bottom Nav)
    object CreateSecretSanta : Screen("create_secret_santa")
    object SecretSantaDetails : Screen("secret_santa_details/{santaId}") {
        fun createRoute(santaId: String) = "secret_santa_details/$santaId"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }

}