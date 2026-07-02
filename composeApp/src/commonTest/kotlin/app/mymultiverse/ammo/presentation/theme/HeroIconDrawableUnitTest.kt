package app.mymultiverse.ammo.presentation.theme

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Pure-JVM regression gate: hero/nav vectors must ship separate, non-empty path nodes.
 * Runs in CI via `:composeApp:testDebugUnitTest` before any push.
 */
class HeroIconDrawableUnitTest {

    @Test
    fun planLunchIcon_hasMinimumSeparatePaths() {
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIcons.PlanLunchPlaceSetting, minPaths = 5)
    }

    @Test
    fun freshGroceriesIcon_hasMinimumSeparatePaths() {
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIcons.FreshGroceries, minPaths = 7)
    }

    @Test
    fun heroIcons_eachPathHasNonZeroBounds() {
        assertPathsHaveNonZeroBounds(AppIcons.PlanLunchPlaceSetting)
        assertPathsHaveNonZeroBounds(AppIcons.FreshGroceries)
        assertPathsHaveNonZeroBounds(AppIcons.Home)
    }

    @Test
    fun navTabIcons_matchApprovedHeroVectors() {
        val mealPlan = AppIconRole.NavMealPlan.imageVector()
        val grocery = AppIconRole.NavGrocery.imageVector()
        assertTrue(mealPlan.name == AppIcons.PlanLunchPlaceSetting.name)
        assertTrue(grocery.name == AppIcons.FreshGroceries.name)
        ImageVectorDrawableAssertions.assertHasDrawablePaths(mealPlan, minPaths = 5)
        ImageVectorDrawableAssertions.assertHasDrawablePaths(grocery, minPaths = 7)
    }

    private fun assertPathsHaveNonZeroBounds(icon: androidx.compose.ui.graphics.vector.ImageVector) {
        collectPaths(icon.root).forEach { path ->
            val bounds = path.pathData.boundingBox()
            assertTrue(
                bounds.width > 0.5f && bounds.height > 0.5f,
                "${icon.name} path '${path.name}' has zero drawable bounds",
            )
        }
    }

    private fun collectPaths(group: VectorGroup): List<VectorPath> = buildList {
        for (node in group) {
            when (node) {
                is VectorPath -> add(node)
                is VectorGroup -> addAll(collectPaths(node))
            }
        }
    }
}

internal fun List<PathNode>.boundingBox(): Rect {
    if (isEmpty()) return Rect.Zero

    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    var x = 0f
    var y = 0f

    fun include(px: Float, py: Float) {
        minX = minOf(minX, px)
        minY = minOf(minY, py)
        maxX = maxOf(maxX, px)
        maxY = maxOf(maxY, py)
    }

    for (node in this) {
        when (node) {
            is PathNode.MoveTo -> {
                x = node.x
                y = node.y
                include(x, y)
            }
            is PathNode.LineTo -> {
                x = node.x
                y = node.y
                include(x, y)
            }
            is PathNode.HorizontalTo -> {
                x = node.x
                include(x, y)
            }
            is PathNode.VerticalTo -> {
                y = node.y
                include(x, y)
            }
            is PathNode.RelativeMoveTo -> {
                x += node.dx
                y += node.dy
                include(x, y)
            }
            is PathNode.RelativeLineTo -> {
                x += node.dx
                y += node.dy
                include(x, y)
            }
            is PathNode.RelativeHorizontalTo -> {
                x += node.dx
                include(x, y)
            }
            is PathNode.RelativeVerticalTo -> {
                y += node.dy
                include(x, y)
            }
            is PathNode.CurveTo -> {
                include(node.x1, node.y1)
                include(node.x2, node.y2)
                x = node.x3
                y = node.y3
                include(x, y)
            }
            is PathNode.RelativeCurveTo -> {
                include(x + node.dx1, y + node.dy1)
                include(x + node.dx2, y + node.dy2)
                x += node.dx3
                y += node.dy3
                include(x, y)
            }
            is PathNode.QuadTo -> {
                include(node.x1, node.y1)
                x = node.x2
                y = node.y2
                include(x, y)
            }
            is PathNode.RelativeQuadTo -> {
                include(x + node.dx1, y + node.dy1)
                x += node.dx2
                y += node.dy2
                include(x, y)
            }
            is PathNode.ReflectiveCurveTo -> {
                include(node.x1, node.y1)
                x = node.x2
                y = node.y2
                include(x, y)
            }
            is PathNode.RelativeReflectiveCurveTo -> {
                include(x + node.dx1, y + node.dy1)
                x += node.dx2
                y += node.dy2
                include(x, y)
            }
            is PathNode.ArcTo,
            is PathNode.RelativeArcTo,
            is PathNode.ReflectiveQuadTo,
            is PathNode.RelativeReflectiveQuadTo,
            -> Unit
            is PathNode.Close -> Unit
        }
    }

    return Rect(minX, minY, maxX, maxY)
}
