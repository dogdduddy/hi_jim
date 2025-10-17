package com.jim.hi_jim.shared.model

data class MultiplayerGameData(
    val gameId: String = "",
    val player1Id: String = "",  // 요청 보낸 사람
    val player2Id: String = "",  // 요청 받은 사람
    val player1Position: Float = -5f,
    val player2Position: Float = 5f,
    val gameStatus: String = "PLAYING",  // Firebase는 enum을 직접 지원하지 않으므로 String 사용
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val lastMovePlayerId: String = "",
    val lastMoveTimestamp: Long = 0L,
    val collisionPosition: Float? = null,
    val collisionTimestamp: Long = 0L
) {
    // Firebase에서 가져올 때 필요한 빈 생성자
    constructor() : this("", "", "", -5f, 5f, "PLAYING", 0, 0, "", 0L, null, 0L)

    // SumoGameState로 변환
    fun toSumoGameState(): SumoGameState {
        return SumoGameState(
            player1Position = player1Position,
            player2Position = player2Position,
            player1Velocity = 0f,
            player2Velocity = 0f,
            gameStatus = GameStatus.valueOf(gameStatus),
            lastUpdateTime = lastMoveTimestamp,
            player1Score = player1Score,
            player2Score = player2Score,
            collisionPosition = collisionPosition,
            collisionTimestamp = collisionTimestamp
        )
    }

    companion object {
        // SumoGameState에서 변환
        fun fromSumoGameState(
            gameId: String,
            player1Id: String,
            player2Id: String,
            state: SumoGameState,
            lastMovePlayerId: String
        ): MultiplayerGameData {
            return MultiplayerGameData(
                gameId = gameId,
                player1Id = player1Id,
                player2Id = player2Id,
                player1Position = state.player1Position,
                player2Position = state.player2Position,
                gameStatus = state.gameStatus.name,
                player1Score = state.player1Score,
                player2Score = state.player2Score,
                lastMovePlayerId = lastMovePlayerId,
                lastMoveTimestamp = state.lastUpdateTime,
                collisionPosition = state.collisionPosition,
                collisionTimestamp = state.collisionTimestamp
            )
        }
    }
}
