package com.jaime.ascend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.FriendRequestViewModelFactory
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import com.jaime.ascend.ui.components.ActionBarFriendsScreen
import com.jaime.ascend.ui.components.FriendItem
import com.jaime.ascend.viewmodel.FriendRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FriendRequestViewModel = viewModel(
        factory = FriendRequestViewModelFactory(context, FriendRequestRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            functions = FirebaseFunctions.getInstance(),
            messaging = FirebaseMessaging.getInstance()
        ), UserRepository())
    )
    val friendsList by viewModel.friendsList.collectAsState()
    val loading by viewModel.loading.collectAsState()


    // Estado para el menú desplegable
    var expanded by remember { mutableStateOf(false) }
    // Estado para la opción seleccionada
    var selectedOption by remember { mutableIntStateOf(0) }

    // Función para ordenar la lista según la opción seleccionada
    val sortedFriendsList = remember(friendsList, selectedOption) {
        when (selectedOption) {
            0 -> friendsList.sortedBy { it["username"]?.toString()?.lowercase() }
            1 -> friendsList.sortedByDescending { it["username"]?.toString()?.lowercase() }
            2 -> friendsList.sortedByDescending { (it["coins"] as? Number)?.toInt() ?: 0 }
            3 -> friendsList.sortedBy { (it["coins"] as? Number)?.toInt() ?: 0 }
            4 -> friendsList.sortedByDescending { (it["currentLife"] as? Number)?.toInt() ?: 0 }
            5 -> friendsList.sortedBy { (it["currentLife"] as? Number)?.toInt() ?: 0 }
            else -> friendsList
        }
    }

    val options = listOf(
        stringResource(R.string.username_a_z),
        stringResource(R.string.username_z_a),
        stringResource(R.string.more_coins),
        stringResource(R.string.less_coins),
        stringResource(R.string.more_life),
        stringResource(R.string.less_life)
    )

    Scaffold(
        topBar = { ActionBarFriendsScreen(navController = navController, modifier = Modifier) },
        content = { innerPadding ->

            Column(modifier = Modifier.padding(innerPadding).padding(top = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        TextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = options[selectedOption],
                            onValueChange = {},
                            label = { Text(stringResource(R.string.sort_by)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.username_a_z)) },
                                onClick = {
                                    selectedOption =0
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.username_z_a)) },
                                onClick = {
                                    selectedOption = 1
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.more_coins)) },
                                onClick = {
                                    selectedOption =2
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.less_coins)) },
                                onClick = {
                                    selectedOption = 3
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.more_life)) },
                                onClick = {
                                    selectedOption = 4
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.less_life)) },
                                onClick = {
                                    selectedOption =5
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(sortedFriendsList) { friend ->
                            FriendItem(
                                friend = friend
                            )
                        }
                    }
                }
            }
        }
    )
}
