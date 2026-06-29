package app.mymultiverse.ammo

import app.mymultiverse.ammo.data.invite.InviteRedirectEvents
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("InviteRedirectBridge")
object IosInviteRedirectBridge {
    fun handle(url: String) {
        InviteRedirectEvents.emit(url)
    }
}
