import SwiftUI
import UIKit
import UserNotifications
import ComposeApp

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
	func application(
		_ application: UIApplication,
		didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
	) -> Bool {
		UNUserNotificationCenter.current().delegate = self
		return true
	}

	func application(
		_ application: UIApplication,
		didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
	) {
		let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
		PushTokenBridge.shared.register(token: token)
	}

	func application(
		_ application: UIApplication,
		didFailToRegisterForRemoteNotificationsWithError error: Error
	) {
		print("apns_register_failed: \(error.localizedDescription)")
	}

	func userNotificationCenter(
		_ center: UNUserNotificationCenter,
		didReceive response: UNNotificationResponse,
		withCompletionHandler completionHandler: @escaping () -> Void
	) {
		defer { completionHandler() }
		guard let token = response.notification.request.content.userInfo["invite_token"] as? String,
		      !token.isEmpty else {
			return
		}
		let url = "app.mymultiverse.kmp://invite?token=\(token)"
		InviteRedirectBridge.shared.handle(url: url)
	}
}

@main
struct iOSApp: App {
	@UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in
					AuthRedirectBridge.shared.handle(url: url.absoluteString)
					InviteRedirectBridge.shared.handle(url: url.absoluteString)
				}
		}
	}
}
