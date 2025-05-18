package com.jaime.ascend.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.ActionBarProfileScreen
import com.jaime.ascend.ui.components.HealthProgressBar
import com.jaime.ascend.viewmodel.ProfileViewModel
import kotlin.random.Random

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val username by viewModel.username.observeAsState("")
    val showAvatarDialog = remember { mutableStateOf(false) }
    val avatarUrl = remember(username) {
        viewModel.getAvatarInitialUrl(username)
    }

    /*if (showAvatarDialog.value) {
        AvatarSelectionDialog(
            currentAvatar = currentAvatarUrl,
            onDismiss = { showAvatarDialog.value = false },
            onAvatarSelected = { newUrl ->
                viewModel.saveSelectedAvatar(newUrl)
            }
        )
    }*/

    Scaffold(
        topBar = { ActionBarProfileScreen(navController = navController, modifier = Modifier) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar de $username",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        HealthProgressBar(
                            currentLife = viewModel.currentLife.value ?: 0,
                            maxLife = viewModel.maxLife.value ?: 0,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Paid,
                                contentDescription = "Coins",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
        }
    )
}

@Composable
fun AvatarSelectionDialog(
    currentAvatar: String,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    val avatarOptions = remember {
        listOf(
            "random" to "Aleatorio",
            "A" to "Letra A",
            "B" to "Letra B",
            // Añade más opciones según necesites
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar avatar") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(300.dp)
            ) {
                items(avatarOptions) { (option, description) ->
                    val avatarUrl = remember(option) {
                        if (option == "random") {
                            "https://api.placeholder.pics/avatar/200?random=${Random.nextInt(1000)}"
                        } else {
                            "https://api.placeholder.pics/avatar/200/$option"
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                onAvatarSelected(avatarUrl)
                                onDismiss()
                            }
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = description,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}