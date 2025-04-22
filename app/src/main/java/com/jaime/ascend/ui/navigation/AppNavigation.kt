package com.jaime.ascend.ui.navigation

import AuthViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.ui.screens.FriendsScreen
import com.jaime.ascend.ui.screens.HomeScreen
import com.jaime.ascend.ui.screens.LoginScreen
import com.jaime.ascend.ui.screens.ProfileScreen
import com.jaime.ascend.ui.screens.SettingsScreen
import com.jaime.ascend.ui.screens.ShopScreen
import com.jaime.ascend.ui.screens.SignupScreen
import com.jaime.ascend.ui.screens.SplashScreenContent

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == AppScreens.LoginScreen.route ||
                currentRoute == AppScreens.SignupScreen.route) {
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                }
            }
        } else {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != AppScreens.LoginScreen.route &&
                currentRoute != AppScreens.SignupScreen.route &&
                currentRoute != AppScreens.SplashScreen.route) {
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.SplashScreen.route
    ) {
        composable(route = AppScreens.SplashScreen.route) {
            SplashScreenContent {
                navController.navigate(
                    if (currentUser != null) AppScreens.HomeScreen.route
                    else AppScreens.LoginScreen.route
                ) {
                    popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                }
            }
        }

        composable(route = AppScreens.LoginScreen.route) {
            LoginScreen(navController = navController)
        }

        composable(route = AppScreens.SignupScreen.route) {
            SignupScreen(navController = navController)
        }

        composable(route = AppScreens.HomeScreen.route) {
            if (currentUser != null) {
                HomeScreen(navController)
            }
        }

        composable(route = AppScreens.FriendsScreen.route) {
            if (currentUser != null) {
                FriendsScreen(navController)
            }
        }

        composable(route = AppScreens.ShopScreen.route) {
            if (currentUser != null) {
                ShopScreen(navController)
            }
        }

        composable(route = AppScreens.ProfileScreen.route) {
            if (currentUser != null) {
                ProfileScreen(navController)
            }
        }

        composable(route = AppScreens.SettingsScreen.route) {
            if (currentUser != null) {
                SettingsScreen(navController)
            }
        }
    }
}