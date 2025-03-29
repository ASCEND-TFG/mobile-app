package com.jaime.ascend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.ui.screens.HomeScreen
import com.jaime.ascend.ui.screens.LoginScreen
import com.jaime.ascend.ui.screens.SignupScreen
import com.jaime.ascend.ui.screens.SplashScreenContent

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.SplashScreen.route) {
        composable(route = AppScreens.SplashScreen.route) {
            SplashScreenContent {
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                }
            }
        }
        composable(route = AppScreens.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(route = AppScreens.SignupScreen.route) {
            SignupScreen(navController)
        }
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }
    }
}