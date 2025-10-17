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
    var status: GameRequestStatus
    var timestamp: Int64
    var gameId: String?

    init(
        requestId: String = "",
        fromUserId: String = "",
        toUserId: String = "",
        status: GameRequestStatus = .pending,
        timestamp: Int64 = 0,
        gameId: String? = nil
    ) {
        self.requestId = requestId
        self.fromUserId = fromUserId
        self.toUserId = toUserId
        self.status = status
        self.timestamp = timestamp
        self.gameId = gameId
    }
}

// MARK: - User Constants
struct UserConstants {
    static let USER_1 = "user_jim"
    static let USER_2 = "user_girlfriend"

    // 각 워치에서 다르게 설정해야 함
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
