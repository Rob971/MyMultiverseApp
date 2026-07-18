package app.mymultiverse.ammo.presentation.screens.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.components.JourneyPrimaryButton
import app.mymultiverse.ammo.presentation.components.JourneySecondaryButton
import app.mymultiverse.ammo.presentation.components.JourneyTertiaryButton
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.tour_action_finish
import ammo.composeapp.generated.resources.tour_action_next
import ammo.composeapp.generated.resources.tour_action_previous
import ammo.composeapp.generated.resources.tour_action_skip
import ammo.composeapp.generated.resources.tour_step_counter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val ScrimColor = Color.Black.copy(alpha = 0.72f)
private val SpotlightPadding = 10.dp
private val SpotlightCornerRadius = 14.dp
private const val TooltipMinTopPx = 80f

/**
 * Full-screen product tour overlay with a spotlight cutout and animated tooltip card.
 *
 * Place this as the topmost element in the authenticated app shell [Box] so it renders
 * above all content. The overlay shows/hides itself based on [ProductTourUiState].
 *
 * The spotlight highlights the root-relative bounding rect registered via
 * [Modifier.productTourTarget] for the current step's [ProductTourStep.targetTag]. When a step
 * has no target tag, the overlay shows a centred modal card over a plain dim background.
 */
@Composable
fun SpotlightTourOverlay(
    modifier: Modifier = Modifier,
    screenModel: ProductTourScreenModel = koinInject(),
) {
    val state by screenModel.uiState.collectAsState()
    val rects by screenModel.spotlightRects.collectAsState()

    AnimatedVisibility(
        visible = state is ProductTourUiState.Active,
        enter = fadeIn(animationSpec = tween(durationMillis = 350)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
        modifier = modifier.fillMaxSize(),
    ) {
        val active = state as? ProductTourUiState.Active ?: return@AnimatedVisibility
        val spotlightRect: Rect? = active.currentStep.targetTag?.let { tag -> rects[tag] }

        SpotlightScrimLayer(
            state = active,
            spotlightRect = spotlightRect,
            onNext = screenModel::next,
            onPrevious = screenModel::previous,
            onSkip = screenModel::skip,
        )
    }
}

@Composable
private fun SpotlightScrimLayer(
    state: ProductTourUiState.Active,
    spotlightRect: Rect?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
) {
    val density = LocalDensity.current
    val spotlightPaddingPx = with(density) { SpotlightPadding.toPx() }
    val cornerRadiusPx = with(density) { SpotlightCornerRadius.toPx() }

    val paddedRect: Rect? = spotlightRect?.expand(spotlightPaddingPx)

    // Offscreen layer so BlendMode.Clear punches through the scrim without erasing
    // the main app content underneath — only the overlay buffer is affected.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(ProductTourTestTags.OVERLAY)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Paint the dim scrim.
            drawRect(color = ScrimColor, blendMode = BlendMode.SrcOver)
            // 2. Clear the spotlight area so the main app content shows through.
            if (paddedRect != null) {
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(paddedRect.left, paddedRect.top),
                    size = Size(paddedRect.width, paddedRect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx),
                    blendMode = BlendMode.Clear,
                )
            }
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = {
                val forward = targetState.currentIndex > initialState.currentIndex
                (fadeIn(tween(200)) + slideInVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { if (forward) it / 4 else -it / 4 },
                )) togetherWith (fadeOut(tween(150)) + slideOutVertically(
                    animationSpec = tween(150),
                    targetOffsetY = { if (forward) -it / 4 else it / 4 },
                ))
            },
            label = "tour_step_transition",
        ) { activeState ->
            TourTooltipCard(
                state = activeState,
                spotlightRect = paddedRect,
                onNext = onNext,
                onPrevious = onPrevious,
                onSkip = onSkip,
            )
        }
    }
}

