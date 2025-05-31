package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.jaime.ascend.data.factory.FriendRequestViewModelFactory
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import com.jaime.ascend.ui.components.ActionBarFriendsScreen
import com.jaime.ascend.ui.components.FriendItem
import com.jaime.ascend.viewmodel.FriendRequestViewModel

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

    Scaffold(
        topBar = { ActionBarFriendsScreen(navController = navController, modifier = Modifier) },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(friendsList) { friend ->
                            FriendItem(
                                friend = friend,
                            )
                        }
                    }
                }
            }
        })
}