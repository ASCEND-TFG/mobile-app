package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.FriendRequestViewModelFactory
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.components.FriendRequestDialog
import com.jaime.ascend.ui.components.PendingRequestItem
import com.jaime.ascend.viewmodel.FriendRequestUiState
import com.jaime.ascend.viewmodel.FriendRequestViewModel
import kotlinx.coroutines.launch

@Composable
fun FriendsRequestScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FriendRequestViewModel = viewModel(
        factory = FriendRequestViewModelFactory(context, FriendRequestRepository(), UserRepository())
    )
    val uiState by viewModel.uiState.collectAsState()
    val foundUser by viewModel.foundUser.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val pendingRequests by viewModel.pendingRequests.collectAsState()


    LaunchedEffect(uiState) {
        when (uiState) {

            is FriendRequestUiState.UserNotFound -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.user_not_found))
                }
            }

            is FriendRequestUiState.RequestSent -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.request_sended))
                    showDialog = false
                    username = ""
                    viewModel.clearFoundUser()
                }
            }

            is FriendRequestUiState.AlreadyFriends -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.already_friends))
                    showDialog = false
                    username = ""
                    viewModel.clearFoundUser()
                }
            }

            is FriendRequestUiState.RequestAlreadySent -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.request_already_sent))
                    showDialog = false
                    username = ""
                    viewModel.clearFoundUser()
                }
            }

            is FriendRequestUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((uiState as FriendRequestUiState.Error).message)
                }
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ActionBarWithBackButton(
                modifier = Modifier,
                screenName = stringResource(R.string.add_friend),
                navController = navController,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                BlackButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 16.dp, horizontal = 28.dp)
                        .align(Alignment.CenterHorizontally),
                    content = {
                        Row {
                            Icon(

                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(modifier = Modifier.padding(end = 4.dp))

                            Text(
                                text = stringResource(R.string.add_friend),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.friend_requests),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                if (pendingRequests.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_pending_requests),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(pendingRequests) { request ->
                            PendingRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptRequest(request["documentId"].toString()) },
                                onReject = { viewModel.rejectRequest(request["documentId"].toString()) },
                            )
                        }
                    }
                }

                if (showDialog) {
                    FriendRequestDialog(
                        username = username,
                        onUsernameChange = { username = it },
                        isLoading = uiState is FriendRequestUiState.Loading,
                        onSendRequest = { viewModel.sendFriendRequest(username) },
                        onDismiss = {
                            showDialog = false
                            username = ""
                        }
                    )
                }
            }
        }
    )
}