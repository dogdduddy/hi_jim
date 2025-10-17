//
//  hi_jimApp.swift
//  hi_jim Watch App
//
//  Created by Jim의 Mac on 10/16/25.
//

import SwiftUI
import FirebaseCore

@main
struct hi_jim_Watch_AppApp: App {
    @WKApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // Firebase 초기화
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            GameLobbyView()
        }
    }
}
