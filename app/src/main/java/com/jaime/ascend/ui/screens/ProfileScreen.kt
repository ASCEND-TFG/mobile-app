package com.jaime.ascend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jaime.ascend.R
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.ui.components.ActionBarProfileScreen
import com.jaime.ascend.ui.components.CategoryCard
import com.jaime.ascend.ui.components.HealthProgressBar
import com.jaime.ascend.ui.components.RadarChartView
import com.jaime.ascend.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
) {
    val currentLanguage = LocalConfiguration.current.locale.language
    val username by viewModel.username.observeAsState("")
    val avatarId by viewModel.avatarId.observeAsState(0)
    val showAvatarDialog = remember { mutableStateOf(false) }
    val avatarUrl = remember(avatarId, username) {
        if (avatarId > 0) {
            viewModel.getAvatarUrl(avatarId)
        } else {
            viewModel.getAvatarInitialUrl(username)
        }
    }
    val categories by viewModel.categories.observeAsState(emptyMap<String, Category>())
    val improvedCategories = remember(categories) {
        categories.values.filter { it.level > 1 }
    }

    Scaffold(
        topBar = { ActionBarProfileScreen(navController = navController, modifier = Modifier) },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de perfil
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.avatar_de, username),
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable { showAvatarDialog.value = true },
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            HealthProgressBar(
                                currentLife = viewModel.currentLife.value ?: 0,
                                maxLife = viewModel.maxLife.value ?: 0,
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
                                    text = "${viewModel.coins.value ?: 0}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Radar Chart
                item {
                    Text(
                        text = stringResource(R.string.radar_chart_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    RadarChartView(
                        categories = categories,
                        modifier = Modifier
                            .fillMaxSize()
                            .height(400.dp)

                    )
                }

                // Título de categorías
                item {
                    Text(
                        text = stringResource(R.string.categories),
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (improvedCategories.isEmpty()) {
                            stringResource(R.string.no_improved_categories)
                        } else {
                            stringResource(R.string.improved_categories, improvedCategories.size)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Lista de categorías
                items(categories.values.toList()) { category ->
                    CategoryCard(category = category, language = currentLanguage)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    )

    if (showAvatarDialog.value) {
        AvatarSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showAvatarDialog.value = false }
        )
    }
}

@Composable
fun AvatarSelectionDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
) {
    val randomAvatars by viewModel.randomAvatars.observeAsState(emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.select_your_avatar))
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.updateAvatar(0)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.reset_to_initial),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                content = {
                    items(randomAvatars) { avatarId ->
                        val avatarUrl = viewModel.getAvatarUrl(avatarId)
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar $avatarId",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        viewModel.updateAvatar(avatarId)
                                    }
                                    onDismiss()
                                }
                        )
                    }
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.generateRandomAvatars()
                }
            ) {
                Text(stringResource(R.string.generate_new))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}