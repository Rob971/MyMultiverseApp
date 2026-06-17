package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun List<HouseholdInvite>.activeInvites(nowEpochMillis: Long = Clock.System.now().toEpochMilliseconds()): List<HouseholdInvite> =
    filter { invite ->
        invite.expiresAtEpochMillis?.let { it > nowEpochMillis } != false
    }

fun emailsMatch(left: String, right: String): Boolean =
    left.trim().lowercase() == right.trim().lowercase()
