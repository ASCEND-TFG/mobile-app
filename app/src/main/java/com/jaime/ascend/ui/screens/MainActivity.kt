package com.jaime.ascend.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.jaime.ascend.data.repository.BadHabitRepository
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.BottomNavigation
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val auth = Firebase.auth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }


                // Get new FCM registration token
                val token = task.result

                FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid)
                    .update("fcmToken", token)

            })
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        askNotificationPermission()
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
    val goodHabitRepository = GoodHabitRepository(firestore, auth)
    val badHabitRepository = BadHabitRepository(firestore, auth)
    val templateRepository = TemplateRepository(firestore)

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

                        composable(route = AppScreens.BadHabitsScreen.route) {
                            if (currentUser != null) BadHabitsScreen(navController)
                        }

                        composable(route = AppScreens.FriendsRequestScreen.route) {
                            if (currentUser != null) FriendsRequestScreen(navController)
                        }

                        composable(route = AppScreens.AddNewGoodHabitScreen.route) {
                            AddNewGoodHabitScreen(
                                navController = navController,
                                categoryRepository = categoryRepository,
                                habitRepository = goodHabitRepository,
                                templateRepository = templateRepository
                            )
                        }

                        composable(route = AppScreens.AddNewBadHabitScreen.route) {
                            AddNewBadHabitScreen(
                                navController = navController,
                                categoryRepository = categoryRepository,
                                habitRepository = badHabitRepository,
                                auth = auth,
                                templateRepository = templateRepository
                            )
                        }

                        composable(
                            route = AppScreens.AddingGoodHabitScreen.route,
                            arguments = listOf(
                                navArgument("categoryId") { type = NavType.StringType },
                                navArgument("templateId") { type = NavType.StringType },
                            )
                        ) { backStackEntry ->
                            Log.i("TAG", "MainScreenNavHost: ${backStackEntry.arguments}")
                            val templateId =
                                backStackEntry.arguments?.getString("templateId") ?: ""

                            AddingGoodHabitScreen(
                                navController = navController,
                                templateId = templateId
                            )
                        }

                        composable(
                            route = AppScreens.AddingBadHabitScreen.route,
                            arguments = listOf(
                                navArgument("categoryId") { type = NavType.StringType },
                                navArgument("templateId") { type = NavType.StringType },
                            )
                        ) { backStackEntry ->
                            Log.i("TAG", "MainScreenNavHost: ${backStackEntry.arguments}")
                            val templateId =
                                backStackEntry.arguments?.getString("templateId") ?: ""

                            AddingBadHabitScreen(
                                navController = navController,
                                templateId = templateId
                            )
                        }

                        composable(
                            route = AppScreens.GoodHabitsDetailScreen.route,
                            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            GoodHabitDetailScreen(
                                habitId = habitId,
                                navController = navController
                            )
                        }

                        composable(
                            route = AppScreens.BadHabitsDetailScreen.route,
                            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            BadHabitDetailsScreen(
                                habitId = habitId,
                                navController = navController
                            )
                        }

                        composable(
                            route = "edit_habit/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            EditGoodHabitScreen(navController, habitId)
                        }

                        composable(
                            route = "edit_bhabit/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            EditBadHabitScreen(navController, habitId)
                        }
                    }
                }
            }
        )
    }
}