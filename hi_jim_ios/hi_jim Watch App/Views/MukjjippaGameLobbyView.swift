//
//  MukjjippaGameLobbyView.swift
//  hi_jim Watch App
//
//  묵찌빠 게임 로비 UI
//

import SwiftUI

struct MukjjippaGameLobbyView: View {
    @StateObject private var viewModel = MukjjippaGameLobbyViewModel()

    var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 12) {
                    // 제목
                    Text("묵찌빠 게임 로비")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.top, 8)

                    // 게임 요청 보내기 버튼
                    Button(action: viewModel.sendGameRequest) {
                        Text("게임 요청하기")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 40)
                            .background(Color(red: 0.18, green: 0.55, blue: 0.34)) // #2E8B57
                            .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    // 보낸 요청 상태 표시
                    if let status = viewModel.sentRequestStatus {
                        sentRequestSection(status: status)
                    }

                    // 받은 요청 목록
                    if !viewModel.receivedRequests.isEmpty {
                        Text("받은 요청")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.31)) // #FFD54F
                            .padding(.top, 8)

                        ForEach(viewModel.receivedRequests) { request in
                            receivedRequestRow(request: request)
                        }
                    }

                    Spacer(minLength: 20)
                }
                .padding(.horizontal, 8)
            }

            // 로딩 오버레이
            if viewModel.isLoading {
                ProgressView()
                    .scaleEffect(1.5)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.3))
            }
        }
        .background(Color.black.ignoresSafeArea())
        .navigationDestination(item: $viewModel.currentGameId) { gameId in
            MultiplayerMukjjippaGameView(
                gameId: gameId,
                onDismiss: { viewModel.resetGame() }
            )
        }
    }

    // MARK: - View Components

    private func sentRequestSection(status: GameRequestStatus) -> some View {
        VStack(spacing: 8) {
            Text("보낸 요청")
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(Color(red: 1.0, green: 0.84, blue: 0.31))

            HStack {
                if status == .pending {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("대기 중...")
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                } else {
                    Text(statusText(for: status))
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                }

                Spacer()

                Button(action: viewModel.cancelSentRequest) {
                    Text("취소")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color(red: 0.8, green: 0.29, blue: 0.29)) // #CC4A4A
                        .cornerRadius(6)
                }
                .buttonStyle(.plain)
            }
            .padding(12)
            .background(Color(red: 0.15, green: 0.15, blue: 0.15))
            .cornerRadius(8)
        }
    }

    private func receivedRequestRow(request: GameRequest) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("받은 요청")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.white)

                Text(userDisplayName(userId: request.fromUserId))
                    .font(.system(size: 10))
                    .foregroundColor(.gray)
            }

            Spacer()

            HStack(spacing: 8) {
                Button(action: { viewModel.acceptRequest(request) }) {
                    Text("수락")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Color(red: 0.18, green: 0.55, blue: 0.34))
                        .cornerRadius(6)
                }
                .buttonStyle(.plain)

                Button(action: { viewModel.rejectRequest(request) }) {
                    Text("거절")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Color(red: 0.4, green: 0.4, blue: 0.4))
                        .cornerRadius(6)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(12)
        .background(Color(red: 0.15, green: 0.15, blue: 0.15))
        .cornerRadius(8)
    }

    // MARK: - Helper Methods

    private func statusText(for status: GameRequestStatus) -> String {
        switch status {
        case .pending: return "대기 중..."
        case .accepted: return "수락됨"
        case .rejected: return "거절됨"
        case .cancelled: return "취소됨"
        }
    }

    private func userDisplayName(userId: String) -> String {
        return userId == "user_jim" ? "Jim" : "Hi"
    }
}

#Preview {
    MukjjippaGameLobbyView()
}
