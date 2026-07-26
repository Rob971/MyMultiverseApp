package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvatarUploadTelemetryTest {

    @Test
    fun persistException_exposesStableErrorCode() {
        val error = AvatarPersistException(
            target = AvatarUploadTarget.Household,
            dbTable = "households",
            householdId = "hh-1",
            storagePath = "households/hh-1/avatar.jpg",
        )

        assertEquals(AvatarPersistException.ERROR_CODE, error.message)
        assertEquals(AvatarUploadTarget.Household, error.target)
        assertEquals("households", error.dbTable)
    }

    @Test
    fun failureReasonFor_mapsPersistExceptionToRlsZeroRows() {
        val error = AvatarPersistException(
            target = AvatarUploadTarget.MemberProfile,
            dbTable = "profiles",
            householdId = "hh-1",
            storagePath = "profiles/user-1/avatar.jpg",
            memberId = "member-1",
        )

        assertEquals(
            AvatarUploadFailureReason.RlsZeroRows,
            AvatarUploadTelemetry.failureReasonFor(error),
        )
    }

    @Test
    fun failureReasonFor_mapsLegacyMessageToRlsZeroRows() {
        assertEquals(
            AvatarUploadFailureReason.RlsZeroRows,
            AvatarUploadTelemetry.failureReasonFor(
                IllegalStateException(AvatarPersistException.ERROR_CODE),
            ),
        )
    }

    @Test
    fun failureReasonFor_mapsUnsupportedImageAndStorageMimeErrors() {
        assertEquals(
            AvatarUploadFailureReason.InvalidMime,
            AvatarUploadTelemetry.failureReasonFor(AvatarUnsupportedImageException()),
        )
        assertEquals(
            AvatarUploadFailureReason.InvalidMime,
            AvatarUploadTelemetry.failureReasonFor(
                IllegalStateException("mime type image/jpg is not supported"),
            ),
        )
        assertEquals(
            AvatarUploadFailureReason.PayloadTooLarge,
            AvatarUploadTelemetry.failureReasonFor(
                IllegalStateException("Payload too large"),
            ),
        )
    }

    @Test
    fun context_includesStructuredCrashlyticsKeys() {
        val context = AvatarUploadTelemetry.context(
            target = AvatarUploadTarget.Household,
            step = AvatarUploadStep.DbPersist,
            householdId = "hh-1",
            storagePath = "households/hh-1/avatar.jpg",
            dbTable = "households",
            failureReason = AvatarUploadFailureReason.RlsZeroRows,
        )

        assertEquals("household", context[AvatarUploadTelemetry.KEY_TARGET])
        assertEquals("db_persist", context[AvatarUploadTelemetry.KEY_STEP])
        assertEquals("rls_zero_rows", context[AvatarUploadTelemetry.KEY_FAILURE_REASON])
        assertEquals("households", context[AvatarUploadTelemetry.KEY_DB_TABLE])
        assertEquals("hh-1", context[AvatarUploadTelemetry.KEY_HOUSEHOLD_ID])
        assertTrue(context[AvatarUploadTelemetry.KEY_STORAGE_PATH]!!.contains("households/hh-1"))
    }

    @Test
    fun breadcrumb_usesStableStepAndTargetTokens() {
        val message = AvatarUploadTelemetry.breadcrumb(
            target = AvatarUploadTarget.Dependant,
            step = AvatarUploadStep.StorageUpload,
            householdId = "hh-2",
            extra = "member=dep-1",
        )

        assertTrue(message.contains("step=storage_upload"))
        assertTrue(message.contains("target=dependant"))
        assertTrue(message.contains("household=hh-2"))
        assertTrue(message.contains("member=dep-1"))
    }
}
