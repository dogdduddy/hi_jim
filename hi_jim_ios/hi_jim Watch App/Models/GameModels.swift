//
//  GameModels.swift
//  hi_jim Watch App
//
//  KMP 공유 모듈의 모델들을 Swift로 재정의
//

import Foundation

// MARK: - Game Status
enum GameStatus: String, Codable {
    case waiting = "WAITING"
    case playing = "PLAYING"
    case player1Win = "PLAYER1_WIN"
    case player2Win = "PLAYER2_WIN"
}

// MARK: - Sumo Game State
struct SumoGameState {
    var player1Position: Float
    var player2Position: Float
    var player1Velocity: Float
    var player2Velocity: Float
    var gameStatus: GameStatus
    var lastUpdateTime: Int64
    var player1Score: Int
    var player2Score: Int
    var collisionPosition: Float?
    var collisionTimestamp: Int64

    init(
        player1Position: Float = -5.0,
        player2Position: Float = 5.0,
        player1Velocity: Float = 0.0,
        player2Velocity: Float = 0.0,
        gameStatus: GameStatus = .playing,
        lastUpdateTime: Int64 = 0,
        player1Score: Int = 0,
        player2Score: Int = 0,
        collisionPosition: Float? = nil,
        collisionTimestamp: Int64 = 0
    ) {
        self.player1Position = player1Position
        self.player2Position = player2Position
        self.player1Velocity = player1Velocity
        self.player2Velocity = player2Velocity
        self.gameStatus = gameStatus
        self.lastUpdateTime = lastUpdateTime
        self.player1Score = player1Score
        self.player2Score = player2Score
        self.collisionPosition = collisionPosition
        self.collisionTimestamp = collisionTimestamp
    }
}

// MARK: - Multiplayer Game Data
struct MultiplayerGameData: Codable {
    var gameId: String
    var player1Id: String
    var player2Id: String
    var player1Position: Float
    var player2Position: Float
    var gameStatus: String
    var player1Score: Int
    var player2Score: Int
    var lastMovePlayerId: String
    var lastMoveTimestamp: Int64
    var collisionPosition: Float?
    var collisionTimestamp: Int64

    init(
        gameId: String = "",
        player1Id: String = "",
        player2Id: String = "",
        player1Position: Float = -5.0,
        player2Position: Float = 5.0,
        gameStatus: String = "PLAYING",
        player1Score: Int = 0,
        player2Score: Int = 0,
        lastMovePlayerId: String = "",
        lastMoveTimestamp: Int64 = 0,
        collisionPosition: Float? = nil,
        collisionTimestamp: Int64 = 0
    ) {
        self.gameId = gameId
        self.player1Id = player1Id
        self.player2Id = player2Id
        self.player1Position = player1Position
        self.player2Position = player2Position
        self.gameStatus = gameStatus
        self.player1Score = player1Score
        self.player2Score = player2Score
        self.lastMovePlayerId = lastMovePlayerId
        self.lastMoveTimestamp = lastMoveTimestamp
        self.collisionPosition = collisionPosition
        self.collisionTimestamp = collisionTimestamp
    }

    // SumoGameState로 변환
    func toSumoGameState() -> SumoGameState {
        return SumoGameState(
            player1Position: player1Position,
            player2Position: player2Position,
            player1Velocity: 0,
            player2Velocity: 0,
            gameStatus: GameStatus(rawValue: gameStatus) ?? .playing,
            lastUpdateTime: lastMoveTimestamp,
            player1Score: player1Score,
            player2Score: player2Score,
            collisionPosition: collisionPosition,
            collisionTimestamp: collisionTimestamp
        )
    }

    // SumoGameState에서 변환
    static func fromSumoGameState(
        gameId: String,
        player1Id: String,
        player2Id: String,
        state: SumoGameState,
        lastMovePlayerId: String
    ) -> MultiplayerGameData {
        return MultiplayerGameData(
            gameId: gameId,
            player1Id: player1Id,
            player2Id: player2Id,
            player1Position: state.player1Position,
            player2Position: state.player2Position,
            gameStatus: state.gameStatus.rawValue,
            player1Score: state.player1Score,
            player2Score: state.player2Score,
            lastMovePlayerId: lastMovePlayerId,
            lastMoveTimestamp: state.lastUpdateTime,
            collisionPosition: state.collisionPosition,
            collisionTimestamp: state.collisionTimestamp
        )
    }
}

// MARK: - Game Type
enum GameType: String, Codable {
    case SUMO = "SUMO"
    case MUKJJIPPA = "MUKJJIPPA"
}

// MARK: - Game Request Status
enum GameRequestStatus: String, Codable {
    case pending = "PENDING"
    case accepted = "ACCEPTED"
    case rejected = "REJECTED"
    case cancelled = "CANCELLED"
}

