package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    init {
        fetchUserName()
        fetchCoins()
    }

    private fun fetchUserName() {
        if (userId.isNotEmpty()) {
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val usernameFromFirestore = documentSnapshot.getString("username") ?: "Username"
                    _userName.value = usernameFromFirestore
                } else {
                    Log.e("UserViewModel", "Couldn't get user username")
                }
            }.addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user username", exception)
            }
        } else {
            Log.e("UserViewModel", "User ID is empty")
        }
    }

    private fun fetchCoins() {
        if (userId.isNotEmpty()) {
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val coinsFromFirestore = documentSnapshot.getLong("coins")?.toInt() ?: 0
                    _coins.value = coinsFromFirestore
                } else {
                    Log.e("UserViewModel", "Couldn't get user coins")
                }
            }.addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user coins", exception)
            }
        } else {
            Log.e("UserViewModel", "User ID is empty")
        }
    }
}