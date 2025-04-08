package com.jaime.ascend.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jaime.ascend.R
import com.jaime.ascend.ui.navigation.AppScreens

@Composable
@Preview(showBackground = true)
fun BlackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit) = { Text("Button") },
) {
    Button(
        onClick = onClick,
        content = content,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}

@Composable
fun BottomNavigation(navController: NavController, screens: Set<AppScreens>) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        screens.forEach { screen ->
            if (screen.route == "home" || screen.route == "friends" || screen.route == "shop" || screen.route == "profile") {
                NavigationBarItem(
                    selected = currentRoute == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(imageVector = screen.icon, contentDescription = null) }
                )
            }
        }
    }
}
@Composable
@Preview(showBackground = true)
fun ActionBar(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ascendlogo_removebg),
            contentDescription = R.string.app_name.toString(),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(120.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)

    }
}