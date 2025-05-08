package com.jaime.ascend.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.jaime.ascend.R

sealed class AppScreens(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // Pantallas sin BottomNavigation
    object LoginScreen : AppScreens(
        route = "login",
        title = R.string.login_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object SignupScreen : AppScreens(
        route = "signup",
        title = R.string.signup_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object SplashScreen : AppScreens(
        route = "splash",
        title = R.string.app_name,
        icon = Icons.Filled.DisabledByDefault
    )

    object SettingsScreen : AppScreens(
        route = "settings",
        title = R.string.settings_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object GoodHabitsScreen : AppScreens(
        route = "goodHabits",
        title = R.string.ghabits_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object BadHabitsScreen : AppScreens(
        route = "badHabits",
        title = R.string.bhabits_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object AddNewGoodHabitScreen : AppScreens(
        route = "addNewGoodHabit",
        title = R.string.add_new_good_habit_title,
        icon = Icons.Filled.DisabledByDefault
    )

    object AddingGoodHabitScreen : AppScreens(
        route = "addingGoodHabit/{habitName}/{habitDescription}/{habitCategory}/{habitIcon}",
        arguments = listOf(
            navArgument("habitName") { type = NavType.StringType },
            navArgument("habitDescription") { type = NavType.StringType },
            navArgument("habitCategory") { type = NavType.StringType },
            navArgument("habitIcon") { type = NavType.StringType }
        ),
        title = R.string.add_new_good_habit_title,
        icon = Icons.Filled.DisabledByDefault
    )

    // Pantallas con BottomNavigation
    object HomeScreen : AppScreens(
        route = "home",
        title = R.string.home_title,
        icon = Icons.Filled.Home
    )

    object ProfileScreen : AppScreens(
        route = "profile",
        title = R.string.profile_title,
        icon = Icons.Filled.Person
    )

    object ShopScreen : AppScreens(
        route = "shop",
        title = R.string.shop_title,
        icon = Icons.Filled.ShoppingCart
    )

    object FriendsScreen : AppScreens(
        route = "friends",
        title = R.string.friends_title,
        icon = Icons.Filled.People
    )
}