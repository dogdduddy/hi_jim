//
//  AppDelegate.swift
//  hi_jim Watch App
//
//  Firebase 및 FCM 초기화
//

import SwiftUI
import FirebaseCore
import UserNotifications

class AppDelegate: NSObject, WKApplicationDelegate {
    func applicationDidFinishLaunching() {
        // Firebase 초기화 (이미 되어 있을 수 있음)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }

        // 알림 권한 요청
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                print("❌ Notification permission error: \(error.localizedDescription)")
            } else if granted {
                print("✅ Notification permission granted")
                DispatchQueue.main.async {
                    WKApplication.shared().registerForRemoteNotifications()
                }
            } else {
                print("⚠️ Notification permission denied")
            }
        }

        // Notification Delegate 설정
        UNUserNotificationCenter.current().delegate = self
    }

    // APNs 토큰 수신
    func didRegisterForRemoteNotifications(withDeviceToken deviceToken: Data) {
        let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("📱 APNs token: \(tokenString)")

        // FCM 토큰으로 변환하여 저장
        // watchOS에서는 FCM을 직접 사용하기 어려우므로 APNs 토큰을 저장
        FCMTokenManager.shared.saveToken(tokenString)
    }

    func didFailToRegisterForRemoteNotificationsWithError(_ error: Error) {
        print("❌ Failed to register for remote notifications: \(error.localizedDescription)")
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension AppDelegate: UNUserNotificationCenterDelegate {
    // 앱이 foreground에 있을 때 알림 수신
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        print("📩 Notification received in foreground")
        completionHandler([.banner, .sound])
    }

    // 알림 클릭 시
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("📱 Notification tapped: \(userInfo)")

        // TODO: 게임 요청 화면으로 이동하는 로직 추가
        if let requestId = userInfo["requestId"] as? String,
           let fromUserId = userInfo["fromUserId"] as? String {
            print("🎮 Game request from \(fromUserId), requestId: \(requestId)")
            // NotificationCenter를 통해 앱에 알림 전달
            NotificationCenter.default.post(
                name: NSNotification.Name("GameRequestReceived"),
                object: nil,
                userInfo: ["requestId": requestId, "fromUserId": fromUserId]
            )
        }

        completionHandler()
    }
}
