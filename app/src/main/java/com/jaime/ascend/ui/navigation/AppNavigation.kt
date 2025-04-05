package com.jaime.ascend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.ui.screens.FriendsScreen
import com.jaime.ascend.ui.screens.HomeScreen
import com.jaime.ascend.ui.screens.LoginScreen
import com.jaime.ascend.ui.screens.ProfileScreen
import com.jaime.ascend.ui.screens.ShopScreen
import com.jaime.ascend.ui.screens.SignupScreen
import com.jaime.ascend.ui.screens.SplashScreenContent

@Composable
fun AppNavigation(navController: NavController) {
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
        composable(route = AppScreens.FriendsScreen.route) {
            FriendsScreen(navController)
        }
        composable(route = AppScreens.ShopScreen.route) {
            ShopScreen(navController)
        }
        composable(route = AppScreens.ProfileScreen.route) {
            ProfileScreen(navController)
        }
    }
}