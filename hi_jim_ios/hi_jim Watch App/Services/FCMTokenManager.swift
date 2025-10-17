//
//  FCMTokenManager.swift
//  hi_jim Watch App
//
//  FCM 토큰 관리
//

import Foundation
import FirebaseDatabase

class FCMTokenManager {
    static let shared = FCMTokenManager()

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

    /// 상대방의 FCM 토큰 가져오기
    func fetchToken(for userId: String) async throws -> String? {
        let ref = Database.database().reference().child("users/\(userId)/fcmToken")

        let snapshot = try await ref.getData()
        return snapshot.value as? String
    }
}
