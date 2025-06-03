package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.viewmodel.RewardsViewModel

/**
 * Factory class for creating instances of [RewardsViewModel].
 * @author Jaime Martínez Fernández
 */
class RewardsViewModelFactory(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {
            return RewardsViewModel(auth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}