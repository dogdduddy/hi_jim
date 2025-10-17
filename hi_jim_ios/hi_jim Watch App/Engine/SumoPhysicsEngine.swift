//
//  SumoPhysicsEngine.swift
//  hi_jim Watch App
//
//  게임 물리 엔진 (KMP 공유 모듈과 동일한 로직)
//

import Foundation

class SumoPhysicsEngine {
    // 게임 상수
    static let BOUNDARY: Float = 10.0
    static let STEP_SIZE: Float = 0.8
    static let PLAYER_RADIUS: Float = 2.5
    static let PUSH_FORCE: Float = 1.5
    static let IMPULSE: Float = 0.5
    static let FRICTION: Float = 0.85
    static let MIN_VELOCITY: Float = 0.05
    static let COLLISION_ENERGY_TRANSFER: Float = 0.7

    // 플레이어 이동 처리
    func processMove(
        currentState: SumoGameState,
        playerId: String,
        timestamp: Int64
    ) -> SumoGameState {
        if currentState.gameStatus != .playing {
            return currentState
        }

        let isPlayer1 = (playerId == "player1")

        // 1단계: 현재 위치
        var p1 = currentState.player1Position
        var p2 = currentState.player2Position

        // 2단계: 클릭한 플레이어만 STEP_SIZE만큼 이동
        if isPlayer1 {
            p1 += SumoPhysicsEngine.STEP_SIZE  // Player 1은 오른쪽으로
        } else {
            p2 -= SumoPhysicsEngine.STEP_SIZE  // Player 2는 왼쪽으로
        }

        // 3단계: 충돌 체크 및 상대 밀어내기
        let collision = checkCollisionAndPush(p1Pos: p1, p2Pos: p2, isPlayer1Moving: isPlayer1)
        p1 = collision.player1Position
        p2 = collision.player2Position

        // 4단계: 승리 조건 체크
        let status = checkWinCondition(p1Pos: p1, p2Pos: p2)

        // 5단계: 승리 시 스코어 업데이트
        let newP1Score = (status == .player1Win) ? currentState.player1Score + 1 : currentState.player1Score
        let newP2Score = (status == .player2Win) ? currentState.player2Score + 1 : currentState.player2Score

        return SumoGameState(
            player1Position: p1,
            player2Position: p2,
            player1Velocity: 0,
            player2Velocity: 0,
            gameStatus: status,
            lastUpdateTime: timestamp,
            player1Score: newP1Score,
            player2Score: newP2Score,
            collisionPosition: collision.collisionPosition,
            collisionTimestamp: collision.collisionPosition != nil ? timestamp : 0
        )
    }

    // 충돌 결과
    private struct CollisionResult {
        let player1Position: Float
        let player2Position: Float
        let collisionPosition: Float?
    }

    // 충돌 체크 및 밀어내기
    private func checkCollisionAndPush(
        p1Pos: Float,
        p2Pos: Float,
        isPlayer1Moving: Bool
    ) -> CollisionResult {
        let centerDistance = p2Pos - p1Pos
        let collisionThreshold = SumoPhysicsEngine.PLAYER_RADIUS * 2

        // 충돌 발생하지 않음
        if centerDistance >= collisionThreshold {
            return CollisionResult(
                player1Position: p1Pos,
                player2Position: p2Pos,
                collisionPosition: nil
            )
        }

        // 충돌 발생!
        let overlap = collisionThreshold - centerDistance
        let collisionPoint = (p1Pos + p2Pos) / 2

        if isPlayer1Moving {
            // Player 1이 움직여서 충돌
            let newP1 = p1Pos - overlap / 2
            let newP2 = p2Pos + overlap / 2 + SumoPhysicsEngine.PUSH_FORCE
            return CollisionResult(
                player1Position: newP1,
                player2Position: newP2,
                collisionPosition: collisionPoint
            )
        } else {
            // Player 2가 움직여서 충돌
            let newP1 = p1Pos - overlap / 2 - SumoPhysicsEngine.PUSH_FORCE
            let newP2 = p2Pos + overlap / 2
            return CollisionResult(
                player1Position: newP1,
                player2Position: newP2,
                collisionPosition: collisionPoint
            )
        }
    }

    // 승리 조건 체크
    private func checkWinCondition(p1Pos: Float, p2Pos: Float) -> GameStatus {
        if p1Pos < -SumoPhysicsEngine.BOUNDARY {
            return .player2Win
        } else if p2Pos > SumoPhysicsEngine.BOUNDARY {
            return .player1Win
        } else {
            return .playing
        }
    }

    // 라운드 리셋 (스코어 유지)
    func resetRound(currentScore1: Int, currentScore2: Int) -> SumoGameState {
        return SumoGameState(
            player1Score: currentScore1,
            player2Score: currentScore2
        )
    }

    // 전체 게임 리셋
    func resetGame() -> SumoGameState {
        return SumoGameState()
    }
}
