package com.jaime.ascend.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

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

    fun signUp(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                    Log.d("AuthRepository", "Sign up successful")
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        callback(false) // Weak password
                        Log.e("AuthRepository", "Sign up failed: ${e.message}")
                    } catch (e: FirebaseAuthUserCollisionException) {
                        callback(false) // Email already in use
                        Log.e("AuthRepository", "Sign up failed: ${e.message}")
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        callback(false) // Invalid email format
                        Log.e("AuthRepository", "Sign up failed: ${e.message}")
                    } catch (e: Exception) {
                        callback(false) // Others
                        Log.e("AuthRepository", "Sign up failed: ${e.message}", e)
                    }
                }
            }
    }

}