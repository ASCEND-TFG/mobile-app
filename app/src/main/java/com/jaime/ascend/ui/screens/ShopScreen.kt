package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.ui.components.BottomNavigation
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme

@Composable
fun ShopScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigation(navController, setOf(
            AppScreens.HomeScreen,
            AppScreens.FriendsScreen,
            AppScreens.ShopScreen,
            AppScreens.ProfileScreen
        )) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text("Shop Screen")
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewShopScreen() {
    AppTheme {
        ShopScreen(navController = rememberNavController())
    }
}