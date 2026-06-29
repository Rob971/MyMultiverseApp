package app.mymultiverse.ammo.data.observability

import app.mymultiverse.ammo.domain.observability.CrashReporter

class NoOpCrashReporter : CrashReporter {
    override fun initialize() = Unit

    override fun setUserId(userId: String?) = Unit

    override fun logBreadcrumb(message: String) = Unit

    override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) = Unit
}
