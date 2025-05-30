package com.jaime.ascend.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.jaime.ascend.R
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.models.Moment
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.IconMapper
import com.jaime.ascend.viewmodel.ProfileViewModel
import com.jaime.ascend.viewmodel.RewardsViewModel
import com.jaime.ascend.viewmodel.UserViewModel
import kotlinx.coroutines.launch
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
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        )
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
fun ActionBarFriendsScreen(
    navController: NavController,
    modifier: Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ascendlogo_removebg),
                contentDescription = R.string.app_name.toString(),
                modifier = Modifier
                    .size(120.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            BlackButton(
                onClick = { navController.navigate(AppScreens.FriendsRequestScreen.route) },
                modifier = Modifier
                    .height(35.dp)
                    .width(135.dp),
                content = {
                    Text(
                        text = stringResource(R.string.add_friend),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)
    }
}

@Composable
fun ActionBarShopScreen(
    viewModel: UserViewModel = viewModel(),
    coins: Int,
    modifier: Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ascendlogo_removebg),
                contentDescription = R.string.app_name.toString(),
                modifier = Modifier
                    .size(120.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = coins.toString(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Filled.Paid,
                contentDescription = stringResource(R.string.configuration_icon_content),
                modifier = Modifier
                    .size(24.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)
    }
}

@Composable
fun FriendItem(
    friend: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val username = friend["username"].toString()
    val avatarUrl = friend["avatarId"].toString()
    val currentLife = (friend["currentLife"] as? Number)?.toInt() ?: 0
    val maxLife = (friend["maxLife"] as? Number)?.toInt() ?: 100
    val coins = (friend["coins"] as? Number)?.toInt() ?: 0
    val profileViewModel: ProfileViewModel = viewModel()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (avatarUrl == "0") {
                    profileViewModel.getAvatarInitialUrl(username)
                } else {
                    profileViewModel.getAvatarUrl(avatarUrl.toInt())
                },
                contentDescription = stringResource(R.string.avatar_de, username),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(2.dp))

                HealthProgressBar(
                    currentLife = currentLife,
                    maxLife = maxLife,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Paid,
                        contentDescription = stringResource(R.string.coins),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$coins",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ActionBarWithBackButton(
    modifier: Modifier = Modifier,
    screenName: String,
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() },
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
                    .clickable { onBack() }
            )

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
    modifier: Modifier = Modifier,
    selectedDays: List<Int>,
    onDaySelected: (Int) -> Unit,
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
fun HealthProgressBar(
    currentLife: Int,
    maxLife: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (maxLife > 0) currentLife.toFloat() / maxLife.toFloat() else 0f
    val progressWidth = progress.coerceIn(0f, 1f)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val startColor = when {
        progress < 0.3f -> Color.Red
        progress < 0.6f -> Color.Yellow
        else -> Color.Green
    }

    val endColor = when {
        progress < 0.3f -> Color(0xFFFFA500)
        progress < 0.6f -> Color(0xFFFFEB3B)
        else -> Color(0xFF4CAF50)
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.life, currentLife, maxLife),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            drawRect(
                color = surfaceVariant,
                size = size
            )

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(startColor, endColor),
                    startX = 0f,
                    endX = size.width * progressWidth
                ),
                topLeft = Offset.Zero,
                size = size.copy(width = size.width * progressWidth)
            )
        }
    }
}

