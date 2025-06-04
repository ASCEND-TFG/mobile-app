import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.jaime.ascend.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * View model for authentication.
 * @author Jaime Martínez Fernández
 */
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val auth = Firebase.auth

    private val _authState = MutableStateFlow<FirebaseUser?>(null)
    val authState: StateFlow<FirebaseUser?> get() = _authState

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    /**
     * Send password reset email.
     * @param email The email.
     * @param onComplete The callback.
     */
    fun sendPasswordResetEmail(email: String, onComplete: (Boolean) -> Unit) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    /**
     * Send email verification.
     * @param onComplete The callback.
     */
    fun sendEmailVerification(onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                user.sendEmailVerification()
                    .addOnCompleteListener { verificationTask ->
                        onComplete(verificationTask.isSuccessful)
                    }
            } else {
                onComplete(false)
            }
        } ?: onComplete(false)
    }

    /**
     * Check if the email is verified.
     * @param callback The callback.
     */
    fun checkEmailVerifiedWithReload(callback: (Boolean) -> Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            callback(auth.currentUser?.isEmailVerified ?: false)
        }
    }

    /**
     * Sign in with email and password.
     * @param email The email.
     * @param password The password.
     * @param callback The callback.
     */
    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false)
            return
        }
        authRepository.signIn(email, password) { success ->
            callback(success)
        }
    }

    /**
     * Sign up with email and password.
     * @param email The email.
     * @param password The password.
     * @param username The username.
     * @param onComplete The callback.
     */
    fun signUp(email: String, password: String, username: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepository.signUp(email, password, username) { success ->
                onComplete(success)
            }
        }
    }

    /**
     * Sign out.
     */
    fun signOut() {
        auth.signOut()
    }
}