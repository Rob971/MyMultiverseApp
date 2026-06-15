package app.mymultiverse.kmp

import app.mymultiverse.kmp.data.supabase.AuthRedirectEvents
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("AuthRedirectBridge")
object IosAuthRedirectBridge {
    fun handle(url: String) {
        AuthRedirectEvents.emit(url)
    }
}
