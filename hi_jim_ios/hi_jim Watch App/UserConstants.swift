//
//  UserConstants.swift
//  hi_jim (Shared)
//
//  iOS와 Watch 앱 모두에서 사용하는 사용자 상수
//

import Foundation

// MARK: - User Constants
struct UserConstants {
    static let USER_1 = "user_jim"
    static let USER_2 = "user_girlfriend"

    // 각 기기에서 다르게 설정해야 함
    static var CURRENT_USER_ID: String {
        get {
            UserDefaults.standard.string(forKey: "currentUserId") ?? USER_2
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "currentUserId")
        }
    }

    static var opponentUserId: String {
        return CURRENT_USER_ID == USER_1 ? USER_2 : USER_1
    }
}
