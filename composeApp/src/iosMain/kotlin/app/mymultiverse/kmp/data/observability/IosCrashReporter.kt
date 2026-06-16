package app.mymultiverse.kmp.data.observability

import app.mymultiverse.kmp.domain.observability.CrashReporter

/**
 * iOS Crashlytics wiring is pending Firebase CocoaPods setup.
 * Logs still flow through Kermit on Apple platforms.
 */
class IosCrashReporter : CrashReporter by NoOpCrashReporter()
