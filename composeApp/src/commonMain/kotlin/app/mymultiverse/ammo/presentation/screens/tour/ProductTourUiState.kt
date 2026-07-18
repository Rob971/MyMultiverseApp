package app.mymultiverse.ammo.presentation.screens.tour

/**
 * UI state machine for the product tour overlay.
 */
sealed interface ProductTourUiState {

    /** Tour overlay is not visible — either already seen or not yet triggered. */
    data object Hidden : ProductTourUiState

    /**
     * Tour overlay is active and showing [currentStep].
     *
     * @param steps Ordered list of tour steps for this session.
     * @param currentIndex Zero-based index of the step being shown.
     */
    data class Active(
        val steps: List<ProductTourStep>,
        val currentIndex: Int,
    ) : ProductTourUiState {
        val currentStep: ProductTourStep get() = steps[currentIndex]
        val isFirst: Boolean get() = currentIndex == 0
        val isLast: Boolean get() = currentIndex == steps.lastIndex
        val stepCount: Int get() = steps.size
        val displayNumber: Int get() = currentIndex + 1
    }
}