// MARK: - Game Request
struct GameRequest: Codable, Identifiable {
    var id: String { requestId }
    var requestId: String
    var fromUserId: String
    var toUserId: String
    var gameType: GameType
    var status: GameRequestStatus
    var timestamp: Int64
    var gameId: String?

    init(
        requestId: String = "",
        fromUserId: String = "",
        toUserId: String = "",
        gameType: GameType = .SUMO,
        status: GameRequestStatus = .pending,
        timestamp: Int64 = 0,
        gameId: String? = nil
    ) {
        self.requestId = requestId
        self.fromUserId = fromUserId
        self.toUserId = toUserId
        self.gameType = gameType
        self.status = status
        self.timestamp = timestamp
        self.gameId = gameId
    }
}

// MARK: - Mukjjippa Choice
enum MukjjippaChoice: String, Codable, CaseIterable {
    case ROCK = "ROCK"
    case SCISSORS = "SCISSORS"
    case PAPER = "PAPER"

    var displayName: String {
        switch self {
        case .ROCK: return "묵"
        case .SCISSORS: return "찌"
        case .PAPER: return "빠"
        }
    }

    func beats(_ other: MukjjippaChoice) -> Bool {
        switch (self, other) {
        case (.ROCK, .SCISSORS), (.SCISSORS, .PAPER), (.PAPER, .ROCK):
            return true
        default:
            return false
        }
    }

    func getCountdownMessage() -> String {
        return "\(displayName)에"
    }
}

// MARK: - Mukjjippa Phase
enum MukjjippaPhase: String, Codable {
    case ROCK_PAPER_SCISSORS = "ROCK_PAPER_SCISSORS"
    case MUKJJIPPA = "MUKJJIPPA"
    case GAME_OVER = "GAME_OVER"
}

// MARK: - Countdown State
enum CountdownState: String, Codable {
    case WAITING = "WAITING"
    case COUNTDOWN_1 = "COUNTDOWN_1"
    case COUNTDOWN_2 = "COUNTDOWN_2"
    case COUNTDOWN_3 = "COUNTDOWN_3"
    case RESULT_WAIT = "RESULT_WAIT"
    case SHOWING_RESULT = "SHOWING_RESULT"
}

// MARK: - Mukjjippa Game State
struct MukjjippaGameState {
    var phase: MukjjippaPhase
    var countdownState: CountdownState
    var currentMessage: String
    var jimScore: Int
    var hiScore: Int
    var jimChoice: MukjjippaChoice?
    var hiChoice: MukjjippaChoice?
    var attackerId: String?
    var previousAttackerChoice: MukjjippaChoice?
    var winner: String?
    var isGameFinished: Bool
    var bothPlayersReady: Bool

    init(
        phase: MukjjippaPhase = .ROCK_PAPER_SCISSORS,
        countdownState: CountdownState = .WAITING,
        currentMessage: String = "",
        jimScore: Int = 0,
        hiScore: Int = 0,
        jimChoice: MukjjippaChoice? = nil,
        hiChoice: MukjjippaChoice? = nil,
        attackerId: String? = nil,
        previousAttackerChoice: MukjjippaChoice? = nil,
        winner: String? = nil,
        isGameFinished: Bool = false,
        bothPlayersReady: Bool = false
    ) {
        self.phase = phase
        self.countdownState = countdownState
        self.currentMessage = currentMessage
        self.jimScore = jimScore
        self.hiScore = hiScore
        self.jimChoice = jimChoice
        self.hiChoice = hiChoice
        self.attackerId = attackerId
        self.previousAttackerChoice = previousAttackerChoice
        self.winner = winner
        self.isGameFinished = isGameFinished
        self.bothPlayersReady = bothPlayersReady
    }

    func isChoiceComplete() -> Bool {
        return jimChoice != nil && hiChoice != nil
    }

    func getChoiceForPlayer(_ playerId: String) -> MukjjippaChoice? {
        return playerId == "user_jim" ? jimChoice : hiChoice
    }

    func getAttackerDisplayName() -> String {
        return attackerId == "user_jim" ? "Jim" : "Hi"
    }

    func getWinnerDisplayName() -> String {
        return winner == "user_jim" ? "Jim" : "Hi"
    }

    func resetChoices() -> MukjjippaGameState {
        var newState = self
        newState.jimChoice = nil
        newState.hiChoice = nil
        newState.countdownState = .WAITING
        newState.currentMessage = ""
        return newState
    }
}

// MARK: - Multiplayer Mukjjippa Data
struct MultiplayerMukjjippaData: Codable {
    var gameId: String
    var player1Id: String
    var player2Id: String
    var gameType: String
    var phase: String
    var countdownState: String
    var currentMessage: String
    var jimScore: Int
    var hiScore: Int
    var jimChoice: String?
    var hiChoice: String?
    var attackerId: String?
    var previousAttackerChoice: String?
    var winner: String?
    var isGameFinished: Bool
    var bothPlayersReady: Bool
    var lastMovePlayerId: String
    var lastMoveTimestamp: Int64

