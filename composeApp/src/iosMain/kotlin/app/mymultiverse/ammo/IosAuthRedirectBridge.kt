package app.mymultiverse.ammo

import app.mymultiverse.ammo.data.supabase.AuthRedirectEvents
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("AuthRedirectBridge")
object IosAuthRedirectBridge {
    fun handle(url: String) {
        AuthRedirectEvents.emit(url)
    }
}
