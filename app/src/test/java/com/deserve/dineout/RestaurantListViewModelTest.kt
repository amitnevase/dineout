import androidx.lifecycle.Observer
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.domain.model.RestaurantResponse
import com.deserve.dineout.domain.repository.MainRepository
import com.deserve.dineout.presentation.list.RestaurantListViewModel
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RestaurantListViewModelTest {

    private lateinit var viewModel: RestaurantListViewModel
    private val mockRepository = mockk<MainRepository>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RestaurantListViewModel()
        viewModel.repository = mockRepository
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        clearAllMocks()
    }

    @Test
    fun fetchNearbyRestaurants_success() {
        // Mock response
        val mockResponse = RestaurantResponse(
            restaurants = listOf(

            ),
            nextPageToken = "next_page_token"
        )
        // Stub repository function
        coEvery { mockRepository.getNearbyRestaurants(any(), any(), any()) } returns mockResponse

        // Call the function to test
        viewModel.fetchNearbyRestaurants(0.0, 0.0)

        // Create observer for LiveData
        val observer = Observer<List<Restaurant>> { restaurants ->
            // Verify that the LiveData is updated correctly
            assertEquals(mockResponse.restaurants, restaurants)
        }

        // Call the function to test
        viewModel.fetchNearbyRestaurants(0.0, 0.0)
    }

    @Test
    fun fetchNearbyRestaurants_error() {
        // Stub repository function to throw an exception
        coEvery { mockRepository.getNearbyRestaurants(any(), any(), any()) } throws Exception()

        // Call the function to test
        viewModel.fetchNearbyRestaurants(0.0, 0.0)

    }
}
