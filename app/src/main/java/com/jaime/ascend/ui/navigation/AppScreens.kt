package com.jaime.ascend.ui.navigation

sealed class AppScreens (val route: String) {
    object LoginScreen: AppScreens("login")
    object SignupScreen: AppScreens("signup")
    object HomeScreen: AppScreens("home")

}