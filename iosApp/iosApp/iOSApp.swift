import SwiftUI
import UIKit
import ComposeApp

final class AppDelegate: NSObject, UIApplicationDelegate {
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
}

@main
struct iOSApp: App {
	@UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in
					AuthRedirectBridge.shared.handle(url: url.absoluteString)
				}
		}
	}
}
