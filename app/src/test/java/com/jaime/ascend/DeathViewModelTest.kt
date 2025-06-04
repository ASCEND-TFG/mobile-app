import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.jaime.ascend.viewmodel.DeathViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [DeathViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeathViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var context: Context
    private lateinit var viewModel: DeathViewModel

    /**
     * Sets up the test environment.
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        auth = mockk()
        firestore = mockk()
        context = mockk()
    }

    /**
     *  Tears down the test environment.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    /**
     * Tests the loading of a user's challenge.
     */
    @Test
    fun `loadUserChallenge loads existing challenge`() = runTest {
        val userId = "testUserId"
        val pendingChallengeId = "challenge123"
        val challengeDescription = mapOf("en" to "Test Challenge")

        val user = mockk<FirebaseUser>()
        every { user.uid } returns userId
        every { auth.currentUser } returns user

        val userDocSnapshot = mockk<DocumentSnapshot>()
        every { userDocSnapshot.getString("pendingChallenge") } returns pendingChallengeId

        val challengeDocSnapshot = mockk<DocumentSnapshot>()
        every { challengeDocSnapshot.get("description") } returns challengeDescription

        val userDocRef = mockk<DocumentReference>()
        every { userDocRef.get() } returns Tasks.forResult(userDocSnapshot)

        val challengeDocRef = mockk<DocumentReference>()
        every { challengeDocRef.get() } returns Tasks.forResult(challengeDocSnapshot)

        val usersCollection = mockk<CollectionReference>()
        every { usersCollection.document(userId) } returns userDocRef

        val challengesCollection = mockk<CollectionReference>()
        every { challengesCollection.document(pendingChallengeId) } returns challengeDocRef

        every { firestore.collection("users") } returns usersCollection
        every { firestore.collection("challenges") } returns challengesCollection

        viewModel = DeathViewModel(context)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Test Challenge", viewModel.revivalChallenge.value)
    }
}
