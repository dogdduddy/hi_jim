//
//  GameLobbyView.swift
//  hi_jim Watch App
//
//  게임 로비 UI
//

import SwiftUI

struct GameLobbyView: View {
    @StateObject private var viewModel = GameLobbyViewModel()
    @State private var showUserSetup = false

    var body: some View {
        NavigationStack {
            ZStack {
                ScrollView {
                    VStack(spacing: 12) {
                        // 제목
                        Text("스모 게임 로비")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.top, 16)

                        // 게임 요청 보내기 버튼 (Android와 동일한 스타일)
                        Button(action: viewModel.sendGameRequest) {
                            Text("게임 요청하기")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 40)
                                .background(Color(red: 0.3, green: 0.69, blue: 0.31)) // #4CAF50
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
                        } else {
                            Text("받은 요청이 없습니다")
                                .font(.system(size: 11))
                                .foregroundColor(.gray)
                                .padding(.top, 12)
                        }

                        Spacer()
                    }
                    .padding(.horizontal, 12)
                }
                .background(Color.black)

                // 로딩 인디케이터
                if viewModel.isLoading {
                    ProgressView()
                        .scaleEffect(1.5)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black.opacity(0.5))
                }
            }
            .navigationTitle("스모 게임")
            .navigationBarTitleDisplayMode(.inline)
            .alert("오류", isPresented: .constant(viewModel.errorMessage != nil)) {
                Button("확인") {
                    viewModel.errorMessage = nil
                }
            } message: {
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                }
            }
            .navigationDestination(item: $viewModel.currentGameId) { gameId in
                MultiplayerGameView(gameId: gameId)
                    .onDisappear {
                        viewModel.resetGame()
                    }
            }
            .sheet(isPresented: $showUserSetup) {
                UserSetupView(isPresented: $showUserSetup)
            }
        }
    }

    // 보낸 요청 상태 섹션 (Android와 비슷한 스타일)
    @ViewBuilder
    private func sentRequestSection(status: GameRequestStatus) -> some View {
        let statusInfo = getStatusInfo(status)

        VStack(spacing: 6) {
            Text(statusInfo.text)
                .font(.system(size: 11))
                .foregroundColor(statusInfo.color)

            // 대기 중일 때만 취소 버튼 표시
            if status == .pending {
                Button {
                    viewModel.cancelSentRequest()
                } label: {
                    Text("취소")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 90, height: 30)
                        .background(Color(red: 1.0, green: 0.6, blue: 0.0)) // #FF9800
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func getStatusInfo(_ status: GameRequestStatus) -> (text: String, color: Color) {
        switch status {
        case .pending:
            return ("요청 대기 중...", .yellow)
        case .accepted:
            return ("요청 수락됨!", .green)
        case .rejected:
            return ("요청 거절됨", .red)
        @unknown default:
            return ("", .white)
        }
    }

    // 받은 요청 행 (Android와 비슷한 스타일)
    @ViewBuilder
    private func receivedRequestRow(request: GameRequest) -> some View {
        VStack(spacing: 6) {
            // 요청자 정보
            Text(request.fromUserId == UserConstants.USER_1 ? "Jim" : "Hi")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.white)

            // 수락/거절 버튼
            HStack(spacing: 8) {
                Button {
                    viewModel.acceptRequest(request)
                } label: {
                    Text("수락")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 35)
                        .background(Color(red: 0.3, green: 0.69, blue: 0.31)) // #4CAF50
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)

                Button {
                    viewModel.rejectRequest(request)
                } label: {
                    Text("거절")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 35)
                        .background(Color(red: 0.91, green: 0.36, blue: 0.46)) // #E85D75
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 4)
    }
}

// String을 Identifiable로 만들기 위한 extension
extension String: Identifiable {
    public var id: String { self }
}

#Preview {
    GameLobbyView()
}
