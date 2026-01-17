package com.example.secretsanta.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object SecretSantaList : Screen("secret_santa_list")
    object Wishlist : Screen("wishlist")
    object Messaging : Screen("messaging")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object CreateSecretSanta : Screen("create_secret_santa")
    object SecretSantaDetails : Screen("secret_santa_details/{santaId}") {
        fun createRoute(santaId: String) = "secret_santa_details/$santaId"
    }
    object UserWishlist : Screen("user_wishlist/{santaId}/{userId}") {
        fun createRoute(santaId: String, userId: String) = "user_wishlist/$santaId/$userId"
    }
    object AddWishlistItem : Screen("add_wishlist_item")
}