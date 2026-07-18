package app.mymultiverse.ammo.presentation.screens.tour

import app.mymultiverse.ammo.data.tour.ProductTourStore
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [ProductTourScreenModel].
 *
 * [ProductTourStep] uses [StringResource] which cannot be loaded in commonTest without an
 * Android context. We use stub instances (created via reflection-free fake approach) by passing
 * the real [StringResource] constructor is not accessible, so we instead verify state machine
 * transitions that are independent of the actual string content.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductTourScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun store(settings: MapSettings = MapSettings()) = ProductTourStore(settings)

    private fun model(store: ProductTourStore = store()): ProductTourScreenModel =
        ProductTourScreenModel(store = store)

    // ─── State transitions ────────────────────────────────────────────────────

    @Test
    fun initialState_isHidden() = runTest(testDispatcher) {
        val m = model()
        assertIs<ProductTourUiState.Hidden>(m.uiState.first())
    }

    @Test
    fun maybeShowTour_activatesTour_whenNotSeenBefore() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        val state = m.uiState.first()
        assertIs<ProductTourUiState.Active>(state)
        assertEquals(0, state.currentIndex)
        assertEquals(3, state.stepCount)
    }

    @Test
    fun maybeShowTour_doesNotActivate_whenAlreadySeen() = runTest(testDispatcher) {
        val s = store()
        s.markTourSeen("1.5.3")
        val m = model(s)

        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        assertIs<ProductTourUiState.Hidden>(m.uiState.first())
    }

    @Test
    fun maybeShowTour_doesNotActivate_whenStepsListIsEmpty() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", emptyList())
        advanceUntilIdle()

        assertIs<ProductTourUiState.Hidden>(m.uiState.first())
    }

    @Test
    fun next_advancesToNextStep() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        m.next()
        advanceUntilIdle()

        val state = m.uiState.first()
        assertIs<ProductTourUiState.Active>(state)
        assertEquals(1, state.currentIndex)
    }

    @Test
    fun next_onLastStep_completesTourAndHides() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(2))
        advanceUntilIdle()

        m.next() // step 0 → step 1 (last)
        advanceUntilIdle()
        m.next() // last → complete
        advanceUntilIdle()

        assertIs<ProductTourUiState.Hidden>(m.uiState.first())
    }

    @Test
    fun previous_returnsToPreviousStep() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        m.next()
        advanceUntilIdle()
        m.previous()
        advanceUntilIdle()

        val state = m.uiState.first()
        assertIs<ProductTourUiState.Active>(state)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun previous_onFirstStep_doesNothing() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        m.previous()
        advanceUntilIdle()

        val state = m.uiState.first()
        assertIs<ProductTourUiState.Active>(state)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun skip_hidesOverlay() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        m.skip()
        advanceUntilIdle()

        assertIs<ProductTourUiState.Hidden>(m.uiState.first())
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    @Test
    fun skip_marksTourSeen_preventsShowOnNextAttempt() = runTest(testDispatcher) {
        val settings = MapSettings()
        val m = model(store(settings))
        m.maybeShowTour("1.5.3", stubSteps(2))
        advanceUntilIdle()

        m.skip()
        advanceUntilIdle()

        // A new model with the same persisted settings should not show the tour again
        val m2 = model(store(settings))
        m2.maybeShowTour("1.5.3", stubSteps(2))
        advanceUntilIdle()

        assertIs<ProductTourUiState.Hidden>(m2.uiState.first())
    }

    @Test
    fun next_onCompletion_marksTourSeen() = runTest(testDispatcher) {
        val settings = MapSettings()
        val m = model(store(settings))
        m.maybeShowTour("1.5.3", stubSteps(1))
        advanceUntilIdle()

        m.next() // only step → complete
        advanceUntilIdle()

        assertTrue(store(settings).hasSeenTour("1.5.3"))
    }

    // ─── Computed state properties ────────────────────────────────────────────

    @Test
    fun activeState_isFirst_trueOnlyForFirstStep() = runTest(testDispatcher) {
        val m = model()
        m.maybeShowTour("1.5.3", stubSteps(3))
        advanceUntilIdle()

        val s0 = m.uiState.first() as ProductTourUiState.Active
        assertTrue(s0.isFirst)
        assertFalse(s0.isLast)
        assertEquals(1, s0.displayNumber)

        m.next()
        advanceUntilIdle()
        val s1 = m.uiState.first() as ProductTourUiState.Active
        assertFalse(s1.isFirst)
        assertFalse(s1.isLast)

        m.next()
        advanceUntilIdle()
        val s2 = m.uiState.first() as ProductTourUiState.Active
        assertFalse(s2.isFirst)
        assertTrue(s2.isLast)
        assertEquals(3, s2.displayNumber)
    }

    @Test
    fun registerCoordinate_storesRectForTag() = runTest(testDispatcher) {
        val m = model()
        val rect = androidx.compose.ui.geometry.Rect(10f, 20f, 100f, 200f)

        m.registerCoordinate("some_tag", rect)
        advanceUntilIdle()

        assertEquals(rect, m.spotlightRects.first()["some_tag"])
    }

    @Test
    fun registerCoordinate_updatesExistingEntry() = runTest(testDispatcher) {
        val m = model()
        val rect1 = androidx.compose.ui.geometry.Rect(0f, 0f, 50f, 50f)
        val rect2 = androidx.compose.ui.geometry.Rect(10f, 10f, 80f, 80f)

        m.registerCoordinate("tag", rect1)
        m.registerCoordinate("tag", rect2)
        advanceUntilIdle()

        assertEquals(rect2, m.spotlightRects.first()["tag"])
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Returns [count] stub [ProductTourStep] instances.
 *
 * [ProductTourStep.title] and [ProductTourStep.description] are [StringResource] values which
 * are normally generated from composeResources. In commonTest we cannot resolve them, so we
 * produce throwaway instances using the fake resource helper below.
 */
private fun stubSteps(count: Int): List<ProductTourStep> =
    (0 until count).map { i ->
        ProductTourStep(
            id = "step_$i",
            title = fakeStringResource("title_$i"),
            description = fakeStringResource("desc_$i"),
            targetTag = if (i > 0) "target_$i" else null,
        )
    }

/**
 * Creates a [StringResource] without a real compose-resources lookup.
 * The value is never resolved in these unit tests — only the state machine behaviour is tested.
 * Uses [InternalResourceApi] to construct the stub instance directly.
 */
@OptIn(InternalResourceApi::class)
private fun fakeStringResource(key: String): StringResource =
    StringResource(
        id = key,
        key = key,
        items = setOf(),
    )
