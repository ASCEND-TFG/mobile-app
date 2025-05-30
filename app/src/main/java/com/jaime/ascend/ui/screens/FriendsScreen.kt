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
import com.jaime.ascend.ui.components.ActionBarFriendsScreen
import com.jaime.ascend.ui.theme.AppTheme

@Composable
fun FriendsScreen(navController: NavController) {
    Scaffold(
        topBar = { ActionBarFriendsScreen(navController = navController, modifier = Modifier) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text("Friends Screen")
            }
        }
    )
}