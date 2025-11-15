//
//  MultiplayerMukjjippaGameView.swift
//  hi_jim Watch App
//
//  묵찌빠 멀티플레이어 게임 화면
//

import SwiftUI

struct MultiplayerMukjjippaGameView: View {
    @StateObject private var viewModel: MultiplayerMukjjippaViewModel
    @State private var showTimeoutError = false
    let onDismiss: () -> Void

    init(gameId: String, onDismiss: @escaping () -> Void) {
        print("🟣 [MukjjippaGameView] Initializing with gameId: \(gameId)")
        self._viewModel = StateObject(wrappedValue: MultiplayerMukjjippaViewModel(gameId: gameId))
        self.onDismiss = onDismiss
    }

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if let gameData = viewModel.gameData {
                let gameState = gameData.toMukjjippaGameState()
                let _ = print("🟣 [MukjjippaGameView] Rendering with gameData: phase=\(gameState.phase)")

                VStack(spacing: 0) {
                    // 뒤로가기 버튼
                    HStack {
                        Button(action: {
                            Task {
                                await viewModel.quitGame()
                                onDismiss()
                            }
                        }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 24, height: 24)
                                .background(Color(red: 0.3, green: 0.3, blue: 0.3))
                                .cornerRadius(12)
                        }
                        .buttonStyle(.plain)

                        Spacer()
                    }
                    .padding(.horizontal, 8)
                    .padding(.top, 4)

                    // 상단 영역 (스코어 + 공격자 정보)
                    topSection(gameState: gameState)
                        .padding(.top, 4)

                    Spacer()

                    // 중앙 영역 (메시지 또는 결과)
                    centerSection(gameState: gameState)
                        .frame(height: 80)

                    Spacer()

                    // 하단 버튼 영역
                    if !gameState.isGameFinished {
                        bottomButtons(gameState: gameState)
                            .padding(.bottom, 8)
                    }
                }
                .padding(.horizontal, 8)
            } else {
                // 로딩 또는 타임아웃 에러
                VStack(spacing: 16) {
                    if showTimeoutError {
                        VStack(spacing: 12) {
                            Text("게임 로딩 실패")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.red)

                            Text("게임 데이터를 불러올 수 없습니다.\nXcode 콘솔을 확인해주세요.")
                                .font(.system(size: 11))
                                .foregroundColor(.gray)
                                .multilineTextAlignment(.center)

                            Button(action: { onDismiss() }) {
                                Text("돌아가기")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 8)
                                    .background(Color(red: 0.4, green: 0.4, blue: 0.4))
                                    .cornerRadius(8)
                            }
                            .buttonStyle(.plain)
                        }
                    } else {
                        ProgressView()
                            .scaleEffect(1.5)
                            .tint(.white)

                        Text("게임 로딩 중...")
                            .font(.system(size: 12))
                            .foregroundColor(.gray)
                            .padding(.top, 8)
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            print("🟣 [MukjjippaGameView] View appeared")
            // 10초 후에도 데이터가 없으면 타임아웃 에러 표시
            DispatchQueue.main.asyncAfter(deadline: .now() + 10) {
                if viewModel.gameData == nil && !viewModel.shouldExitToLobby {
                    print("🔴 [MukjjippaGameView] Timeout - no game data received after 10 seconds")
                    showTimeoutError = true
                }
            }
        }
        .onChange(of: viewModel.shouldExitToLobby) { shouldExit in
            if shouldExit {
                print("🟣 [MukjjippaGameView] shouldExitToLobby detected, dismissing view")
                onDismiss()
            }
        }
    }

    // MARK: - View Components

    private func topSection(gameState: MukjjippaGameState) -> some View {
        VStack(spacing: 4) {
            // 공격자 정보
            if gameState.phase == .MUKJJIPPA, let attackerId = gameState.attackerId {
                Text("\(displayName(for: attackerId))의 공격!")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.red)
            }

            // 스코어
            Text("Jim : \(gameState.jimScore)   Hi : \(gameState.hiScore)")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.white)
        }
    }

    private func centerSection(gameState: MukjjippaGameState) -> some View {
        VStack {
            if gameState.isGameFinished {
                // 게임 종료 - 승자 표시 및 버튼
                gameOverSection(gameState: gameState)
            } else if gameState.countdownState == .SHOWING_RESULT {
                // 상대방 선택 표시
                showingResultSection(gameState: gameState)
            } else if !gameState.currentMessage.isEmpty {
                // 카운트다운 메시지
                Text(gameState.currentMessage)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
            } else if !gameState.bothPlayersReady {
                // 상대방 대기 중
                Text("상대방을 기다리는 중...")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
        }
    }

    private func gameOverSection(gameState: MukjjippaGameState) -> some View {
        VStack(spacing: 8) {
            if let winner = gameState.winner {
                Text("\(displayName(for: winner))의 승리!")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.green)
            }

            HStack(spacing: 8) {
                Button(action: { viewModel.restartGame() }) {
                    Text("재시작")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .frame(height: 32)
                        .frame(maxWidth: .infinity)
                        .background(Color(red: 0.29, green: 0.57, blue: 0.89)) // #4A90E2
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)

                Button(action: {
                    Task {
                        await viewModel.quitGame()
                        onDismiss()
                    }
                }) {
                    Text("나가기")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .frame(height: 32)
                        .frame(maxWidth: .infinity)
                        .background(Color(red: 0.4, green: 0.4, blue: 0.4))
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func showingResultSection(gameState: MukjjippaGameState) -> some View {
        let currentUserId = viewModel.currentUserId
        let opponentChoice = currentUserId == "user_jim" ? gameState.hiChoice : gameState.jimChoice
        let myChoice = currentUserId == "user_jim" ? gameState.jimChoice : gameState.hiChoice

        return VStack(spacing: 4) {
            Text("상대방 선택:")
                .font(.system(size: 11))
                .foregroundColor(.white)

            HStack(spacing: 16) {
                Text(opponentChoice?.displayName ?? "")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.yellow)

                Text(myChoice?.displayName ?? "")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.cyan)
            }
        }
    }

    private func bottomButtons(gameState: MukjjippaGameState) -> some View {
        let currentUserId = viewModel.currentUserId
        let currentPlayerChoice = gameState.getChoiceForPlayer(currentUserId)

        // 버튼 활성화 조건
        let canInteract: Bool = {
            // 결과 표시 중에는 비활성화
            if gameState.countdownState == .SHOWING_RESULT {
                return false
            }

            // 이미 선택했고 결과 대기 중이면 비활성화
            if currentPlayerChoice != nil && gameState.countdownState == .RESULT_WAIT {
                return false
            }

            // 가위바위보: COUNTDOWN_3 이후부터 활성화 ("보!" 멘트 이후)
            if gameState.phase == .ROCK_PAPER_SCISSORS {
                return gameState.countdownState == .COUNTDOWN_3 || gameState.countdownState == .RESULT_WAIT
            }

            // 묵찌빠: COUNTDOWN_2 이후부터 활성화 (두 번째 멘트 이후)
            if gameState.phase == .MUKJJIPPA {
                return gameState.countdownState == .COUNTDOWN_2 || gameState.countdownState == .RESULT_WAIT
            }

            return false
        }()

        let isButtonEnabled = canInteract && gameState.bothPlayersReady

        return HStack(spacing: 6) {
            choiceButton(
                choice: .SCISSORS,
                isSelected: currentPlayerChoice == .SCISSORS,
                isEnabled: isButtonEnabled
            )

            choiceButton(
                choice: .ROCK,
                isSelected: currentPlayerChoice == .ROCK,
                isEnabled: isButtonEnabled
            )

            choiceButton(
                choice: .PAPER,
                isSelected: currentPlayerChoice == .PAPER,
                isEnabled: isButtonEnabled
            )
        }
    }

    private func choiceButton(choice: MukjjippaChoice, isSelected: Bool, isEnabled: Bool) -> some View {
        Button(action: { viewModel.makeChoice(choice) }) {
            Text(choice.displayName)
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(isEnabled ? .white : .gray)
                .frame(maxWidth: .infinity)
                .frame(height: 38)
                .background(isSelected ? Color(red: 0.29, green: 0.57, blue: 0.89) : Color(red: 0.16, green: 0.16, blue: 0.16))
                .cornerRadius(8)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
    }

    // MARK: - Helper Methods

    private func displayName(for userId: String) -> String {
        return userId == "user_jim" ? "Jim" : "Hi"
    }
}

#Preview {
    MultiplayerMukjjippaGameView(gameId: "test-game-id", onDismiss: {})
}
