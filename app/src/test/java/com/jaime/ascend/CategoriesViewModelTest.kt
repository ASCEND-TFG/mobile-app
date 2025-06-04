import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.viewmodel.CategoriesViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the [CategoriesViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: CategoryRepository
    private lateinit var viewModel: CategoriesViewModel

    /**
     * Sets up the test environment.
     */
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
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
     * Tests the loading of categories.
     */
    @Test
    fun `loadCategories sets Loading then Success state`() = runTest {
        val fakeCategories = listOf(
            Category(id = "1", name = mapOf("es" to "Category1", "en" to "Category1_en")),
            Category(id = "2", name = mapOf("es" to "Category2", "en" to "Category2_en"))
        )

        coEvery { repository.getCategories() } returns fakeCategories

        viewModel = CategoriesViewModel(repository)

        assertTrue(viewModel.state.value is CategoriesViewModel.CategoriesState.Loading)

        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is CategoriesViewModel.CategoriesState.Success)
        assertEquals(fakeCategories, (state as CategoriesViewModel.CategoriesState.Success).categories)

        coVerify(exactly = 1) { repository.getCategories() }
    }

    /**
     * Tests the loading of categories when an exception is thrown.
     */
    @Test
    fun `loadCategories sets Loading then Error state on exception`() = runTest {
        val errorMessage = "Network error"
        coEvery { repository.getCategories() } throws Exception(errorMessage)

        viewModel = CategoriesViewModel(repository)

        assertTrue(viewModel.state.value is CategoriesViewModel.CategoriesState.Loading)

        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is CategoriesViewModel.CategoriesState.Error)
        assertTrue((state as CategoriesViewModel.CategoriesState.Error).message.contains(errorMessage))

        coVerify(exactly = 1) { repository.getCategories() }
    }
}
