package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.jaime.ascend.data.factory.ShopViewModelFactory
import com.jaime.ascend.data.models.Moment
import com.jaime.ascend.data.repository.ShopRepository
import com.jaime.ascend.ui.components.ActionBarShopScreen
import com.jaime.ascend.ui.components.MomentCard
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.ShopLocalCache
import com.jaime.ascend.viewmodel.ShopViewModel

@Composable
fun ShopScreen(navController: NavController) {
    val viewModel: ShopViewModel = viewModel(
        factory = ShopViewModelFactory(
            shopRepo = ShopRepository(
                localCache = ShopLocalCache(
                    context = LocalContext.current,

                    ),
                currentContext = LocalContext.current
            ),
            ctx = LocalContext.current
        )
    )
    val moments by viewModel.moments
    val userCoins by viewModel.userCoins
    val daysUntilRefresh by viewModel.daysUntilRefresh
    val isLoading by viewModel.isLoading
    val showResetMessage by viewModel.showResetMessage
    val isUserDead by viewModel.isUserDead
    val revivalChallenge by viewModel.revivalChallenge
    val isShopLocked by viewModel.isShopLocked
    val showRevivalDialog by viewModel.showRevivalDialog

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Scaffold(
        topBar = { ActionBarShopScreen(modifier = Modifier, coins = userCoins) },
        content = { padding ->
            if (isUserDead) {
                DeathScreen(padding, navController)
            } else {
                ShopScreen(padding, moments, userCoins, isShopLocked, viewModel, daysUntilRefresh)
            }
        }
    )

    // Weekly reset dialog
    if (!isUserDead && showResetMessage) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetMessage() },
            title = { Text(stringResource(R.string.new_week)) },
            text = { Text(stringResource(R.string.reset_moments_message)) },
            confirmButton = {
                Button(onClick = { viewModel.dismissResetMessage() }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
private fun ShopScreen(
    padding: PaddingValues,
    moments: List<Moment>,
    userCoins: Int,
    isShopLocked: Boolean,
    viewModel: ShopViewModel,
    daysUntilRefresh: Int
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Weekly moments title
        Text(
            text = stringResource(R.string.this_week_s_moments),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(20.dp))

        // Moments grid
        if (moments.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(moments) { moment ->
                    MomentCard(
                        moment = moment,
                        coins = userCoins,
                        onClick = {
                            if (!isShopLocked) {
                                viewModel.purchaseMoment(moment.id)
                            }
                        }
                    )
                }
            }
        } else {
            Text(
                stringResource(R.string.loading_moments),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Refresh countdown
        Text(
            text = stringResource(R.string.new_moments_in_days, daysUntilRefresh),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}