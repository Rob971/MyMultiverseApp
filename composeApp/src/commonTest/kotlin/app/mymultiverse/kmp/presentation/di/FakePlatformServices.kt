package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import app.mymultiverse.kmp.domain.platform.PushNotificationRegistrar

class FakePersonalDataExporter(
    var lastSharedContent: String? = null,
    var shareResult: Boolean = true,
) : PersonalDataExporter {
    override fun shareJson(filename: String, content: String): Boolean {
        lastSharedContent = content
        return shareResult
    }
}

class FakePushNotificationRegistrar : PushNotificationRegistrar {
    var registerCalls = 0

    override suspend fun registerCurrentDeviceToken() {
        registerCalls++
    }
}
