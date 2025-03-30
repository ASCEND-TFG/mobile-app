package com.jaime.ascend.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true)
                Log.d("AuthRepository", "Sign in successful")
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    // Invalid credentials, show error message to user
                    callback(false)
                    Log.e("AuthRepository", "Sign in failed: ${e.message}")
                } catch (e: Exception) {
                    // Other errors, capture and handle properly
                    callback(false)
                    Log.e("AuthRepository", "Sign in failed: ${e.message}", e)
                }
            }
        }
    }

    fun signUp(email: String, password: String, username: String, callback: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val uid = user?.uid ?: ""
                val userDocRef = firestore.collection("users").document(uid)
                val userData = mapOf(
                    "username" to username,
                    "coins" to 0,
                    "maxLife" to 100,
                    "currentLife" to 100,
                    "friends" to emptyList<String>(),
                    "ghabits" to emptyList<String>(),
                    "bhabits" to emptyList<String>(),
                    "categories" to mapOf(
                        "mental_health" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "physic_health" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "finances" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "family" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "couple" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "social" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "career_studies" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        ),
                        "self_care" to mutableMapOf<String, Any>(
                            "level" to 1,
                            "currentExp" to 0,
                            "neededExp" to 150,
                            "nextLevelExp" to calculateNextLevelExp(0)
                        )
                    )
                )
                userDocRef.set(userData).addOnCompleteListener { userDocTask ->
                    callback(userDocTask.isSuccessful)
                }
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthWeakPasswordException) {
                    callback(false) // Weak password
                } catch (e: FirebaseAuthUserCollisionException) {
                    callback(false) // Email already in use
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    callback(false) // Invalid email format
                } catch (e: Exception) {
                    callback(false) // Others
                }
            }
        }
    }

    private fun calculateNextLevelExp(currentExp: Int): Int {
        val level = getLevelFromExp(currentExp)
        return if (level < 10) currentExp + (150 * level) else currentExp
    }

    private fun getLevelFromExp(exp: Int): Int {
        return exp / 150 + 1
    }
}