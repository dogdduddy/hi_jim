//
//  MultiplayerGameViewModel.swift
//  hi_jim Watch App
//
//  멀티플레이어 게임 상태 관리
//

import Foundation
import SwiftUI
import Combine

@MainActor
class MultiplayerGameViewModel: ObservableObject {
    @Published var gameState: SumoGameState
    @Published var errorMessage: String?
    @Published var shouldExitToLobby = false
    @Published var collisionAlpha: Double = 0.0

    private let repository = FirebaseGameRepository()
    private var cancellables = Set<AnyCancellable>()
    private let gameId: String
    private let currentUserId: String
    private var gameData: MultiplayerGameData?

    // 현재 플레이어가 player1인지 player2인지
    var isPlayer1: Bool {
        guard let gameData = gameData else { return false }
        return currentUserId == gameData.player1Id
    }

    // 현재 플레이어의 player id ("player1" or "player2")
    var currentPlayerId: String {
        isPlayer1 ? "player1" : "player2"
    }

    init(gameId: String) {
        self.gameId = gameId
        self.currentUserId = UserConstants.CURRENT_USER_ID
        self.gameState = SumoGameState()

        observeGameState()
    }

    // MARK: - Observe Game State

    private func observeGameState() {
        repository.observeGameState(gameId: gameId)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    if case .failure(let error) = completion {
                        self?.errorMessage = "게임 상태 감지 오류: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] gameData in
                    guard let self = self else { return }

                    if let gameData = gameData {
                        self.gameData = gameData
                        let newState = gameData.toSumoGameState()

                        // 충돌 타임스탬프가 변경되면 애니메이션 시작
                        if newState.collisionTimestamp != self.gameState.collisionTimestamp,
                           newState.collisionPosition != nil {
                            print("💥 Collision detected! timestamp=\(newState.collisionTimestamp), position=\(newState.collisionPosition ?? 0)")
                            self.startCollisionAnimation()
                        }

                        self.gameState = newState
                    } else {
                        // 게임이 삭제됨 (상대가 나갔을 경우)
                        self.shouldExitToLobby = true
                    }
                }
            )
            .store(in: &cancellables)
    }

    // 충돌 애니메이션 시작 (800ms 동안 부드럽게 페이드아웃)
    private func startCollisionAnimation() {
        print("🎆 Collision animation started")

        // 즉시 alpha를 1.0으로 설정
        collisionAlpha = 1.0

        // 100ms 동안 최대 밝기 유지 후, 700ms 동안 부드럽게 페이드아웃
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            withAnimation(.easeOut(duration: 0.7)) {
                self.collisionAlpha = 0.0
            }
        }
    }

    // MARK: - Actions

    // 플레이어 이동
    func onTap() {
        // 게임 중이 아니면 무시
        guard gameState.gameStatus == .playing else { return }

        Task {
            do {
                try await repository.sendPlayerMove(gameId: gameId, playerId: currentPlayerId)
            } catch {
                errorMessage = "이동 전송 실패: \(error.localizedDescription)"
            }
        }
    }

    // 라운드 재시작 (승부가 난 후)
    func restartRound() {
        Task {
            do {
                try await repository.resetRound(gameId: gameId)
            } catch {
                errorMessage = "라운드 재시작 실패: \(error.localizedDescription)"
            }
        }
    }

    // 게임 종료
    func exitGame() {
        Task {
            do {
                try await repository.endGame(gameId: gameId)
                shouldExitToLobby = true
            } catch {
                errorMessage = "게임 종료 실패: \(error.localizedDescription)"
            }
        }
    }

}
