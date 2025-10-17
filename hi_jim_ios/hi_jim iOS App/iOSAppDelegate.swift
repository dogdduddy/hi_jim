//
//  iOSAppDelegate.swift
//  hi_jim iOS App
//
//  FCM 및 알림 처리
//

import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class iOSAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {

        // Firebase 초기화 (이미 되어 있을 수 있음)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }

        // FCM Delegate 설정
        Messaging.messaging().delegate = self

        // 알림 권한 요청
        UNUserNotificationCenter.current().delegate = self
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { granted, error in
                if let error = error {
                    print("❌ Notification permission error: \(error.localizedDescription)")
                } else if granted {
                    print("✅ Notification permission granted")
                } else {
                    print("⚠️ Notification permission denied")
                }
            }
        )

        // APNs 등록
        application.registerForRemoteNotifications()

        return true
    }

    // APNs 토큰 수신
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("📱 APNs token received")
        // FCM이 자동으로 APNs 토큰을 FCM 토큰으로 변환
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("❌ Failed to register for remote notifications: \(error.localizedDescription)")
    }

    // MARK: - MessagingDelegate

    // FCM 토큰 수신
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken = fcmToken else { return }

        print("📱 FCM token: \(fcmToken)")

        // FCM 토큰을 Firebase Database에 저장
        iOSFCMTokenManager.shared.saveToken(fcmToken)
    }

    // MARK: - UNUserNotificationCenterDelegate

    // 앱이 foreground에 있을 때 알림 수신
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        print("📩 Notification received in foreground")

        // iOS 14+ 스타일
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .sound, .badge])
        } else {
            completionHandler([.alert, .sound, .badge])
        }
    }

    // 알림 클릭 시
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("📱 Notification tapped: \(userInfo)")

        // 게임 요청 정보 추출
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
