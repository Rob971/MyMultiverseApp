package app.mymultiverse.ammo.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.assertTrue

object ImageVectorDrawableAssertions {

    fun assertHasDrawablePaths(
        imageVector: ImageVector,
        minPaths: Int,
        label: String = imageVector.name,
    ) {
        val pathCount = countPaths(imageVector.root)
        assertTrue(
            pathCount >= minPaths,
            "$label expected at least $minPaths path nodes but had $pathCount",
        )
        assertAllPathsHaveData(imageVector.root, label)
    }

    private fun countPaths(group: VectorGroup): Int {
        var count = 0
        for (node in group) {
            when (node) {
                is VectorPath -> count++
                is VectorGroup -> count += countPaths(node)
            }
        }
        return count
    }

    private fun assertAllPathsHaveData(group: VectorGroup, label: String) {
        for (node in group) {
            when (node) {
                is VectorPath -> assertTrue(
                    node.pathData.isNotEmpty(),
                    "$label path '${node.name}' has empty pathData",
                )
                is VectorGroup -> assertAllPathsHaveData(node, label)
            }
        }
    }
}
