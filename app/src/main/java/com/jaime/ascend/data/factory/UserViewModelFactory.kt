package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.viewmodel.UserViewModel

/**
 * Factory class for creating instances of [UserViewModel].
 * @author Jaime Martínez Fernández
 */
class UserViewModelFactory(
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}