package app.mymultiverse.ammo.data.supabase.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class GeminiKeyRpcDecoderTest {

    @Test
    fun decodeKeyFromData_arrayResponse_returnsTrimmedKey() {
        assertEquals(
            "AIzaSy_test",
            GeminiKeyRpcDecoder.decodeKeyFromData("[{\"key\":\"  AIzaSy_test  \"}]"),
        )
    }

    @Test
    fun decodeKeyFromData_emptyArray_returnsEmptyString() {
        assertEquals("", GeminiKeyRpcDecoder.decodeKeyFromData("[]"))
    }

    @Test
    fun decodeKeyFromData_singleObjectResponse_returnsKey() {
        assertEquals(
            "single-object-key",
            GeminiKeyRpcDecoder.decodeKeyFromData("{\"key\":\"single-object-key\"}"),
        )
    }

    @Test
    fun decodeKeyFromData_invalidPayload_throws() {
        assertFails { GeminiKeyRpcDecoder.decodeKeyFromData("not-json") }
    }
}
