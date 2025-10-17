package com.jim.hi_jim.shared.model

data class SumoGameState(
    val player1Position: Float = -5f,
    val player2Position: Float = 5f,
    val player1Velocity: Float = 0f,   // Player 1의 속도
    val player2Velocity: Float = 0f,   // Player 2의 속도
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val lastUpdateTime: Long = 0L,
    val player1Score: Int = 0,         // 승리 횟수로 변경
    val player2Score: Int = 0,         // 승리 횟수로 변경
    val collisionPosition: Float? = null,  // 충돌 발생 위치 (null이면 충돌 없음)
    val collisionTimestamp: Long = 0L      // 충돌 발생 시간
)