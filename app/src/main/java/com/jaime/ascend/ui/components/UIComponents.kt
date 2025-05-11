package com.jaime.ascend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jaime.ascend.R
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.IconMapper
import com.jaime.ascend.viewmodel.UserViewModel
import kotlinx.coroutines.tasks.await
import java.util.Locale

@Composable
fun BlackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit,
    enabled: Boolean = true,
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled) {
                if (enabled) onClick()
            }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}


@Composable
fun BottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(
        AppScreens.HomeScreen,
        AppScreens.FriendsScreen,
        AppScreens.ShopScreen,
        AppScreens.ProfileScreen
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = stringResource(screen.title)) },
                label = {
                    Text(
                        text = stringResource(screen.title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                selected = currentRoute == screen.route,
                alwaysShowLabel = false,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ActionBar(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start, modifier = Modifier
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

@Composable
fun ActionBarProfileScreen(
    viewModel: UserViewModel = viewModel(), modifier: Modifier, navController: NavController,
) {
    val username by viewModel.userName.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp), horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.width(32.dp))

            Text(
                text = username,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.configuration_icon_content),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.navigate(AppScreens.SettingsScreen.route) })
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)
    }
}

@Composable
fun ActionBarWithBackButton(
    screenName: String, modifier: Modifier = Modifier, navController: NavController,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp), horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = stringResource(R.string.back_icon_content),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() })

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = screenName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp
        )
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: List<Int>,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = listOf(
        stringResource(R.string.monday_initial),
        stringResource(R.string.tuesday_initial),
        stringResource(R.string.wednesday_initial),
        stringResource(R.string.thursday_initial),
        stringResource(R.string.friday_initial),
        stringResource(R.string.saturday_initial),
        stringResource(R.string.sunday_initial)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedDays.contains(index)) {
                            if (isSystemInDarkTheme()) Color.White else Color.Black
                        } else {
                            if (isSystemInDarkTheme()) Color.Transparent else Color.White
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        shape = CircleShape
                    )
                    .clickable { onDaySelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (selectedDays.contains(index)) {
                        if (isSystemInDarkTheme()) Color.Black else Color.White
                    } else {
                        if (isSystemInDarkTheme()) Color.White else Color.Black
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun HabitCard(habit: GoodHabit) {
    // Track template/category loading per card
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Resolve template ONLY when this card appears
    LaunchedEffect(habit.template) {
        if (template == null && habit.template != null) {
            isLoading = true
            template = habit.template.get().await()
                .toObject(HabitTemplate::class.java)
            isLoading = false
        }
    }

    // Resolve category similarly
    LaunchedEffect(habit.category) {
        if (category == null && habit.category != null) {
            isLoading = true
            category = habit.category.get().await()
                .toObject(Category::class.java)
            isLoading = false
        }
    }

    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...")
                }
            } else {
                Text(
                    text = template?.getLocalizedName(Locale.getDefault()) ?: "Unnamed Habit",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Category: ${category?.getName(Locale.getDefault()) ?: "Uncategorized"}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text("Reward: ${habit.xpReward} XP • ${habit.coinReward} coins")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HabitCard2(
    modifier: Modifier = Modifier,
    habitName: String = "Habit super mas largo aun maaaas name",
    categoryName: String = "Category",
    xpReward: Int = 30,
    coinReward: Int = 5,
    iconName: String = "iconName",
) {
    var checked by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = IconMapper.getHabitIcon(iconName),
                contentDescription = null,
            )


            Spacer(modifier = Modifier.width(16.dp))

            Column{
                Text(
                    text = habitName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "+$coinReward",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.Paid,
                            contentDescription = "Monedas ganadas",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "+$xpReward XP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Checkbox(
                modifier = Modifier.weight(.1f),
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
    }
}