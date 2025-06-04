import android.content.Context
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import com.jaime.ascend.data.repository.*
import com.jaime.ascend.data.models.*
import com.jaime.ascend.utils.Difficulty
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

/**
 * Unit tests for the [GoodHabitsViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoodHabitsViewModelTest {
    @MockK
    private lateinit var mockCategoryRepo: CategoryRepository

    @MockK
    private lateinit var mockHabitRepo: GoodHabitRepository

    @MockK
    private lateinit var mockTemplateRepo: TemplateRepository

    @MockK
    private lateinit var mockContext: Context

    private lateinit var viewModel: GoodHabitsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    /**
     * Sets up the test environment.
     */
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = GoodHabitsViewModel(
            categoryRepository = mockCategoryRepo,
            habitRepository = mockHabitRepo,
            templateRepository = mockTemplateRepo,
            context = mockContext
        )
    }

    /**
     * Tears down the test environment.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * Tests the loading of good habit templates.
     */
    @Test
    fun `loadTemplate should handle localized names`() = runTest {
        val testTemplate = HabitTemplate(
            id = "1",
            name = mapOf("en" to "Test Habit", "es" to "Hábito de prueba"),
            description = mapOf("en" to "Test Description", "es" to "Descripción de prueba")
        )

        coEvery { mockTemplateRepo.getGoodHabitTemplateById("1") } returns testTemplate

        viewModel.loadTemplate("1")

        assert(viewModel.templateToAdd.value == testTemplate)
        coVerify { mockTemplateRepo.getGoodHabitTemplateById("1") }
    }

    /**
     * Tests the creation of a good habit.
     */
    @Test
    fun `createGoodHabit should preserve localized names`() = runTest {
        val localizedTemplate = HabitTemplate(
            id = "multiLang",
            name = mapOf("en" to "Exercise", "es" to "Ejercicio"),
            description = mapOf("en" to "Daily workout", "es" to "Entrenamiento diario")
        )

        coEvery { mockTemplateRepo.getGoodHabitTemplateById("multiLang") } returns localizedTemplate
        coEvery { mockHabitRepo.createGoodHabit(any(), any(), any(), any()) } returns true

        viewModel.loadTemplate("multiLang")

        var result: Result<Unit>? = null
        viewModel.createGoodHabit(
            templateId = "multiLang",
            days = listOf(1, 3, 5),
            difficulty = Difficulty.HARD,
            onComplete = { result = it }
        )

        assertTrue(result?.isSuccess == true)
        coVerify {
            mockHabitRepo.createGoodHabit(
                templateId = "multiLang",
                days = listOf(1, 3, 5),
                difficulty = Difficulty.HARD,
                reminderTime = null
            )
        }
    }

    /**
     * Tests the loading of categories.
     */
    @Test
    fun `loadCategories should update categories list`() = runTest {
        val testCategories = listOf(Category(id = "1", name = mapOf("en" to "Test Habit", "es" to "Hábito de prueba")))
        coEvery { mockCategoryRepo.getCategories() } returns testCategories

        viewModel.loadCategories()

        assert(viewModel.categories.value == testCategories)
        coVerify { mockCategoryRepo.getCategories() }
    }
}