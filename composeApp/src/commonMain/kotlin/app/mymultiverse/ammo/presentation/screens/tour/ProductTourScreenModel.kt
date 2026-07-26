package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.ui.geometry.Rect
import app.mymultiverse.ammo.data.tour.ProductTourStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the product tour lifecycle and spotlight coordinate registry.
 *
 * Responsibilities:
 * - Decides whether the tour should be shown for the current app version.
 * - Exposes the step-by-step [uiState] to the overlay composable.
 * - Receives spotlight bounding-box registrations from composables annotated with
 *   [Modifier.productTourTarget] so the overlay can draw the correct cutout.
 * - Marks the tour as permanently seen (per [ProductTourCatalog.TOUR_ID]) when the user
 *   finishes the last step or skips — so a new user completes the walkthrough only once.
 */
class ProductTourScreenModel(
    private val store: ProductTourStore,
    @Suppress("unused")
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

    private val _uiState = MutableStateFlow<ProductTourUiState>(ProductTourUiState.Hidden)
    val uiState: StateFlow<ProductTourUiState> = _uiState.asStateFlow()

    /** Root-relative bounding boxes of each targeted composable, keyed by step target tag. */
    private val _spotlightRects = MutableStateFlow<Map<String, Rect>>(emptyMap())
    val spotlightRects: StateFlow<Map<String, Rect>> = _spotlightRects.asStateFlow()

    private var activeTourKey: String? = null

    /**
     * Activates the tour for [versionKey] if the user has not yet seen it.
     *
     * Pass [ProductTourCatalog.TOUR_ID] as [versionKey] so alpha/beta version-name stamps
     * do not re-trigger the tour. Call after Welcome Home is visible.
     */
    fun maybeShowTour(versionKey: String, steps: List<ProductTourStep>) {
        if (steps.isEmpty()) return
        // Do not reset an in-progress tour if HomePhase re-emits Welcome.
        if (_uiState.value is ProductTourUiState.Active) return
        if (!store.hasSeenTour(versionKey)) {
            activeTourKey = versionKey
            _uiState.value = ProductTourUiState.Active(steps = steps, currentIndex = 0)
        }
    }

    /** Registers the root-relative bounding box of a targeted composable element. */
    fun registerCoordinate(stepTag: String, rect: Rect) {
        _spotlightRects.value = _spotlightRects.value + (stepTag to rect)
    }

    /** Advances to the next step, or completes the tour if already on the last step. */
    fun next() {
        val current = _uiState.value as? ProductTourUiState.Active ?: return
        if (current.isLast) {
            completeTour()
        } else {
            _uiState.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    /** Returns to the previous step. No-op when on the first step. */
    fun previous() {
        val current = _uiState.value as? ProductTourUiState.Active ?: return
        if (!current.isFirst) {
            _uiState.value = current.copy(currentIndex = current.currentIndex - 1)
        }
    }

    /** Dismisses the tour without completing all steps and records it as seen. */
    fun skip() = completeTour()

    private fun completeTour() {
        activeTourKey?.let { store.markTourSeen(it) }
        _uiState.value = ProductTourUiState.Hidden
    }
}
