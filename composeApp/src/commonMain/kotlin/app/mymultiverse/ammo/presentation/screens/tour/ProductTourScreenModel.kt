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
 * - Marks the tour as permanently seen (per version) when the user completes or skips it.
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

    private var activeVersionKey: String? = null

    /**
     * Activates the tour for [versionKey] if the user has not yet seen it.
     *
     * Should be called once after the authenticated shell is visible (e.g. in a
     * [LaunchedEffect(Unit)] inside [AuthenticatedMainApp]).
     */
    fun maybeShowTour(versionKey: String, steps: List<ProductTourStep>) {
        if (steps.isEmpty()) return
        if (!store.hasSeenTour(versionKey)) {
            activeVersionKey = versionKey
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
        activeVersionKey?.let { store.markTourSeen(it) }
        _uiState.value = ProductTourUiState.Hidden
    }
}
