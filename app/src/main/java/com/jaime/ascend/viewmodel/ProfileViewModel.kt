package com.jaime.ascend.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaime.ascend.data.models.Category
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * ViewModel for the profile screen.
 * It allows the user to edit the profile.
 * @author Jaime Martínez Fernández
 * @param application The application context
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var userListener: ListenerRegistration? = null

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _currentLife = MutableLiveData<Int>()
    val currentLife: LiveData<Int> = _currentLife

    private val _maxLife = MutableLiveData<Int>()
    val maxLife: LiveData<Int> = _maxLife

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    private val _currentExp = MutableLiveData<Int>()
    val currentExp: LiveData<Int> = _currentExp

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    private val _avatarId = MutableLiveData<Int>()
    val avatarId: LiveData<Int> = _avatarId

    private val _randomAvatars = MutableLiveData<List<Int>>()
    val randomAvatars: LiveData<List<Int>> = _randomAvatars

    private val _categories = MutableLiveData<Map<String, Category>>()
    val categories: LiveData<Map<String, Category>> = _categories

    init {
        setupFirestoreListener()
        generateRandomAvatars()
    }

    /**
     * Gets the URL of the avatar with the given ID.
     * @param avatarId The ID of the avatar
     */
    fun getAvatarUrl(avatarId: Int): String {
        return "https://avatar.iran.liara.run/public/$avatarId"
    }

    /**
     * Gets the initial URL of the avatar with the given username.
     * @param username The username of the avatar
     */
    fun getAvatarInitialUrl(username: String): String {
        return "https://avatar.iran.liara.run/username?username=$username"
    }

    /**
     * Sets up the firestore listener.
     * @throws Exception if there is an error setting up the listener
     */
    private fun setupFirestoreListener() {
        if (userId.isEmpty()) {
            Log.e("ProfileViewModel", "User ID is empty")
            return
        }

        viewModelScope.launch {
            val userDocRef = firestore.collection("users").document(userId)
            val categoriesRef = firestore.collection("categories")
            userListener = userDocRef.addSnapshotListener {snapshot, error ->
                if (error != null) {
                    Log.e("ProfileViewModel", "Listener failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userCategories = snapshot.get("categories") as? Map<String, Map<String, Any>> ?: mapOf()
                    val categoriesList = mutableListOf<Category>()

                    categoriesRef.get().addOnSuccessListener { categoriesSnapshot ->
                        categoriesSnapshot.documents.forEach { categoryDoc ->
                            val categoryData = categoryDoc.data ?: return@forEach
                            val categoryId = categoryDoc.id
                            val userCategoryData = userCategories[categoryId] ?: return@forEach

                            categoriesList.add(
                                Category(
                                    id = categoryId,
                                    name = (categoryData["name"] as? Map<String, String>) ?: mapOf(),
                                    description = (categoryData["description"] as? Map<String, String>) ?: mapOf(),
                                    icon = categoryData["icon"] as? String ?: "",
                                    level = (userCategoryData["level"] as? Long)?.toInt() ?: 1,
                                    currentExp = (userCategoryData["currentExp"] as? Long)?.toInt() ?: 0,
                                    neededExp = (userCategoryData["neededExp"] as? Long)?.toInt() ?: 150
                                )
                            )
                        }

                        _categories.value = categoriesList.associateBy { it.id }
                        Log.i("TAG", "setupFirestoreListener categories: ${_categories.value}")
                    }

                    _username.value = snapshot.getString("username") ?: ""
                    _currentLife.value = snapshot.getLong("currentLife")?.toInt() ?: 0
                    _maxLife.value = snapshot.getLong("maxLife")?.toInt() ?: 0
                    _coins.value = snapshot.getLong("coins")?.toInt() ?: 0
                    _avatarId.value = snapshot.getLong("avatarId")?.toInt() ?: 0
                } else {
                    Log.d("ProfileViewModel", "Current data: null")
                }
            }
        }
    }

    /**
     * Generates a list of random avatars.
     */
    fun generateRandomAvatars() {
        val randomAvatars = mutableListOf<Int>()
        while (randomAvatars.size < 6) {
            val randomNum = Random.nextInt(1, 100)
            if (!randomAvatars.contains(randomNum)) {
                randomAvatars.add(randomNum)
            }
        }
        _randomAvatars.value = randomAvatars
    }

    /**
     * Updates the avatar of the user.
     * @param newAvatarId The new avatar ID
     */
    suspend fun updateAvatar(newAvatarId: Int) {
        try {
            firestore.collection("users").document(userId)
                .update("avatarId", newAvatarId)
                .await()
            _avatarId.value = newAvatarId
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error updating avatar", e)
        }
    }

    /**
     * Cleans up the listener when the ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }
}