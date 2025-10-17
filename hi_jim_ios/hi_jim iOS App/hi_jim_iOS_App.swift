//
//  hi_jim_iOS_App.swift
//  hi_jim iOS App
//
//  iPhone companion app for notification support
//

import SwiftUI
import FirebaseCore
import FirebaseMessaging

@main
struct hi_jim_iOS_App: App {
    @UIApplicationDelegateAdaptor(iOSAppDelegate.self) var appDelegate

    init() {
        // Firebase 초기화
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
