package com.jaime.ascend.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import com.jaime.ascend.viewmodel.FriendRequestViewModel

/**
 * Factory class for creating instances of [FriendRequestViewModel].
 * @author Jaime Martínez Fernández
 */
class FriendRequestViewModelFactory(
    private val ctx: Context,
    private val friendRepo: FriendRequestRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRequestViewModel::class.java)) {
            return FriendRequestViewModel(context = ctx, friendRequestRepository = friendRepo, userRepository = userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}