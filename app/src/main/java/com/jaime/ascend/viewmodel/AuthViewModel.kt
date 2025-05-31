import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.jaime.ascend.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean) -> Unit) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun sendEmailVerification()  {
        Firebase.auth.currentUser?.sendEmailVerification()
    }

    fun checkEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false)
            return
        }
        authRepository.signIn(email, password) { success ->
            callback(success)
        }
    }

    fun signUp(email: String, password: String, username: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepository.signUp(email, password, username) { success ->
                onComplete(success)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}