package app.mymultiverse.kmp

import app.mymultiverse.kmp.data.supabase.SupabaseClientFactory
import app.mymultiverse.kmp.data.supabase.handleAuthDeeplink
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("AuthRedirectBridge")
object IosAuthRedirectBridge {
    fun handle(url: String) {
        val client = SupabaseClientFactory.createOrNull() ?: return
        handleAuthDeeplink(client, url)
    }
}
