package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvatarImagePreparationTest {

    @Test
    fun avatarSampleSize_keepsFullResolutionForSmallImages() {
        assertEquals(1, avatarSampleSize(800, 600, maxDimension = 1024))
        assertEquals(1, avatarSampleSize(1024, 1024, maxDimension = 1024))
    }

    @Test
    fun avatarSampleSize_halvesLargeImages() {
        // 4000x3000 down to <=1024: half edges 2000x1500 still >= 1024 -> 2,
        // quarter edges 1000x750 -> 750 < 1024 so stop at 2.
        assertEquals(2, avatarSampleSize(4000, 3000, maxDimension = 1024))
    }

    @Test
    fun avatarSampleSize_growsWithVeryLargeImages() {
        val sample = avatarSampleSize(9000, 9000, maxDimension = 1024)
        assertTrue(sample >= 4, "expected sampleSize >= 4 for a 9000px image, was $sample")
        // Sample size is always a power of two.
        assertEquals(0, sample and (sample - 1))
    }

    @Test
    fun avatarSampleSize_guardsAgainstInvalidInput() {
        assertEquals(1, avatarSampleSize(0, 600))
        assertEquals(1, avatarSampleSize(800, -1))
        assertEquals(1, avatarSampleSize(800, 600, maxDimension = 0))
    }

    @Test
    fun avatarScaledDimensions_leavesSmallImagesUntouched() {
        assertEquals(AvatarDimensions(800, 600), avatarScaledDimensions(800, 600, maxDimension = 1024))
    }

    @Test
    fun avatarScaledDimensions_scalesLandscapeToMaxWidth() {
        assertEquals(AvatarDimensions(1024, 768), avatarScaledDimensions(4000, 3000, maxDimension = 1024))
    }

    @Test
    fun avatarScaledDimensions_scalesPortraitToMaxHeight() {
        assertEquals(AvatarDimensions(768, 1024), avatarScaledDimensions(3000, 4000, maxDimension = 1024))
    }

    @Test
    fun avatarScaledDimensions_neverReturnsZeroForExtremeAspectRatios() {
        val scaled = avatarScaledDimensions(5000, 1, maxDimension = 1024)
        assertEquals(1024, scaled.width)
        assertTrue(scaled.height >= 1, "height must stay at least 1px, was ${scaled.height}")
    }

    @Test
    fun normalizedContentType_isAnAcceptedBucketMimeType() {
        assertEquals("image/jpeg", AvatarImageSpec.NORMALIZED_CONTENT_TYPE)
        assertEquals("jpg", avatarExtensionForContentType(AvatarImageSpec.NORMALIZED_CONTENT_TYPE))
    }
}
