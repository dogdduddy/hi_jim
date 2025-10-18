package com.jim.hi_jim.presentation.engine.sumo

import android.util.Log
import com.jim.hi_jim.shared.model.GameStatus
import com.jim.hi_jim.shared.model.SumoGameState

class SumoPhysicsEngine {

    companion object {
        const val BOUNDARY = 10f
        const val STEP_SIZE = 0.8f     // 한 번 클릭 시 이동 거리
        const val PLAYER_RADIUS = 2.5f  // 플레이어 충돌 반지름 (테두리 포함 시각적 크기)
        const val PUSH_FORCE = 1.5f    // 충돌 시 상대를 밀어내는 힘
        const val IMPULSE = 0.5f       // 버튼 클릭 시 추가되는 속도
        const val FRICTION = 0.85f     // 마찰 계수 (속도 감쇠)
        const val MIN_VELOCITY = 0.05f // 최소 속도 (이 이하는 0으로 간주)
        const val COLLISION_ENERGY_TRANSFER = 0.7f // 충돌 시 에너지 전달 비율
    }

    fun processMove(
        currentState: SumoGameState,
        playerId: String,
        timestamp: Long
    ): SumoGameState {
        Log.d("PlayLog", "processMove $playerId")
        if (currentState.gameStatus != GameStatus.PLAYING) {
            return currentState
        }

        val isPlayer1 = playerId == "player1"

        // 1단계: 클릭한 플레이어만 이동, 클릭하지 않은 플레이어는 정지
        var p1 = currentState.player1Position
        var p2 = currentState.player2Position

        Log.d("PlayLog", "Before: p1=$p1, p2=$p2")

        // 2단계: 클릭한 플레이어만 STEP_SIZE만큼 이동
        if (isPlayer1) {
            p1 += STEP_SIZE  // Player 1은 오른쪽으로 이동
        } else {
            p2 -= STEP_SIZE  // Player 2는 왼쪽으로 이동
        }

        Log.d("PlayLog", "After move: p1=$p1, p2=$p2")

        // 3단계: 충돌 체크 및 상대 밀어내기
        val collision = checkCollisionAndPush(p1, p2, isPlayer1)
        p1 = collision.player1Position
        p2 = collision.player2Position

        // 4단계: 승리 조건 체크
        val status = checkWinCondition(p1, p2)

        // 5단계: 승리 시 스코어 업데이트
        val newP1Score = if (status == GameStatus.PLAYER1_WIN)
            currentState.player1Score + 1 else currentState.player1Score
        val newP2Score = if (status == GameStatus.PLAYER2_WIN)
            currentState.player2Score + 1 else currentState.player2Score

        return SumoGameState(
            player1Position = p1,
            player2Position = p2,
            player1Velocity = 0f,  // 속도는 항상 0 (즉시 정지)
            player2Velocity = 0f,
            gameStatus = status,
            lastUpdateTime = timestamp,
            player1Score = newP1Score,
            player2Score = newP2Score,
            collisionPosition = collision.collisionPosition,  // 충돌 위치 전달
            collisionTimestamp = if (collision.collisionPosition != null) timestamp else 0L
        )
    }

    private data class CollisionResult(
        val player1Position: Float,
        val player2Position: Float,
        val collisionPosition: Float? = null  // 충돌 발생 위치 (두 플레이어 중간 지점)
    )

    /**
     * 충돌 체크 및 상대 밀어내기
     * - 두 플레이어가 겹치면 충돌로 간주
     * - 충돌 시 둘 다 밀림: 움직인 플레이어는 약간, 상대는 더 많이
     */
    private fun checkCollisionAndPush(
        p1Pos: Float,
        p2Pos: Float,
        isPlayer1Moving: Boolean
    ): CollisionResult {
        val centerDistance = p2Pos - p1Pos
        val collisionThreshold = PLAYER_RADIUS * 2

        // 충돌 발생하지 않음
        if (centerDistance >= collisionThreshold) {
            return CollisionResult(p1Pos, p2Pos, null)
        }

        // 충돌 발생!
        Log.d("PlayLog", "Collision detected! Distance: $centerDistance, Threshold: $collisionThreshold")

        // 겹친 부분 계산
        val overlap = collisionThreshold - centerDistance

        // 충돌 지점 계산 (두 플레이어의 중간 지점)
        val collisionPoint = (p1Pos + p2Pos) / 2

        if (isPlayer1Moving) {
            // Player 1이 움직여서 충돌
            // - Player 1: 겹친 부분의 절반만큼 뒤로 밀림
            // - Player 2: 겹친 부분의 절반 + PUSH_FORCE만큼 밀림
            val newP1 = p1Pos - overlap / 2
            val newP2 = p2Pos + overlap / 2 + PUSH_FORCE
            return CollisionResult(newP1, newP2, collisionPoint)
        } else {
            // Player 2가 움직여서 충돌
            // - Player 1: 겹친 부분의 절반 + PUSH_FORCE만큼 밀림
            // - Player 2: 겹친 부분의 절반만큼 뒤로 밀림
            val newP1 = p1Pos - overlap / 2 - PUSH_FORCE
            val newP2 = p2Pos + overlap / 2
            return CollisionResult(newP1, newP2, collisionPoint)
        }
    }

    private fun checkWinCondition(p1Pos: Float, p2Pos: Float): GameStatus {
        return when {
            // Player 1이 왼쪽 경계 밖으로 밀려나면 Player 2 승리
            p1Pos < -BOUNDARY -> GameStatus.PLAYER2_WIN
            // Player 2가 오른쪽 경계 밖으로 밀려나면 Player 1 승리
            p2Pos > BOUNDARY -> GameStatus.PLAYER1_WIN
            else -> GameStatus.PLAYING
        }
    }

    /**
     * 게임 라운드 리셋 (스코어는 유지)
     */
    fun resetRound(currentScore1: Int, currentScore2: Int): SumoGameState {
        return SumoGameState(
            player1Score = currentScore1,
            player2Score = currentScore2
        )
    }

    /**
     * 전체 게임 리셋 (스코어도 초기화)
     */
    fun resetGame(): SumoGameState {
        return SumoGameState()
    }
}