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

                // Función auxiliar para crear categorías con la estructura correcta
                fun createCategory(initialExp: Int = 0): Map<String, Any> {
                    return mapOf(
                        "currentExp" to initialExp,
                        "level" to 1,
                        "neededExp" to 150,
                    )
                }

                val userData = mapOf(
                    "username" to username,
                    "coins" to 0,
                    "currentLife" to 100,
                    "maxLife" to 100,
                    "friends" to emptyList<String>(),
                    "ghabits" to emptyList<String>(),
                    "bhabits" to emptyList<String>(),
                    "profileImageUrl" to "",
                    "categories" to mapOf(
                        "career_studies" to createCategory(),
                        "couple" to createCategory(),
                        "family" to createCategory(),
                        "finances" to createCategory(),
                        "mental_health" to createCategory(),
                        "physic_health" to createCategory(),
                        "self_care" to createCategory(),
                        "social" to createCategory()
                    ),
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
}