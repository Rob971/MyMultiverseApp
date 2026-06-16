package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import kotlinx.datetime.Clock

fun ContactGroup.isActive(nowEpochMillis: Long = Clock.System.now().toEpochMilliseconds()): Boolean {
    if (lifecycle == GroupLifecycle.Persistent) return true
    val expiresAt = expiresAtEpochMillis ?: return false
    return nowEpochMillis < expiresAt
}

fun List<ContactGroup>.activeOnly(nowEpochMillis: Long = Clock.System.now().toEpochMilliseconds()): List<ContactGroup> =
    filter { it.isActive(nowEpochMillis) }