@Composable
private fun TourTooltipCard(
    state: ProductTourUiState.Active,
    spotlightRect: Rect?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    tooltipHeightEstimate: Dp = 260.dp,
) {
    val density = LocalDensity.current
    val cardAlignment = if (spotlightRect == null) Alignment.Center else Alignment.TopStart
    val topOffsetDp = tooltipTopOffsetDp(spotlightRect, density, tooltipHeightEstimate)

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = JourneySemanticColors.elevatedSurface(),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(cardAlignment)
                .let { m -> if (spotlightRect != null) m.padding(top = topOffsetDp) else m }
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .testTag(ProductTourTestTags.TOOLTIP_CARD),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.currentStep.illustrationRes?.let { res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            Res.string.tour_step_counter,
                            state.displayNumber,
                            state.stepCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = JourneySemanticColors.inkMuted(),
                        modifier = Modifier.testTag(ProductTourTestTags.STEP_COUNTER),
                    )
                    JourneyTertiaryButton(
                        onClick = onSkip,
                        label = stringResource(Res.string.tour_action_skip),
                        modifier = Modifier.testTag(ProductTourTestTags.BUTTON_SKIP),
                    )
                }

                Text(
                    text = stringResource(state.currentStep.title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = JourneySemanticColors.inkDeep(),
                    modifier = Modifier.testTag(ProductTourTestTags.STEP_TITLE),
                )

                Text(
                    text = stringResource(state.currentStep.description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkSecondary(),
                    modifier = Modifier.testTag(ProductTourTestTags.STEP_DESCRIPTION),
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!state.isFirst) {
                        JourneySecondaryButton(
                            onClick = onPrevious,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(ProductTourTestTags.BUTTON_PREVIOUS),
                        ) {
                            Text(stringResource(Res.string.tour_action_previous))
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    JourneyPrimaryButton(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(ProductTourTestTags.BUTTON_NEXT),
                    ) {
                        Text(
                            if (state.isLast) {
                                stringResource(Res.string.tour_action_finish)
                            } else {
                                stringResource(Res.string.tour_action_next)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns the top padding so the card appears:
 * - **above** the spotlight when there is enough room (spotlight in the lower portion of screen)
 * - **below** the spotlight otherwise (spotlight near the top)
 *
 * Returns 0.dp when [spotlightRect] is null (tooltip is centred via [Alignment.Center]).
 */
@Composable
private fun tooltipTopOffsetDp(
    spotlightRect: Rect?,
    density: Density,
    tooltipHeightEstimate: Dp,
): Dp {
    if (spotlightRect == null) return 0.dp
    val tooltipHeightPx = with(density) { tooltipHeightEstimate.toPx() }
    val cardGapPx = with(density) { 16.dp.toPx() }

    return with(density) {
        val aboveTop = spotlightRect.top - tooltipHeightPx - cardGapPx
        val belowBottom = spotlightRect.bottom + cardGapPx
        if (aboveTop > TooltipMinTopPx) aboveTop.toDp() else belowBottom.toDp()
    }
}

// ─── Target registration ────────────────────────────────────────────────────

/**
 * Registers the root-relative bounding box of this composable with [ProductTourScreenModel]
 * so the spotlight overlay can highlight it when the matching tour step is active.
 *
 * Pass the same [stepTag] as [ProductTourStep.targetTag].
 *
 * Example:
 * ```kotlin
 * HomeHubCard(
 *     modifier = Modifier.productTourTarget(ProductTourTestTags.TARGET_HOME_HUB)
 * )
 * ```
 */
@Composable
fun Modifier.productTourTarget(stepTag: String): Modifier {
    val screenModel = koinInject<ProductTourScreenModel>()
    return this.onGloballyPositioned { coords ->
        screenModel.registerCoordinate(stepTag, coords.boundsInRoot())
    }
}

// ─── Private helpers ─────────────────────────────────────────────────────────

private fun Rect.expand(px: Float): Rect = Rect(
    left = left - px,
    top = top - px,
    right = right + px,
    bottom = bottom + px,
)
