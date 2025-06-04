import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import com.jaime.ascend.viewmodel.FriendRequestUiState
import com.jaime.ascend.viewmodel.FriendRequestViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [FriendRequestViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FriendRequestViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FriendRequestViewModel
    private val repo = mockk<FriendRequestRepository>(relaxed = true)
    private val userRepo = mockk<UserRepository>(relaxed = true)
    private val context = mockk<android.content.Context>(relaxed = true)

    /**
     * Sets up the test environment.
     */
    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        viewModel = FriendRequestViewModel(repo, userRepo, context)
    }

    /**
     * Tests the sending of a friend request.
     */
    @Test
    fun `sendFriendRequest - already friends`() = runTest(dispatcher) {
        val currentUserId = "user123"
        val friendId = "friend456"
        val mockUser = mutableMapOf<String, Any>(
            "documentId" to friendId,
            "username" to "Carlos",
            "friends" to listOf(currentUserId),
            "pendingRequests" to emptyList<String>()
        )

        coEvery { repo.searchUserByUsername("Carlos") } returns mockUser
        coEvery { repo.getCurrentUserId() } returns currentUserId

        viewModel.sendFriendRequest("Carlos")
        advanceUntilIdle()

        assertEquals(FriendRequestUiState.AlreadyFriends, viewModel.uiState.value)
    }
}
