import com.jaime.ascend.auth.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the [AuthViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    /**
     * Sets up the test environment.
     */
    @Before
    fun setUp() {
        authRepository = mockk()
        viewModel = AuthViewModel()

        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Tears down the test environment.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    /**
     * Tests the sign-in functionality.
     */
    @Test
    fun `signIn calls callback true when repository returns success`() = runTest {
        val email = "user@example.com"
        val password = "123456"

        every { authRepository.signIn(email, password, any()) } answers {
            val callback = thirdArg<(Boolean) -> Unit>()
            callback(true)
        }

        var result = false
        viewModel.signIn(email, password) { success ->
            result = success
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        verify { authRepository.signIn(email, password, any()) }
    }

    /**
     * Tests the sign-in functionality when email or password is empty.
     */
    @Test
    fun `signIn calls callback false if email or password empty`() {
        var result = true
        viewModel.signIn("", "") { success ->
            result = success
        }

        assertFalse(result)
        verify(exactly = 0) { authRepository.signIn(any(), any(), any()) }
    }
}
