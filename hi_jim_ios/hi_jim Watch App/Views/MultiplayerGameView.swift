//
//  MultiplayerGameView.swift
//  hi_jim Watch App
//
//  멀티플레이어 게임 화면
//

import SwiftUI
import Foundation

struct MultiplayerGameView: View {
    @StateObject private var viewModel: MultiplayerGameViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var buttonsEnabled = false

    init(gameId: String) {
        _viewModel = StateObject(wrappedValue: MultiplayerGameViewModel(gameId: gameId))
    }

    var body: some View {
        ZStack {
            if viewModel.gameState.gameStatus == .playing {
                gamePlayingView
            } else {
                gameResultView
            }
        }
        .navigationBarBackButtonHidden(true)
        .onChange(of: viewModel.gameState.gameStatus) { _, newStatus in
            // 게임 결과 화면으로 전환되면 1.5초 후 버튼 활성화
            if newStatus != .playing {
                buttonsEnabled = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    buttonsEnabled = true
                }
            }
        }
        .onChange(of: viewModel.shouldExitToLobby) { _, shouldExit in
            if shouldExit {
                dismiss()
            }
        }
        .alert("오류", isPresented: .constant(viewModel.errorMessage != nil)) {
            Button("확인") {
                viewModel.errorMessage = nil
            }
        } message: {
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
            }
        }
    }

    // 게임 진행 중 화면
    private var gamePlayingView: some View {
        GeometryReader { geometry in
            ZStack {
                // 배경 - 탭 감지용
                Color.black
                    .ignoresSafeArea()
                    .onTapGesture {
                        viewModel.onTap()
                    }

                // 게임 캔버스 (터치 이벤트 무시)
                gameCanvas(size: geometry.size)
                    .allowsHitTesting(false)

                // 상단 UI (뒤로가기 버튼 + 점수)
                VStack {
                    HStack {
                        // 뒤로가기 버튼
                        Button(action: {
                            Task {
                                await viewModel.exitGame()
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

                    scoreBoard

                    Spacer()
                }
                .allowsHitTesting(true)
            }
        }
    }

    // 게임 캔버스 (Android 원본 스타일 - 스모 링)
    private func gameCanvas(size: CGSize) -> some View {
        let gameState = viewModel.gameState
        let centerX = size.width / 2
        // 게임 보드를 약간 아래로 이동 (스코어 공간 확보)
        let centerY = size.height / 2 + 5

        // 링 반지름 (1.1배 확대)
        let ringRadius = size.width * 0.38 * 1.1

        // 좌표계: 원래 크기 기준 (캐릭터 크기 유지)
        let unitWidth = size.width / 24

        // 플레이어 반지름 (원래 크기)
        let playerRadiusPx = CGFloat(SumoPhysicsEngine.PLAYER_RADIUS) * unitWidth

        // Player 1 색상 (항상 파란색)
        let player1Color = Color(red: 0.42, green: 0.64, blue: 0.85) // #6BA3D8

        // Player 2 색상 (항상 빨간색/분홍색)
        let player2Color = Color(red: 0.91, green: 0.49, blue: 0.55) // #E87D8D

        return Canvas { context, canvasSize in
            // 1. 배경 - 경기장 (노란색 원)
            let ring = Path { path in
                path.addEllipse(in: CGRect(
                    x: centerX - ringRadius,
                    y: centerY - ringRadius,
                    width: ringRadius * 2,
                    height: ringRadius * 2
                ))
            }
            context.fill(ring, with: .color(Color(red: 1.0, green: 0.84, blue: 0.31))) // #FFD54F

            // 2. 경기장 테두리 (하얀색)
            context.stroke(ring, with: .color(.white), lineWidth: 8)

            // 3. 중앙선 (얇은 하얀선)
            let centerLine = Path { path in
                path.move(to: CGPoint(x: centerX, y: centerY - ringRadius * 0.6))
                path.addLine(to: CGPoint(x: centerX, y: centerY + ringRadius * 0.6))
            }
            context.stroke(centerLine, with: .color(.white.opacity(0.4)), lineWidth: 2)

            // 4. Player 1 (파란색)
            let p1X = centerX + CGFloat(gameState.player1Position) * unitWidth
            let p1Y = centerY

            // 그림자
            let p1Shadow = Path { path in
                path.addEllipse(in: CGRect(
                    x: p1X - playerRadiusPx + 3,
                    y: p1Y - playerRadiusPx + 3,
                    width: playerRadiusPx * 2,
                    height: playerRadiusPx * 2
                ))
            }
            context.fill(p1Shadow, with: .color(.black.opacity(0.15)))

            // 몸통
            let p1Circle = Path { path in
                path.addEllipse(in: CGRect(
                    x: p1X - playerRadiusPx,
                    y: p1Y - playerRadiusPx,
                    width: playerRadiusPx * 2,
                    height: playerRadiusPx * 2
                ))
            }
            context.fill(p1Circle, with: .color(player1Color))

            // 테두리
            context.stroke(p1Circle, with: .color(.black), lineWidth: 5)

            // 5. Player 2 (빨간색)
            let p2X = centerX + CGFloat(gameState.player2Position) * unitWidth
            let p2Y = centerY

            // 그림자
            let p2Shadow = Path { path in
                path.addEllipse(in: CGRect(
                    x: p2X - playerRadiusPx + 3,
                    y: p2Y - playerRadiusPx + 3,
                    width: playerRadiusPx * 2,
                    height: playerRadiusPx * 2
                ))
            }
            context.fill(p2Shadow, with: .color(.black.opacity(0.15)))

            // 몸통
            let p2Circle = Path { path in
                path.addEllipse(in: CGRect(
                    x: p2X - playerRadiusPx,
                    y: p2Y - playerRadiusPx,
                    width: playerRadiusPx * 2,
                    height: playerRadiusPx * 2
                ))
            }
            context.fill(p2Circle, with: .color(player2Color))

            // 테두리
            context.stroke(p2Circle, with: .color(.black), lineWidth: 5)

            // 6. 충돌 스파크 이펙트 (방사형 삼각형)
            if let collisionPos = gameState.collisionPosition,
               viewModel.collisionAlpha > 0 {
                print("✨ Drawing collision effect: alpha=\(viewModel.collisionAlpha), pos=\(collisionPos)")

                let sparkX = centerX + CGFloat(collisionPos) * unitWidth
                let sparkY = centerY

                // 방사형으로 퍼지는 삼각형들
                let sparkCount = 10
                let baseLength = playerRadiusPx * 0.83 * (1.0 + (1.0 - viewModel.collisionAlpha) * 0.3)
                let startOffset = baseLength * 0.5

                for i in 0..<sparkCount {
                    let angle = CGFloat(i) * 360.0 / CGFloat(sparkCount)
                    let angleRad = angle * .pi / 180.0

                    // 각 스파크의 길이를 랜덤하게
                    let lengthVariation: CGFloat = (i % 2 == 0) ? 1.0 : 0.7
                    let sparkLength = baseLength * lengthVariation

                    // 삼각형 Path
                    var spark = Path()
                    spark.move(to: CGPoint(x: startOffset, y: 0))

                    // 왼쪽 모서리
                    let leftAngle: CGFloat = -4.0 * .pi / 180.0
                    spark.addLine(to: CGPoint(
                        x: Darwin.cos(leftAngle) * (sparkLength + startOffset),
                        y: Darwin.sin(leftAngle) * (sparkLength + startOffset)
                    ))

                    // 오른쪽 모서리
                    let rightAngle: CGFloat = 4.0 * .pi / 180.0
                    spark.addLine(to: CGPoint(
                        x: Darwin.cos(rightAngle) * (sparkLength + startOffset),
                        y: Darwin.sin(rightAngle) * (sparkLength + startOffset)
                    ))

                    spark.closeSubpath()

                    // 회전 및 이동
                    context.translateBy(x: sparkX, y: sparkY)
                    context.rotate(by: Angle(degrees: Double(angle)))
                    context.fill(spark, with: .color(Color(red: 0.88, green: 0.88, blue: 0.88).opacity(viewModel.collisionAlpha * 0.9)))
                    context.rotate(by: Angle(degrees: -Double(angle)))
                    context.translateBy(x: -sparkX, y: -sparkY)
                }
            }
        }
    }

    // 점수판 (Android 원본 스타일)
    private var scoreBoard: some View {
        HStack(spacing: 12) {
            // Player 1 스코어 (이모지 포함)
            Text("🔵 \(viewModel.gameState.player1Score)")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(Color(red: 0.29, green: 0.56, blue: 0.89)) // #4A90E2

            Spacer()

            // Player 2 스코어 (이모지 포함)
            Text("🔴 \(viewModel.gameState.player2Score)")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(Color(red: 0.91, green: 0.36, blue: 0.46)) // #E85D75
        }
        .padding(.horizontal, 8)
        .padding(.top, 0)
        .padding(.bottom, 2)
    }

    // 게임 결과 화면 (Android 원본 스타일)
    private var gameResultView: some View {
        let winnerInfo = getWinnerInfo()

        return VStack(spacing: 14) {
            // 승자 표시 (Android 스타일)
            Text(winnerInfo.text)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(winnerInfo.color)
                .multilineTextAlignment(.center)
                .lineSpacing(2)

            Spacer().frame(height: 0)

            // 버튼들 (Android 스타일)
            VStack(spacing: 6) {
                Button(action: {
                    if buttonsEnabled {
                        viewModel.restartRound()
                    }
                }) {
                    Text("NEXT ROUND")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 35)
                        .background(buttonsEnabled ? Color(red: 0.3, green: 0.69, blue: 0.31) : Color.gray.opacity(0.5)) // #4CAF50 or disabled
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
                .disabled(!buttonsEnabled)

                Button(action: {
                    if buttonsEnabled {
                        Task {
                            await viewModel.exitGame()
                        }
                    }
                }) {
                    Text("QUIT GAME")
                        .font(.system(size: 10, weight: .regular))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 28)
                        .background(buttonsEnabled ? Color.gray : Color.gray.opacity(0.5))
                        .cornerRadius(8)
                }
                .buttonStyle(.plain)
                .disabled(!buttonsEnabled)
            }
            .padding(.horizontal, 20)
        }
        .padding()
    }

    private func getWinnerInfo() -> (text: String, color: Color) {
        if viewModel.gameState.gameStatus == .player1Win {
            return ("Player 1\nWins!", Color(red: 0.29, green: 0.56, blue: 0.89)) // #4A90E2
        } else {
            return ("Player 2\nWins!", Color(red: 0.91, green: 0.36, blue: 0.46)) // #E85D75
        }
    }
}

#Preview {
    MultiplayerGameView(gameId: "test_game_id")
}
