//
//  MultiplayerMukjjippaViewModel.swift
//  hi_jim Watch App
//
//  묵찌빠 멀티플레이어 게임 상태 관리
//

import Foundation
import Combine

@MainActor
class MultiplayerMukjjippaViewModel: ObservableObject {
    @Published var gameData: MultiplayerMukjjippaData?
    @Published var shouldExitToLobby = false

    private let repository = FirebaseGameRepository()
    private var cancellables = Set<AnyCancellable>()
    private var countdownTask: Task<Void, Never>?
    private var resultProcessingTask: Task<Void, Never>?

    let gameId: String
    let currentUserId: String

    init(gameId: String, currentUserId: String = UserConstants.CURRENT_USER_ID) {
        self.gameId = gameId
        self.currentUserId = currentUserId
        print("🟢 [MukjjippaVM] Initialized with gameId: \(gameId), currentUserId: \(currentUserId)")
        observeGameState()
    }

    // MARK: - Observe Game State

    private func observeGameState() {
        print("🟢 [MukjjippaVM] Starting to observe game state for gameId: \(gameId)")
        repository.observeMukjjippaGameState(gameId: gameId)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { completion in
                    switch completion {
                    case .finished:
                        print("🟢 [MukjjippaVM] Observer completed")
                    case .failure(let error):
                        print("🔴 [MukjjippaVM] Observer error: \(error.localizedDescription)")
                    }
                },
                receiveValue: { [weak self] data in
                    guard let self = self else { return }

                    if let data = data {
                        print("🟢 [MukjjippaVM] Received game data: gameId=\(data.gameId), phase=\(data.phase), bothPlayersReady=\(data.bothPlayersReady)")
                        self.gameData = data
                        self.processGameLogic(data: data)
                    } else {
                        print("🔴 [MukjjippaVM] Received nil game data! Game was deleted, exiting to lobby...")
                        self.gameData = nil
                        self.shouldExitToLobby = true
                    }
                }
            )
            .store(in: &cancellables)
    }

    // MARK: - Game Logic Processing

    private func processGameLogic(data: MultiplayerMukjjippaData) {
        let gameState = data.toMukjjippaGameState()

        print("processGameLogic called: phase=\(gameState.phase), isGameFinished=\(gameState.isGameFinished), countdownState=\(gameState.countdownState)")

        // 게임이 종료되었으면 모든 진행 중인 작업 취소
        if gameState.isGameFinished || gameState.phase == .GAME_OVER {
            print("Game finished or GAME_OVER phase, cancelling tasks and returning")
            countdownTask?.cancel()
            resultProcessingTask?.cancel()
            return
        }

        // 양쪽 플레이어가 모두 참여했고, 대기 상태이며, 게임이 종료되지 않았을 때만 카운트다운 시작 (player1만)
        if gameState.bothPlayersReady &&
            gameState.countdownState == .WAITING &&
            !gameState.isGameFinished &&
            currentUserId == "user_jim" {  // Jim(player1)만 카운트다운 시작
            // 이미 카운트다운이 진행 중이면 무시
            if countdownTask != nil && !(countdownTask?.isCancelled ?? true) {
                return
            }

            startCountdown(gameState: gameState)
        }

        // 양쪽 플레이어가 모두 선택했고, 결과 대기 상태라면 먼저 상대방 선택 표시 (player1만)
        if gameState.isChoiceComplete() &&
            gameState.countdownState == .RESULT_WAIT &&
            currentUserId == "user_jim" {  // Jim(player1)만 처리
            // 이미 처리 중이면 무시
            if resultProcessingTask != nil && !(resultProcessingTask?.isCancelled ?? true) {
                return
            }

            resultProcessingTask = Task {
                try? await Task.sleep(nanoseconds: 1_000_000_000)  // 1초 대기
                // 상대방 선택 표시
                var updatedState = gameState
                updatedState.countdownState = .SHOWING_RESULT
                await updateGameState(gameState: updatedState)
            }
        }

        // 상대방 선택 표시 상태에서 2초 후 결과 처리 (player1만 처리)
        if gameState.isChoiceComplete() &&
            gameState.countdownState == .SHOWING_RESULT &&
            currentUserId == "user_jim" {  // Jim(player1)만 결과 처리
            print("SHOWING_RESULT condition met")
            // 이미 처리 중이면 무시
            if resultProcessingTask != nil && !(resultProcessingTask?.isCancelled ?? true) {
                return
            }

            resultProcessingTask = Task {
                print("Starting 2 second delay before processing result")
                try? await Task.sleep(nanoseconds: 2_000_000_000)  // 2초 동안 상대방 선택 표시
                // 현재 상태를 다시 가져와서 처리
                if let currentGameState = self.gameData?.toMukjjippaGameState(),
                   currentGameState.countdownState == .SHOWING_RESULT &&
                   !currentGameState.isGameFinished {
                    print("Calling processGameResult")
                    await processGameResult(gameState: currentGameState)
                } else {
                    print("NOT calling processGameResult - conditions not met")
                }
            }
        }
    }

    // MARK: - Countdown

    private func startCountdown(gameState: MukjjippaGameState) {
        countdownTask = Task {
            let messages: [String]
            if gameState.phase == .ROCK_PAPER_SCISSORS {
                messages = ["가위", "바위", "보"]
            } else {
                let prevChoice = gameState.previousAttackerChoice ?? .ROCK
                messages = [prevChoice.getCountdownMessage(), prevChoice.getCountdownMessage(), ""]
            }

            // 첫 번째 메시지
            var state1 = gameState
            state1.countdownState = .COUNTDOWN_1
            state1.currentMessage = messages[0]
            await updateGameState(gameState: state1)
            try? await Task.sleep(nanoseconds: 1_000_000_000)  // 1초 표시

            // 메시지 사라짐
            state1.currentMessage = ""
            await updateGameState(gameState: state1)
            try? await Task.sleep(nanoseconds: 500_000_000)  // 0.5초 대기

            // 두 번째 메시지
            var state2 = gameState
            state2.countdownState = .COUNTDOWN_2
            state2.currentMessage = messages[1]
            await updateGameState(gameState: state2)
            try? await Task.sleep(nanoseconds: 1_000_000_000)  // 1초 표시

            // 메시지 사라짐
            state2.currentMessage = ""
            await updateGameState(gameState: state2)
            try? await Task.sleep(nanoseconds: 500_000_000)  // 0.5초 대기

            // 세 번째 메시지 (가위바위보의 경우)
            if gameState.phase == .ROCK_PAPER_SCISSORS {
                var state3 = gameState
                state3.countdownState = .COUNTDOWN_3
                state3.currentMessage = messages[2]
                await updateGameState(gameState: state3)
                try? await Task.sleep(nanoseconds: 1_000_000_000)  // 1초 표시

                // 메시지 사라짐
                state3.currentMessage = ""
                await updateGameState(gameState: state3)
                try? await Task.sleep(nanoseconds: 500_000_000)  // 0.5초 대기
            }

            // 결과 대기 상태로 전환
            var stateResult = gameState
            stateResult.countdownState = .RESULT_WAIT
            stateResult.currentMessage = ""
            await updateGameState(gameState: stateResult)
        }
    }

    // MARK: - Process Game Result

    private func processGameResult(gameState: MukjjippaGameState) async {
        guard gameState.isChoiceComplete() else { return }

        guard let jimChoice = gameState.jimChoice,
              let hiChoice = gameState.hiChoice else { return }

        print("processGameResult: phase=\(gameState.phase), jimChoice=\(jimChoice), hiChoice=\(hiChoice), attackerId=\(gameState.attackerId ?? "nil")")

        let newGameState: MukjjippaGameState

        switch gameState.phase {
        case .ROCK_PAPER_SCISSORS:
            if jimChoice == hiChoice {
                // 무승부, 다시 가위바위보
                newGameState = gameState.resetChoices().withBothPlayersReady(true)
            } else if jimChoice.beats(hiChoice) {
                // Jim이 공격자가 됨
                newGameState = MukjjippaGameState(
                    phase: .MUKJJIPPA,
                    countdownState: .WAITING,
                    currentMessage: "",
                    jimScore: gameState.jimScore,
                    hiScore: gameState.hiScore,
                    jimChoice: nil,
                    hiChoice: nil,
                    attackerId: "user_jim",
                    previousAttackerChoice: jimChoice,
                    winner: nil,
                    isGameFinished: false,
                    bothPlayersReady: true
                )
            } else {
                // Hi가 공격자가 됨
                newGameState = MukjjippaGameState(
                    phase: .MUKJJIPPA,
                    countdownState: .WAITING,
                    currentMessage: "",
                    jimScore: gameState.jimScore,
                    hiScore: gameState.hiScore,
                    jimChoice: nil,
                    hiChoice: nil,
                    attackerId: "user_girlfriend",
                    previousAttackerChoice: hiChoice,
                    winner: nil,
                    isGameFinished: false,
                    bothPlayersReady: true
                )
            }

        case .MUKJJIPPA:
            if jimChoice == hiChoice {
                print("VICTORY CONDITION: Same choices in MUKJJIPPA phase! Winner: \(gameState.attackerId ?? "nil")")
                // 공격자가 승리
                newGameState = MukjjippaGameState(
                    phase: .GAME_OVER,
                    countdownState: .WAITING,
                    currentMessage: "",
                    jimScore: gameState.jimScore,
                    hiScore: gameState.hiScore,
                    jimChoice: nil,
                    hiChoice: nil,
                    attackerId: gameState.attackerId,
                    previousAttackerChoice: gameState.previousAttackerChoice,
                    winner: gameState.attackerId,
                    isGameFinished: true,
                    bothPlayersReady: false
                )
            } else if gameState.attackerId == "user_jim" {
                if jimChoice.beats(hiChoice) {
                    // Jim이 계속 공격
                    newGameState = MukjjippaGameState(
                        phase: .MUKJJIPPA,
                        countdownState: .WAITING,
                        currentMessage: "",
                        jimScore: gameState.jimScore,
                        hiScore: gameState.hiScore,
                        jimChoice: nil,
                        hiChoice: nil,
                        attackerId: "user_jim",
                        previousAttackerChoice: jimChoice,
                        winner: nil,
                        isGameFinished: false,
                        bothPlayersReady: true
                    )
                } else {
                    // Hi가 공격자가 됨
                    newGameState = MukjjippaGameState(
                        phase: .MUKJJIPPA,
                        countdownState: .WAITING,
                        currentMessage: "",
                        jimScore: gameState.jimScore,
                        hiScore: gameState.hiScore,
                        jimChoice: nil,
                        hiChoice: nil,
                        attackerId: "user_girlfriend",
                        previousAttackerChoice: hiChoice,
                        winner: nil,
                        isGameFinished: false,
                        bothPlayersReady: true
                    )
                }
            } else {
                if hiChoice.beats(jimChoice) {
                    // Hi가 계속 공격
                    newGameState = MukjjippaGameState(
                        phase: .MUKJJIPPA,
                        countdownState: .WAITING,
                        currentMessage: "",
                        jimScore: gameState.jimScore,
                        hiScore: gameState.hiScore,
                        jimChoice: nil,
                        hiChoice: nil,
                        attackerId: "user_girlfriend",
                        previousAttackerChoice: hiChoice,
                        winner: nil,
                        isGameFinished: false,
                        bothPlayersReady: true
                    )
                } else {
                    // Jim이 공격자가 됨
                    newGameState = MukjjippaGameState(
                        phase: .MUKJJIPPA,
                        countdownState: .WAITING,
                        currentMessage: "",
                        jimScore: gameState.jimScore,
                        hiScore: gameState.hiScore,
                        jimChoice: nil,
                        hiChoice: nil,
                        attackerId: "user_jim",
                        previousAttackerChoice: jimChoice,
                        winner: nil,
                        isGameFinished: false,
                        bothPlayersReady: true
                    )
                }
            }

        case .GAME_OVER:
            newGameState = gameState
        }

        print("processGameResult complete: newGameState phase=\(newGameState.phase), isGameFinished=\(newGameState.isGameFinished), winner=\(newGameState.winner ?? "nil")")
        await updateGameState(gameState: newGameState)
    }

    // MARK: - User Actions

    func makeChoice(_ choice: MukjjippaChoice) {
        guard let currentData = gameData else { return }
        let gameState = currentData.toMukjjippaGameState()

        // 게임이 종료되었으면 선택할 수 없음
        if gameState.isGameFinished {
            return
        }

        // 결과 표시 중에는 선택할 수 없음
        if gameState.countdownState == .SHOWING_RESULT {
            return
        }

        // 현재 플레이어가 이미 선택했는지 확인
        let currentPlayerChoice = gameState.getChoiceForPlayer(currentUserId)

        // 이미 선택했고 결과 대기 중이면 변경 불가
        if currentPlayerChoice != nil && gameState.countdownState == .RESULT_WAIT {
            return
        }

        var updatedGameState = gameState
        if currentUserId == "user_jim" {
            updatedGameState.jimChoice = choice
        } else {
            updatedGameState.hiChoice = choice
        }

        Task {
            await updateGameState(gameState: updatedGameState)
        }
    }

    func restartGame() {
        guard let currentData = gameData else { return }
        let winner = currentData.winner

        let newJimScore = winner == "user_jim" ? currentData.jimScore + 1 : currentData.jimScore
        let newHiScore = winner == "user_girlfriend" ? currentData.hiScore + 1 : currentData.hiScore

        let newGameState = MukjjippaGameState(
            jimScore: newJimScore,
            hiScore: newHiScore,
            bothPlayersReady: true
        )

        Task {
            await updateGameState(gameState: newGameState)
        }
    }

    func quitGame() async {
        try? await repository.endMukjjippaGame(gameId: gameId)
    }

    // MARK: - Update State

    private func updateGameState(gameState: MukjjippaGameState) async {
        guard let currentData = gameData else { return }

        print("updateGameState called: phase=\(gameState.phase), isGameFinished=\(gameState.isGameFinished), countdownState=\(gameState.countdownState)")

        let updatedData = MultiplayerMukjjippaData.fromMukjjippaGameState(
            gameId: gameId,
            player1Id: currentData.player1Id,
            player2Id: currentData.player2Id,
            state: gameState,
            lastMovePlayerId: currentUserId
        )

        do {
            try await repository.updateMukjjippaGameState(updatedData)
            print("updateMukjjippaGameState completed")
        } catch {
            print("Error updating game state: \(error)")
        }
    }
}

// Helper extension
extension MukjjippaGameState {
    func withBothPlayersReady(_ ready: Bool) -> MukjjippaGameState {
        var newState = self
        newState.bothPlayersReady = ready
        return newState
    }
}