    // CodingKeys to map Firebase field names to Swift property names
    enum CodingKeys: String, CodingKey {
        case gameId, player1Id, player2Id, gameType, phase
        case countdownState, currentMessage, jimScore, hiScore
        case jimChoice, hiChoice, attackerId, previousAttackerChoice
        case winner, bothPlayersReady, lastMovePlayerId, lastMoveTimestamp
        case isGameFinished = "gameFinished"  // Map Firebase's "gameFinished" to Swift's "isGameFinished"
    }

    init(
        gameId: String = "",
        player1Id: String = "",
        player2Id: String = "",
        gameType: String = GameType.MUKJJIPPA.rawValue,
        phase: String = MukjjippaPhase.ROCK_PAPER_SCISSORS.rawValue,
        countdownState: String = CountdownState.WAITING.rawValue,
        currentMessage: String = "",
        jimScore: Int = 0,
        hiScore: Int = 0,
        jimChoice: String? = nil,
        hiChoice: String? = nil,
        attackerId: String? = nil,
        previousAttackerChoice: String? = nil,
        winner: String? = nil,
        isGameFinished: Bool = false,
        bothPlayersReady: Bool = false,
        lastMovePlayerId: String = "",
        lastMoveTimestamp: Int64 = 0
    ) {
        self.gameId = gameId
        self.player1Id = player1Id
        self.player2Id = player2Id
        self.gameType = gameType
        self.phase = phase
        self.countdownState = countdownState
        self.currentMessage = currentMessage
        self.jimScore = jimScore
        self.hiScore = hiScore
        self.jimChoice = jimChoice
        self.hiChoice = hiChoice
        self.attackerId = attackerId
        self.previousAttackerChoice = previousAttackerChoice
        self.winner = winner
        self.isGameFinished = isGameFinished
        self.bothPlayersReady = bothPlayersReady
        self.lastMovePlayerId = lastMovePlayerId
        self.lastMoveTimestamp = lastMoveTimestamp
    }

    // MukjjippaGameState로 변환
    func toMukjjippaGameState() -> MukjjippaGameState {
        let phaseEnum = MukjjippaPhase(rawValue: phase) ?? .ROCK_PAPER_SCISSORS
        return MukjjippaGameState(
            phase: phaseEnum,
            countdownState: CountdownState(rawValue: countdownState) ?? .WAITING,
            currentMessage: currentMessage,
            jimScore: jimScore,
            hiScore: hiScore,
            jimChoice: jimChoice.flatMap { MukjjippaChoice(rawValue: $0) },
            hiChoice: hiChoice.flatMap { MukjjippaChoice(rawValue: $0) },
            attackerId: attackerId,
            previousAttackerChoice: previousAttackerChoice.flatMap { MukjjippaChoice(rawValue: $0) },
            winner: winner,
            // GAME_OVER 상태면 무조건 isGameFinished = true
            isGameFinished: phaseEnum == .GAME_OVER || isGameFinished,
            bothPlayersReady: bothPlayersReady
        )
    }

    // MukjjippaGameState에서 변환
    static func fromMukjjippaGameState(
        gameId: String,
        player1Id: String,
        player2Id: String,
        state: MukjjippaGameState,
        lastMovePlayerId: String
    ) -> MultiplayerMukjjippaData {
        return MultiplayerMukjjippaData(
            gameId: gameId,
            player1Id: player1Id,
            player2Id: player2Id,
            gameType: GameType.MUKJJIPPA.rawValue,
            phase: state.phase.rawValue,
            countdownState: state.countdownState.rawValue,
            currentMessage: state.currentMessage,
            jimScore: state.jimScore,
            hiScore: state.hiScore,
            jimChoice: state.jimChoice?.rawValue,
            hiChoice: state.hiChoice?.rawValue,
            attackerId: state.attackerId,
            previousAttackerChoice: state.previousAttackerChoice?.rawValue,
            winner: state.winner,
            isGameFinished: state.isGameFinished,
            bothPlayersReady: state.bothPlayersReady,
            lastMovePlayerId: lastMovePlayerId,
            lastMoveTimestamp: Int64(Date().timeIntervalSince1970 * 1000)
        )
    }
}

// MARK: - User Constants
struct UserConstants {
    static let USER_1 = "user_jim"
    static let USER_2 = "user_girlfriend"

    // 각 워치에서 다르게 설정해야 함
    static var CURRENT_USER_ID: String {
        get {
            USER_2
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "currentUserId")
        }
    }

    static var opponentUserId: String {
        return CURRENT_USER_ID == USER_1 ? USER_2 : USER_1
    }
}
