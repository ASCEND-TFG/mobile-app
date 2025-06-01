package com.jaime.ascend.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.viewmodel.DeathViewModel

class DeathViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeathViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeathViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}