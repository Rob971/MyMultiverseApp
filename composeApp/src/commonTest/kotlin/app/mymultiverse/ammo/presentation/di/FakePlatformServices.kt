package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.platform.AppStoreLauncher
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar

class FakePersonalDataExporter(
    var lastSharedContent: String? = null,
    var lastSharedText: String? = null,
    var shareResult: Boolean = true,
) : PersonalDataExporter {
    override fun shareJson(filename: String, content: String): Boolean {
        lastSharedContent = content
        return shareResult
    }

    override fun shareText(chooserTitle: String, message: String): Boolean {
        lastSharedText = message
        return shareResult
    }
}

class FakePushNotificationRegistrar : PushNotificationRegistrar {
    var registerCalls = 0

    override suspend fun registerCurrentDeviceToken() {
        registerCalls++
    }
}

class FakeAppStoreLauncher : AppStoreLauncher {
    var openStoreListingCalls = 0

    override fun openStoreListing() {
        openStoreListingCalls++
    }
}
