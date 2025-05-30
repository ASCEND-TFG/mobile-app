package com.jaime.ascend.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.viewmodel.FriendRequestViewModel
import com.jaime.ascend.viewmodel.ShopViewModel

class FriendRequestViewModelFactory(
    private val ctx: Context,
    private val friendRepo: FriendRequestRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRequestViewModel::class.java)) {
            return FriendRequestViewModel(context = ctx, repository = friendRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}