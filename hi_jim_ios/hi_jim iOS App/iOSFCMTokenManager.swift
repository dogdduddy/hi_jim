//
//  iOSFCMTokenManager.swift
//  hi_jim iOS App
//
//  iPhone FCM 토큰 관리
//

import Foundation
import FirebaseDatabase

class iOSFCMTokenManager {
    static let shared = iOSFCMTokenManager()

    private init() {}

    /// FCM 토큰을 Firebase Database에 저장
    /// 경로: users/{userId}/fcmToken
    func saveToken(_ token: String) {
        let userId = UserConstants.CURRENT_USER_ID
        let ref = Database.database().reference().child("users/\(userId)/fcmToken")

        ref.setValue(token) { error, _ in
            if let error = error {
                print("❌ Failed to save FCM token: \(error.localizedDescription)")
            } else {
                print("✅ FCM token saved successfully: \(token)")
            }
        }
    }
}