@Composable
fun RewardSection(
    coinReward: Int,
    xpReward: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.coins),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier) {
                Text(
                    text = "+$coinReward",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.width(3.dp))

                Icon(
                    imageVector = Icons.Filled.Paid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }


        }
        Column {
            Text(
                text = stringResource(R.string.experience),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "+${xpReward} XP",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun BadHabitCard(
    habit: BadHabit,
    onHabitClick: (BadHabit) -> Unit,
    viewModel: RewardsViewModel
) {
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(habit.completed) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(habit.completed) {
        isChecked = habit.completed
    }

    LaunchedEffect(habit.template) {
        if (template == null && habit.template != null) {
            isLoading = true
            template = habit.template.get().await()
                .toObject(HabitTemplate::class.java)
            isLoading = false
        }
    }

    LaunchedEffect(habit.category) {
        if (category == null && habit.category != null) {
            isLoading = true
            category = habit.category.get().await()
                .toObject(Category::class.java)
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onHabitClick(habit) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.loading))
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    template?.icon?.let { icon ->
                        Icon(
                            imageVector = IconMapper.getHabitIcon(icon),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = template?.getName(Locale.getDefault()) ?: "Unnamed Habit",
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
                                    text = "-${habit.lifeLoss}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Filled.HeartBroken,
                                    contentDescription = "Vida perdida",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category?.getName(Locale.getDefault()) ?: "Unnamed Category",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Checkbox(
                        checked = habit.completed,
                        onCheckedChange = { newCheckedState ->
                            isChecked = newCheckedState
                            coroutineScope.launch {
                                viewModel.toggleBadHabitCompleted(habit, newCheckedState)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoodHabitCard(
    habit: GoodHabit,
    onHabitClick: (GoodHabit) -> Unit,
    viewModel: RewardsViewModel
) {
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(habit.template) {
        if (template == null && habit.template != null) {
            isLoading = true
            template = habit.template.get().await()
                .toObject(HabitTemplate::class.java)
            isLoading = false
        }
    }

    LaunchedEffect(habit.category) {
        if (category == null && habit.category != null) {
            isLoading = true
            category = habit.category.get().await()
                .toObject(Category::class.java)
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onHabitClick(habit) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.loading))
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    template?.icon?.let { icon ->
                        Icon(
                            imageVector = IconMapper.getHabitIcon(icon),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = template?.getName(Locale.getDefault()) ?: "Unnamed Habit",
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
                                    text = "+${habit.coinReward}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Filled.Paid,
                                    contentDescription = "Monedas ganadas",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "+${habit.xpReward} XP",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category?.getName(Locale.getDefault()) ?: "Unnamed Category",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Checkbox(
                        checked = habit.completed,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                viewModel.toggleGoodHabitCompleted(habit, isChecked)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name[language] ?: category.id,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Lvl. ${category.level}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = category.currentExp.toFloat() / category.neededExp.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${category.currentExp}/${category.neededExp} XP",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = "${(category.currentExp.toFloat() / category.neededExp.toFloat() * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun RadarChartView(
    categories: Map<String, Category>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    var radarTextColor: Color = if (isDarkTheme) Color.White else Color.Black
    val webColor = Color.LightGray.copy(alpha = 0.3f)

    AndroidView(
        factory = { context ->
            RadarChart(context).apply {
                setBackgroundColor(Color.Transparent.toArgb())
                description.isEnabled = false
                legend.isEnabled = false
                isRotationEnabled = false
                setTouchEnabled(false)
                isClickable = false
                webLineWidth = 1f
                webAlpha = 100
                webLineWidthInner = 1f
                webColorInner = webColor.toArgb()

                yAxis.apply {
                    textColor = radarTextColor.toArgb()
                    axisMinimum = 0f
                    axisMaximum = 5f
                    setDrawLabels(false)
                    setDrawAxisLine(false)
                    setDrawGridLines(false)
                }

                xAxis.apply {
                    textColor = radarTextColor.toArgb()
                    textSize = 12f
                    xOffset = 0f
                    yOffset = 0f
                }
            }
        },
        update = { radarChart ->
            val entries = categories.values.map {
                RadarEntry(it.level.toFloat().coerceAtLeast(0.1f))
            }

            val dataSet = RadarDataSet(entries, "").apply {
                color = Color.LightGray.toArgb()
                fillColor = Color.LightGray.copy(alpha = 0.5f).toArgb()
                setDrawFilled(true)
                fillAlpha = 80
                lineWidth = 2f
                isDrawHighlightCircleEnabled = false
                setDrawValues(false)
            }

            radarChart.data = RadarData(dataSet).apply {
                setValueTextSize(12f)
                setValueTextColor(radarTextColor.toArgb())
            }

            radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(
                categories.values.map { it.getName(Locale.getDefault()) }.toList()
            )
            radarChart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun MomentCard(
    moment: Moment,
    coins: Int,
    onClick: () -> Unit
) {
    val language = Locale.getDefault().language

    var showDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = moment.getName(Locale.getDefault().language)) },
            text = {
                Column {
                    Text(
                        text = moment.getDescription(Locale.getDefault().language),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider()

                    Row(
                    ) {
                        Text(
                            text = stringResource(R.string.reward, moment.reward),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onClick()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row() {
                        Text(
                            text = stringResource(R.string.purchase_for, moment.price),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )

                        Icon(
                            imageVector = Icons.Filled.Paid,
                            contentDescription = null,
                            modifier = Modifier.padding(top = 8.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = IconMapper.getMomentIcon(moment.icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = moment.getName(language),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (moment.isOwned) {
                    Text(
                        text = stringResource(R.string.owned),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                } else {
                    BlackButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        content = {
                            Text(
                                text = stringResource(R.string.view_more),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun FriendRequestDialog(
    username: String,
    onUsernameChange: (String) -> Unit,
    isLoading: Boolean,
    onSendRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_friend)) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                if (isLoading) {
                    CircularProgressIndicator(Modifier.padding(8.dp))
                }
            }
        },
        confirmButton = {
            BlackButton(
                onClick = onSendRequest,
                enabled = username.isNotBlank() && !isLoading,
                content = {
                    Text(
                        text = stringResource(R.string.send_request),
                    )
                },
            )
        }
    )
}

@Composable
fun PendingRequestItem(
    request: Map<String, Any>,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = request["username"].toString(),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            Row {
                IconButton(onClick = onReject) {
                    Icon(Icons.Filled.Close, "Rechazar", tint = Color.Red)
                }
                IconButton(onClick = onAccept) {
                    Icon(Icons.Filled.Check, "Aceptar", tint = Color.Green)
                }
            }
        }
    }
}