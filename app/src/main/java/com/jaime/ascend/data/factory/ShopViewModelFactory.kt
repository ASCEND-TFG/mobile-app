package com.jaime.ascend.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.ShopRepository
import com.jaime.ascend.viewmodel.ShopViewModel

/**
 * Factory class for creating instances of [ShopViewModel].
 * @author Jaime Martínez Fernández
 */
class ShopViewModelFactory(
    private val ctx: Context,
    private val shopRepo: ShopRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
            return ShopViewModel(ctx = ctx, shopRepo = shopRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}