//
//  ContentView.swift
//  hi_jim iOS App
//
//  알림 전용 companion 앱 메인 화면
//

import SwiftUI

struct ContentView: View {
    @State private var notificationStatus: String = "확인 중..."
    @State private var fcmToken: String = "토큰 로딩 중..."

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                // 앱 아이콘 영역
                Image(systemName: "applewatch")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)
                    .padding(.top, 40)

                VStack(spacing: 8) {
                    Text("Hi Jim - Sumo Game")
                        .font(.title2)
                        .fontWeight(.bold)

                    Text("Apple Watch Companion")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                Spacer()

                // 설명 섹션
                VStack(alignment: .leading, spacing: 16) {
                    InfoRow(
                        icon: "bell.fill",
                        title: "알림 지원",
                        description: "게임 요청 알림을 받을 수 있습니다"
                    )

                    InfoRow(
                        icon: "applewatch",
                        title: "Apple Watch 연동",
                        description: "알림이 자동으로 워치에 전달됩니다"
                    )

                    InfoRow(
                        icon: "gamecontroller.fill",
                        title: "게임 플레이",
                        description: "Apple Watch에서 게임을 즐기세요"
                    )
                }
                .padding(.horizontal)

                Spacer()

                // 알림 상태
                VStack(spacing: 12) {
                    HStack {
                        Circle()
                            .fill(notificationStatus == "활성화됨" ? Color.green : Color.orange)
                            .frame(width: 12, height: 12)

                        Text("알림 상태: \(notificationStatus)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Button(action: {
                        checkNotificationPermission()
                    }) {
                        Text("알림 권한 확인")
                            .font(.subheadline)
                            .foregroundColor(.blue)
                    }
                }
                .padding(.bottom, 40)
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                checkNotificationPermission()
            }
        }
    }

    private func checkNotificationPermission() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                switch settings.authorizationStatus {
                case .authorized, .provisional:
                    self.notificationStatus = "활성화됨"
                case .denied:
                    self.notificationStatus = "거부됨"
                case .notDetermined:
                    self.notificationStatus = "설정 필요"
                @unknown default:
                    self.notificationStatus = "알 수 없음"
                }
            }
        }
    }
}

struct InfoRow: View {
    let icon: String
    let title: String
    let description: String

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.blue)
                .frame(width: 32)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)

                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

#Preview {
    ContentView()
}
