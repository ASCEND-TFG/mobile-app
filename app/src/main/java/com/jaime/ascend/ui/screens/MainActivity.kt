package com.jaime.ascend.ui.screens

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import com.jaime.ascend.ui.components.BottomNavigation
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            AppTheme {
                MainScreenNavHost()
            }
        }
    }
}

@Composable
fun MainScreenNavHost() {
    val navController = rememberNavController()
    val auth = Firebase.auth
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var isNavReady by remember { mutableStateOf(false) }
    val startDestination = AppScreens.SplashScreen.route
    val firestore = FirebaseFirestore.getInstance()
    val categoryRepository = CategoryRepository(firestore)
    val habitRepository = HabitRepository(firestore)

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        isNavReady = true
    }

    if (isNavReady) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute in setOf(
                        AppScreens.HomeScreen.route,
                        AppScreens.FriendsScreen.route,
                        AppScreens.ShopScreen.route,
                        AppScreens.ProfileScreen.route
                    ) && currentUser != null
                ) {
                    BottomNavigation(navController = navController)
                }
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(route = AppScreens.SplashScreen.route) {
                            SplashScreenContent {
                                if (isNavReady) {
                                    navController.navigate(
                                        if (currentUser != null) AppScreens.HomeScreen.route
                                        else AppScreens.LoginScreen.route
                                    ) {
                                        popUpTo(0)
                                    }
                                }
                            }
                        }

                        composable(route = AppScreens.LoginScreen.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    if (isNavReady) {
                                        navController.navigate(AppScreens.HomeScreen.route) {
                                            popUpTo(0)
                                        }
                                    }
                                },
                                navController = navController
                            )
                        }

                        composable(route = AppScreens.SignupScreen.route) {
                            SignupScreen(navController = navController)
                        }

                        composable(route = AppScreens.HomeScreen.route) {
                            if (currentUser != null) HomeScreen(navController)
                        }

                        composable(route = AppScreens.FriendsScreen.route) {
                            if (currentUser != null) FriendsScreen(navController)
                        }

                        composable(route = AppScreens.ShopScreen.route) {
                            if (currentUser != null) ShopScreen(navController)
                        }

                        composable(route = AppScreens.ProfileScreen.route) {
                            if (currentUser != null) ProfileScreen(navController)
                        }

                        composable(route = AppScreens.SettingsScreen.route) {
                            if (currentUser != null) SettingsScreen(navController)
                        }

                        composable(route = AppScreens.GoodHabitsScreen.route) {
                            if (currentUser != null) GoodHabitsScreen(navController)
                        }

                        /*composable(route = AppScreens.BadHabitsScreen.route) {
                            if (currentUser != null) BadHabitsScreen(navController)
                        }*/

                        composable(route = AppScreens.AddNewGoodHabitScreen.route) {
                            AddNewGoodHabitScreen(
                                navController = navController,
                                categoryRepository = categoryRepository,
                                habitRepository = habitRepository,
                                auth = auth
                            )
                        }

                        composable(
                            route = AppScreens.AddingGoodHabitScreen.route,
                            arguments = listOf(
                                navArgument("habitName") { type = NavType.StringType },
                                navArgument("habitDescription") { type = NavType.StringType },
                                navArgument("habitCategory") { type = NavType.StringType },
                                navArgument("habitIcon") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val habitName = backStackEntry.arguments?.getString("habitName") ?: ""
                            val habitDescription =
                                backStackEntry.arguments?.getString("habitDescription") ?: ""
                            val habitCategory =
                                backStackEntry.arguments?.getString("habitCategory") ?: ""
                            val habitIcon = backStackEntry.arguments?.getString("habitIcon") ?: ""

                            AddingGoodHabitScreen(
                                navController = navController,
                                habitName = habitName,
                                habitDescription = habitDescription,
                                habitCategory = habitCategory,
                                habitIcon = habitIcon
                            )
                        }
                    }
                }
            }
        )
    }
}