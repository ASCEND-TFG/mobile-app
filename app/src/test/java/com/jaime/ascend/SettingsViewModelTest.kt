import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.viewmodel.SettingsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Unit tests for the [SettingsViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: SettingsViewModel
    private lateinit var user: FirebaseUser
    private lateinit var collection: CollectionReference
    private lateinit var document: DocumentReference

    /**
     * Sets up the test environment.
     */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0

        auth = mockk()
        firestore = mockk()
        user = mockk()
        collection = mockk()
        document = mockk()

        viewModel = SettingsViewModel(auth, firestore)
    }

    /**
     * Tears down the test environment.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Tests the deletion of a user account.
     */
    @Test
    fun `deleteUserAccount fails if user delete throws`() = runTest(testDispatcher) {
        every { auth.currentUser } returns user
        every { user.uid } returns "testUid"

        every { firestore.collection("users") } returns collection
        every { collection.document("testUid") } returns document

        every { document.delete() } returns mockSuccessfulTask()
        every { user.delete() } returns createFailedTask(Exception("Auth error"))

        var failureCalled = false

        viewModel.deleteUserAccount(
            onSuccess = { fail("No debe llamar onSuccess en fallo") },
            onFailure = {
                failureCalled = true
                assertTrue(it.message?.contains("Auth error") == true)
            }
        )

        testScheduler.advanceUntilIdle()

        assertTrue(failureCalled)
    }

    /**
     * Helper method to create a successful task.
     */
    private fun <T> mockSuccessfulTask(): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            val listener = arg<(Task<T>) -> Unit>(0)
            listener.invoke(task)
            task
        }
        return task
    }

    /**
     * Helper method to create a failed task.
     */
    private fun <T> createFailedTask(exception: Exception): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns false
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            val listener = arg<(Task<T>) -> Unit>(0)
            listener.invoke(task)
            task
        }
        return task
    }
}
