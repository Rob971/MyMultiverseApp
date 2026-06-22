package app.mymultiverse.kmp.data.platform

import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

class IosPersonalDataExporter : PersonalDataExporter {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareJson(filename: String, content: String): Boolean {
        val presenter = topViewController() ?: return false
        val activityController = UIActivityViewController(
            activityItems = listOf(content),
            applicationActivities = null,
        )
        presenter.presentViewController(activityController, animated = true, completion = null)
        return true
    }

    override fun shareText(chooserTitle: String, message: String): Boolean {
        val presenter = topViewController() ?: return false
        val activityController = UIActivityViewController(
            activityItems = listOf(message),
            applicationActivities = null,
        )
        presenter.presentViewController(activityController, animated = true, completion = null)
        return true
    }

    @OptIn(ExperimentalForeignApi::class)
    @Suppress("DEPRECATION")
    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
        while (controller.presentedViewController != null) {
            controller = controller.presentedViewController!!
        }
        return controller
    }
}
