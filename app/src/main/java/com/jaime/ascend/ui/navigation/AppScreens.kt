package com.jaime.ascend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisabledByDefault
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreens(val route: String, open val icon: ImageVector) {
    object LoginScreen : AppScreens("login", Icons.Filled.DisabledByDefault)
    object SignupScreen : AppScreens("signup", Icons.Filled.DisabledByDefault)
    object SplashScreen : AppScreens("splash", Icons.Filled.DisabledByDefault)
    object SettingsScreen : AppScreens("settings", Icons.Filled.DisabledByDefault)


    //BottomNavigation Screens
    object HomeScreen : AppScreens("home", Icons.Filled.Home) {
        override val icon: ImageVector get() = Icons.Filled.Home
    }

    object ProfileScreen : AppScreens("profile", Icons.Filled.Person) {
        override val icon: ImageVector get() = Icons.Filled.Person
    }

    object ShopScreen : AppScreens("shop", Icons.Filled.ShoppingCart) {
        override val icon: ImageVector get() = Icons.Filled.ShoppingCart
    }

    object FriendsScreen : AppScreens("friends", Icons.Filled.People) {
        override val icon: ImageVector get() = Icons.Filled.People
    }
}