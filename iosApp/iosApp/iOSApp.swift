import SwiftUI

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			ContentView()
				.onOpenURL { url in
					AuthRedirectBridge.shared.handle(url: url.absoluteString)
				}
		}
	}
}
