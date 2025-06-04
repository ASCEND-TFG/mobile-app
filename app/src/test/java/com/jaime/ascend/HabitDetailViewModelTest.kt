import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.utils.Difficulty
import com.jaime.ascend.viewmodel.HabitDetailViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the [HabitDetailViewModel].
 * @author Jaime Martínez Fernández
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bhabitCollection: CollectionReference
    private lateinit var docRef: DocumentReference
    private lateinit var viewModel: HabitDetailViewModel

    /**
     * Sets up the test environment.
     */
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        firestore = mockk(relaxed = true)
        bhabitCollection = mockk(relaxed = true)
        docRef = mockk(relaxed = true)

        every { firestore.collection("bhabits") } returns bhabitCollection
        every { bhabitCollection.document(any()) } returns docRef
        coEvery { docRef.update(any<Map<String, Any>>()) } returns mockk()

        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore

        viewModel = HabitDetailViewModel(habitId = "habit123", isGoodHabit = false)

        viewModel.apply {
            val dummyHabit = BadHabit(
                id = "habit123",
                difficulty = Difficulty.EASY,
                coinReward = 5,
                xpReward = 10,
                lifeLoss = 5
            )
            this::class.java.getDeclaredField("_bhabit").apply {
                isAccessible = true
                (get(viewModel) as MutableStateFlow<BadHabit?>).value = dummyHabit
            }
        }
    }

    /**
     * Tests the updating of a bad habit.
     */
    @Test
    fun `updateBadHabit updates the state correctly`() = runTest(dispatcher) {
        viewModel.updateBadHabit(
            habitId = "habit123",
            difficulty = Difficulty.HARD
        )

        advanceUntilIdle()

        val updated = viewModel.bhabit.value
        assertEquals(Difficulty.HARD, updated?.difficulty)
        assertEquals(Difficulty.HARD.coinValue, updated?.coinReward)
        assertEquals(Difficulty.HARD.xpValue, updated?.xpReward)
        assertEquals(Difficulty.HARD.lifeLoss, updated?.lifeLoss)
    }
}
